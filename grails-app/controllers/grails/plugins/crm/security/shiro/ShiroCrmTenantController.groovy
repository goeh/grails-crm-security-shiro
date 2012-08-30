/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.security.shiro

import javax.servlet.http.HttpServletResponse
import org.springframework.web.servlet.support.RequestContextUtils as RCU
import org.springframework.dao.DataIntegrityViolationException
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.CrmException
import grails.plugins.crm.core.DateUtils

/**
 * This controller lets a user manage her account/tenant.
 */
class ShiroCrmTenantController {

    static allowedMethods = [index: 'GET', create: ['GET', 'POST'], activate: ['GET', 'POST']]

    private static final List TENANT_BIND_WHITELIST = ['name']

    def crmSecurityService
    def shiroCrmSecurityService
    def crmInvitationService
    def crmFeatureService

    private boolean checkPermission(grails.plugins.crm.security.shiro.ShiroCrmTenant account) {
        account.user.guid == crmSecurityService.currentUser?.guid
    }

    def index() {
        def user = crmSecurityService.currentUser
        if (!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        [shiroCrmUser: user, shiroCrmTenantList: crmSecurityService.tenants]
    }

    def activate(Long id) {
        if (crmSecurityService.isValidTenant(id)) {
            def oldTenant = TenantUtils.getTenant()
            if (id != oldTenant) {
                TenantUtils.setTenant(id)
                request.session.tenant = id
                event(for: "crm", topic: "tenantChanged", data: [newTenant: id, oldTenant: oldTenant, request: request])
            }
            if (params.referer) {
                redirect(uri: params.referer - request.contextPath)
            } else {
                redirect(mapping: 'start')
            }
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
        }
    }

    def create() {
        def shiroCrmUser = shiroCrmSecurityService.getUser()
        if (!shiroCrmUser) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        def shiroCrmTenant = new ShiroCrmTenant()
        bindData(shiroCrmTenant, params, [include: ['name', 'options']])

        switch (request.method) {
            case 'GET':
                shiroCrmTenant.clearErrors()
                break
            case 'POST':
                shiroCrmTenant.user = shiroCrmUser // To get validate to pass user must be set.
                if (shiroCrmTenant.validate()) {
                    try {
                        def options = [locale:RCU.getLocale(request)]
                        def trialDays = grailsApplication.config.crm.tenant.trialDays
                        if (trialDays) {
                            options.expires = new java.sql.Date(DateUtils.endOfWeek(trialDays).time)
                        }
                        def tenant = crmSecurityService.createTenant(shiroCrmTenant.name, options)
                        def id = tenant.id
                        if (!TenantUtils.tenant) {
                            // No active tenant, set the newly created tenant as active.
                            TenantUtils.setTenant(id)
                            request.session.tenant = id
                        }
                        if (params.boolean('defaultTenant')) {
                            crmSecurityService.updateUser(shiroCrmUser.username, [defaultTenant: id])
                        }
                        def features = params.list('features')
                        for (f in features) {
                            def appFeature = crmFeatureService.getApplicationFeature(f)
                            if(appFeature && !appFeature.enabled) {
                                crmFeatureService.enableFeature(f, id)
                            }
                        }
                        def installedFeatures = features.collect {
                            g.message(code: 'feature.' + it + '.label', default: it)
                        }
                        flash.success = message(code: 'shiroCrmTenant.created.message', args: [message(code: 'account.label', default: 'Account'),
                                tenant.name, installedFeatures.join(', ')])
                        redirect(action: "index")
                        return
                    } catch (Exception e) {
                        log.error(e)
                        flash.error = message(code: 'shiroCrmTenant.error', args: [e.message])
                    }
                }
                break
        }
        return [shiroCrmUser: shiroCrmUser, shiroCrmTenant: shiroCrmTenant, features:[], allFeatures: crmFeatureService.applicationFeatures]
    }

    def edit() {

        def shiroCrmTenant = grails.plugins.crm.security.shiro.ShiroCrmTenant.get(params.id)
        if (!shiroCrmTenant) {
            flash.error = message(code: 'shiroCrmTenant.not.found.message', args: [message(code: 'shiroCrmTenant.label', default: 'Account'), params.id])
            redirect action: 'index'
            return
        }
        if (!checkPermission(shiroCrmTenant)) {
            flash.error = message(code: 'shiroCrmTenant.permission.denied', args: [message(code: 'shiroCrmTenant.label', default: 'Account'), params.id])
            redirect action: 'index'
            return
        }

        def invitations = crmInvitationService ? crmInvitationService.getInvitationsFor(shiroCrmTenant, shiroCrmTenant.id) : []

        switch (request.method) {
            case 'GET':
                def allFeatures = crmFeatureService.applicationFeatures
                def features = crmFeatureService.getFeatures(shiroCrmTenant.id)
                def existingFeatureNames = features*.name
                def moreFeatures = allFeatures.findAll {
                    if (it.hidden || existingFeatureNames.contains(it.name)) {
                        return false
                    }
                    return true
                }
                def showCosts = shiroCrmTenant.getOption('costs')
                return [shiroCrmTenant: shiroCrmTenant, user: crmSecurityService.currentUser,
                        permissions: shiroCrmSecurityService.getTenantPermissions(shiroCrmTenant.id),
                        showCosts: showCosts, invitationList: invitations, features: features, moreFeatures: moreFeatures]
            case 'POST':
                if (params.version) {
                    def version = params.version.toLong()
                    if (shiroCrmTenant.version > version) {
                        shiroCrmTenant.errors.rejectValue('version', 'shiroCrmTenant.optimistic.locking.failure',
                                [message(code: 'shiroCrmTenant.label', default: 'Account')] as Object[],
                                "Another user has updated this account while you were editing")
                        render view: 'edit', model: [shiroCrmTenant: shiroCrmTenant, user: crmSecurityService.currentUser]
                        return
                    }
                }

                bindData(shiroCrmTenant, params, [include: TENANT_BIND_WHITELIST])

                if (!shiroCrmTenant.save(flush: true)) {
                    render view: 'edit', model: [shiroCrmTenant: shiroCrmTenant, user: crmSecurityService.currentUser]
                    return
                }

                def defaultTenant = params.boolean('defaultTenant')
                if (defaultTenant) {
                    crmSecurityService.updateUser(null, [defaultTenant: shiroCrmTenant.id])
                }

                if (params.boolean('showCosts')) {
                    shiroCrmTenant.setOption('costs', true)
                } else {
                    shiroCrmTenant.removeOption('costs')
                }

                flash.success = message(code: 'shiroCrmTenant.updated.message', args: [message(code: 'shiroCrmTenant.label', default: 'Account'), shiroCrmTenant.toString()])
                redirect action: 'index'
                break
        }
    }

    def delete(Long id) {
        def shiroCrmTenant = grails.plugins.crm.security.shiro.ShiroCrmTenant.get(id)
        if (!shiroCrmTenant) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
        try {
            def tombstone = shiroCrmTenant.toString()
            crmSecurityService.deleteTenant(id)
            flash.warning = message(code: 'shiroCrmTenant.deleted.message', args: [message(code: 'shiroCrmTenant.label', default: 'Account'), tombstone])
            redirect action: 'index'
        } catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'shiroCrmTenant.not.deleted.message', args: [message(code: 'shiroCrmTenant.label', default: 'Account'), id])
            redirect action: 'edit', id: id
        } catch (CrmException crme) {
            flash.error = message(code: crme.message, args: crme.args)
            redirect action: 'edit', id: id
        }
    }

    def share(Long id, String email, String msg, String role) {
        def shiroCrmTenant = grails.plugins.crm.security.shiro.ShiroCrmTenant.get(id)
        if (!shiroCrmTenant) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }

        event(for: "crm", topic: "tenantShared", data: [id: id, email: email, role: role, message: msg, user: crmSecurityService.currentUser.username])

        flash.success = message(code: 'account.share.success.message', args: [shiroCrmTenant.name, email, msg])

        if (params.referer) {
            redirect(uri: params.referer - request.contextPath)
        } else {
            redirect action: 'edit', id: id, fragment: 'perm'
        }
    }
}

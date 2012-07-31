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

import org.springframework.dao.DataIntegrityViolationException

/**
 * User administration.
 */
class ShiroCrmUserController {

    static allowedMethods = [list: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 70,
                    title: 'shiroCrmUser.index.label',
                    action: 'index'
            ]
    ]

    def grailsApplication
    def shiroCrmSecurityService
    def crmUserService
    def selectionService

    def index() {
       // If any query parameters are specified in the URL, let them override the last query stored in session.
        def cmd = new CrmUserQueryCommand()
        def query = params.getSelectionQuery()
        bindData(cmd, query ?: session.crmUserQuery)
        [cmd: cmd]
    }

    def list() {
        def baseURI = new URI('bean://crmUserService/list')
        def query = params.getSelectionQuery()
        def uri

        switch (request.method) {
            case 'GET':
                uri = params.getSelectionURI() ?: selectionService.addQuery(baseURI, query)
                break
            case 'POST':
                uri = selectionService.addQuery(baseURI, query)
                session.crmUserQuery = query
                break
        }

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def result
        try {
            result = selectionService.select(uri, params)
            [result: result, totalCount: result.totalCount, selection: uri]
        } catch (Exception e) {
            flash.error = e.message
            [result: [], totalCount: 0, selection: uri]
        }
    }

    def clearQuery() {
        session.crmUserQuery = null
        redirect(action: "index")
    }

    def show() {
        def shiroCrmUser = ShiroCrmUser.get(params.id)
        if (!shiroCrmUser) {
            flash.error = message(code: 'shiroCrmUser.not.found.message', args: [message(code: 'shiroCrmUser.label', default: 'User'), params.id])
            redirect action: 'index'
            return
        }
        def tenants = shiroCrmSecurityService.getTenants(shiroCrmUser.username)
        [shiroCrmUser: shiroCrmUser, tenantList:tenants]
    }

    def edit() {
        def shiroCrmUser = ShiroCrmUser.get(params.id)
        if (!shiroCrmUser) {
            flash.error = message(code: 'shiroCrmUser.not.found.message', args: [message(code: 'shiroCrmUser.label', default: 'User'), params.id])
            redirect action: 'index'
            return
        }

        switch (request.method) {
            case 'GET':
                return [shiroCrmUser: shiroCrmUser]
            case 'POST':
                if (params.version && shiroCrmUser.version) {
                    def version = params.version.toLong()
                    if (shiroCrmUser.version > version) {
                        shiroCrmUser.errors.rejectValue('version', 'shiroCrmUser.optimistic.locking.failure',
                                [message(code: 'shiroCrmUser.label', default: 'User')] as Object[],
                                "Another user has updated this user while you were editing")
                        render view: 'edit', model: [shiroCrmUser: shiroCrmUser]
                        return
                    }
                }

                bindData(shiroCrmUser, params, [include: ShiroCrmUser.BIND_WHITELIST])

                if (!shiroCrmUser.save(flush: true)) {
                    render view: 'edit', model: [shiroCrmUser: shiroCrmUser]
                    return
                }

                flash.success = message(code: 'shiroCrmUser.updated.message', args: [message(code: 'shiroCrmUser.label', default: 'User'), shiroCrmUser.toString()])
                redirect action: 'show', id: shiroCrmUser.id
                break
        }
    }

    def delete() {
        def shiroCrmUser = ShiroCrmUser.get(params.id)
        if (!shiroCrmUser) {
            flash.error = message(code: 'shiroCrmUser.not.found.message', args: [message(code: 'shiroCrmUser.label', default: 'User'), params.id])
            redirect action: 'index'
            return
        }

        try {
            def tombstone = shiroCrmUser.toString()
            shiroCrmUser.delete(flush: true)
            flash.warning = message(code: 'shiroCrmUser.deleted.message', args: [message(code: 'shiroCrmUser.label', default: 'User'), tombstone])
            redirect action: 'index'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'shiroCrmUser.not.deleted.message', args: [message(code: 'shiroCrmUser.label', default: 'User'), params.id])
            redirect action: 'show', id: params.id
        }
    }

    def reset(Long id, Long tenantId) {
        def shiroCrmUser = ShiroCrmUser.get(id)
        if (!shiroCrmUser) {
            flash.error = message(code: 'shiroCrmUser.not.found.message', args: [message(code: 'shiroCrmUser.label', default: 'User'), id])
            redirect action: 'index'
            return
        }

        event(for:"crm", topic:"resetPermissions", data: [tenant:tenantId, username:shiroCrmUser.username])

        flash.warning = message(code: 'shiroCrmUser.permission.reset.message', default:"Permissions reset for user [{0}]", args:[shiroCrmUser.toString()])

        redirect action: 'show', id: id, fragment: 't' + tenantId
    }

}

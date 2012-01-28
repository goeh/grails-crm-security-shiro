/*
 *  Copyright 2012 Goran Ehrsson.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package grails.plugins.crm.security.shiro

import javax.servlet.http.HttpServletResponse
import grails.converters.JSON
import grails.plugins.crm.core.TenantUtils

class CrmUserController {

    def crmSecurityService
    def shiroCrmSecurityService

    def index() {
        def user = crmSecurityService.currentUser
        if(!user) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
            return
        }
        [user:user]
    }

    def chart() {
    }

    def currentTenant() {
        def tenant = TenantUtils.tenant
        def result
        if (tenant) {
            try {
                def info = shiroCrmSecurityService.tenantInfo(tenant)
                result = [success: true, account: info]
            } catch (Exception e) {
                result = [success: false, error: [code: 'tenant.not.found', message: e.message]]
            }
        } else {
            result = [success: true, account: null]
        }
        render result as JSON
    }

    def changeTenant(Long id) {
        def tenant = shiroCrmSecurityService.tenantInfo(id)
        def username = crmSecurityService.currentUser?.username
        // TODO other than tenant owners must be able to switch to tenant.
        if (tenant?.owner != username) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        TenantUtils.setTenant(id)

        redirect(action:'index', params:[tenant:id])
    }

    def createTenant(String name) {
        def result
        try {
            def tenantInfo = shiroCrmSecurityService.createTenant(name)
            result = [success: true, account: tenantInfo]
        } catch (Exception e) {
            result = [success: false, error: [code: 'tenant.error.save', message: e.message]]
        }
        render result as JSON
    }

    def listTenants() {
        def result
        try {
            def currentTenant = TenantUtils.tenant
            def tenants = crmSecurityService.getTenants()
            result = [success:true, list:tenants.collect {[id:it.id, name:it.name, owner:it.owner, current:currentTenant == it.id]}]
        } catch (Exception e) {
            result = [success: false, error: [code: 'tenant.error.list', message: e.message]]
        }
        render result as JSON
    }

    def deleteTenant(Long id) {
        def tenant = shiroCrmSecurityService.tenantInfo(id)
        def username = crmSecurityService.currentUser?.username
        if (tenant?.owner != username) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }
        def result
        try {
            shiroCrmSecurityService.deleteTenant(id)
            result = [success:true]
        } catch (Exception e) {
            result = [success: false, error: [code: 'tenant.error.delete', message: e.message]]
        }
        render result as JSON
    }
}

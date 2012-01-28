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

import org.apache.shiro.SecurityUtils
import grails.plugins.crm.core.TenantUtils
import org.apache.shiro.subject.Subject
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.mgt.DefaultSecurityManager

/**
 * Apache Shiro implementation of a security delegate
 * used by CrmSecurityService.
 */
class ShiroCrmSecurityDelegate {

    def shiroSecurityManager

    boolean isAuthenticated() {
        SecurityUtils.subject?.isAuthenticated()
    }

    def runAs(String username, Closure closure) {
        def realm = shiroSecurityManager.realms.find {it}
        def bootstrapSecurityManager = new DefaultSecurityManager(realm)
        def principals = new SimplePrincipalCollection(username, realm.name)
        def subject = new Subject.Builder(bootstrapSecurityManager).principals(principals).buildSubject()
        subject.execute(closure)
    }

    def getCurrentUser() {
        def username = SecurityUtils.subject?.principal
        return username ? ShiroCrmUser.findByUsername(username.toString())?.dao : null
    }

    def getCurrentTenant() {
        def tenant = TenantUtils.tenant
        return tenant ? ShiroCrmTenant.get(tenant)?.dao : null
    }

    List getTenants() {
        def username = SecurityUtils.subject.principal?.toString()
        if (!username) {
            throw new IllegalArgumentException("not authenticated")
        }
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        ShiroCrmTenant.findAllByOwner(user)*.dao
    }
}

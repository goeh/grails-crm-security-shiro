/*
 * Copyright 2012 Goran Ehrsson.
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

import org.apache.shiro.SecurityUtils
import grails.plugins.crm.core.TenantUtils
import org.apache.shiro.subject.Subject
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.authz.UnauthorizedException

/**
 * Apache Shiro implementation of a security delegate
 * used by CrmSecurityService.
 */
class ShiroCrmSecurityDelegate {

    def shiroSecurityManager
    def credentialMatcher

    boolean isAuthenticated() {
        SecurityUtils.subject?.isAuthenticated()
    }

    boolean isPermitted(permission) {
        def tenant = TenantUtils.tenant.toString()
        SecurityUtils.subject?.isPermitted(permission.toString())
    }

    def runAs(String username, Closure closure) {
        def user = ShiroCrmUser.findByUsernameAndEnabled(username, true)
        if(! user) {
            throw new UnauthorizedException("[$username] is not a valid user")
        }
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

    /**
     * Return all tenants that the current user owns.
     * @return list of tenants (DAO)
     */
    List getTenants() {
        def username = SecurityUtils.subject.principal?.toString()
        if (!username) {
            throw new IllegalArgumentException("not authenticated")
        }
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        ShiroCrmTenant.findAllByUser(user)*.dao
    }

    /**
     * Check if current user can access the specified tenant.
     * @param tenantId the tenant ID to check
     * @return true if user has access to the tenant (by it's roles, permissions or ownership)
     */
    boolean isValidTenant(Long tenantId) {
        def username = SecurityUtils.subject.principal?.toString()
        if (!username) {
            throw new IllegalArgumentException("not authenticated")
        }
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        if (user.accounts.find {it.id == tenantId}) {
            return true // User own this tenant
        }
        if (user.permissions.find {it.tenantId == tenantId}) {
            return true // User has individual permission for this tenant
        }
        if (user.roles.find {it.role.tenantId == tenantId}) {
            return true // User's role gives permission to the tenant.
        }
        return false
    }

    def hashPassword(String password, byte[] salt) {
        new Sha512Hash(password, salt, credentialMatcher.hashIterations ?: 1).toHex()
    }

    byte[] generateSalt() {
        new SecureRandomNumberGenerator().nextBytes().getBytes()
    }
}

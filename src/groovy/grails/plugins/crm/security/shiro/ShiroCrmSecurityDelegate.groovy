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
import grails.plugins.crm.core.SecurityServiceDelegate
import grails.plugins.crm.core.CrmException

/**
 * Apache Shiro implementation of a security delegate
 * used by CrmSecurityService.
 */
class ShiroCrmSecurityDelegate implements SecurityServiceDelegate {

    def shiroSecurityManager
    def credentialMatcher
    def asyncEventPublisher

    boolean isAuthenticated() {
        SecurityUtils.subject?.isAuthenticated()
    }

    boolean isPermitted(permission) {
        SecurityUtils.subject?.isPermitted(permission.toString())
    }

    def runAs(String username, Closure closure) {
        def user = ShiroCrmUser.findByUsernameAndEnabled(username, true)
        if (!user) {
            throw new UnauthorizedException("[$username] is not a valid user")
        }
        def realm = shiroSecurityManager.realms.find {it}
        def bootstrapSecurityManager = new DefaultSecurityManager(realm)
        def principals = new SimplePrincipalCollection(username, realm.name)
        def subject = new Subject.Builder(bootstrapSecurityManager).principals(principals).buildSubject()
        subject.execute(closure)
    }

    Map<String, Object> getCurrentUser() {
        def username = SecurityUtils.subject?.principal
        return username ? ShiroCrmUser.findByUsername(username.toString(), [cache:true])?.dao : null
    }

    Map<String, Object> getCurrentTenant() {
        def tenant = TenantUtils.tenant
        return tenant ? ShiroCrmTenant.get(tenant)?.dao : null
    }

    /**
     * Return all tenants that a user owns.
     *
     * @param username username
     * @return list of tenants (DAO)
     */
    List<Map<String, Object>> getTenants(String username) {
        ShiroCrmTenant.createCriteria().list() {
            user {
                eq('username', username)
            }
            cache true
        }*.dao
    }

    /**
     * Check if current user can access the specified tenant.
     * @param username username
     * @param tenantId the tenant ID to check
     * @return true if user has access to the tenant (by it's roles, permissions or ownership)
     */
    boolean isValidTenant(String username, Long tenantId) {
        def user = ShiroCrmUser.findByUsername(username, [cache:true])
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

    Map<String, Object> createUser(Map<String, Object> props) {
        if (ShiroCrmUser.findByUsername(props.username, [cache:true])) {
            throw new CrmException("createUser.user.exists.error", [props.username])
        }
        def safeProps = props.findAll {ShiroCrmUser.BIND_WHITELIST.contains(it.key)}
        def user = new ShiroCrmUser(safeProps)
        if(props.password) {
            def salt = generateSalt()
            user.passwordHash = hashPassword(props.password, salt)
            user.passwordSalt = salt.encodeBase64().toString()
        }

        user.save(failOnError: true, flush: true)

        if (asyncEventPublisher) {
            asyncEventPublisher.publishEvent(new UserCreatedEvent(user))
        }

        return user.dao
    }

    Map getUserInfo(String username) {
        ShiroCrmUser.findByUsername(username, [cache:true])?.dao
    }

    Map<String, Object> createTenant(String tenantName, String tenantType, Long parent, String owner) {
        if (!tenantName) {
            throw new IllegalArgumentException("tenantName is null")
        }
        if (!tenantType) {
            throw new IllegalArgumentException("tenantType is null")
        }
        if(!owner) {
            owner = SecurityUtils.subject.principal?.toString()
            if (!owner) {
                throw new IllegalArgumentException("not authenticated")
            }
        }
        def user = ShiroCrmUser.findByUsername(owner, [cache:true])
        if (!user) {
            throw new IllegalArgumentException("user [$owner] not found")
        }
        def existing = ShiroCrmTenant.findByUserAndName(user, tenantName, [cache:true])
        if (existing) {
            throw new IllegalArgumentException("Tenant [$tenantName] already exists")
        }
        def parentTenant
        if (parent) {
            parentTenant = ShiroCrmTenant.get(parent)
            if (!parentTenant) {
                throw new IllegalArgumentException("Parent tenant [$parent] does not exist")
            }
        }
        def tenant = new ShiroCrmTenant(name: tenantName, type: tenantType, parent: parentTenant)
        user.discard()
        user = ShiroCrmUser.lock(user.id)
        user.addToAccounts(tenant)
        user.save(flush: true)

        // Use Spring Events plugin to broadcast that a new tenant was created.
        // Receivers could for example assign default roles and permissions for this tenant.
        if (asyncEventPublisher) {
            asyncEventPublisher.publishEvent(new TenantCreatedEvent(tenant))
        }

        return tenant.dao
    }

    Map<String, Object> getTenantInfo(Long id) {
        ShiroCrmTenant.get(id)?.dao
    }
}

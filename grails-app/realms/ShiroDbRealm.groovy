/*
 * Copyright (c) 2014 Goran Ehrsson.
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


import grails.plugins.crm.security.shiro.ShiroCrmUser
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.crypto.hash.Sha256Hash
import org.apache.shiro.util.SimpleByteSource

import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.security.CrmUser
import grails.plugins.crm.security.CrmUserRole
import grails.plugins.crm.security.CrmUserPermission

class ShiroDbRealm {

    static authTokenClass = org.apache.shiro.authc.UsernamePasswordToken

    def credentialMatcher
    def shiroPermissionResolver
    def crmSecurityService

    def authenticate(authToken) {
        log.debug "Attempting to authenticate ${authToken.username} in DB realm..."
        def username = authToken.username

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Blank username is not allowed by this realm.")
        }

        // 2016-01-13 goeh
        // When using Basic Authentication (org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter)
        // We get "org.hibernate.HibernateException: No Session found for current thread".
        // Wrapping all DB accesses in 'withNewSession' makes it work again.
        // This *was* working in Grails 2.2.4 but not in Grails 2.5.2. I don't have time right to investigate more.
        CrmUser.withNewSession {
            // Get the user with the given username. If the user is not
            // found, then they don't have an account and we throw an
            // exception.
            def user = CrmUser.findByUsername(username)
            if (!user) {
                // Try with email instead of username and see if we get a unique record.
                def users = CrmUser.findAllByEmail(username)
                if (users.size() == 1) {
                    user = users.find { it }
                } else {
                    throw new UnknownAccountException("No DB account found for user [${username}]")
                }
            }
            if (!user.enabled) {
                throw new UnknownAccountException("Account is disabled [${username}]")
            }
            log.info "Found CrmUser [${user.username}] in DB"

            // DB queries could be case-insensitive (MySQL is by default) or user was found through the email property.
            // Therefore we need to get the real username from DB.
            username = user.username

            def shiroCrmUser = ShiroCrmUser.findByUsername(username)
            if (!shiroCrmUser) {
                log.error "A CrmUser was found with username [${username}] but no matching ShiroCrmUser!"
                throw new UnknownAccountException("No ShiroCrmUser found with username [${username}]")
            }
            // Now check the user's password against the hashed value stored
            // in the database.
            def salt
            if (shiroCrmUser.passwordSalt) {
                salt = shiroCrmUser.passwordSalt.decodeBase64()
            } else {
                salt = username.bytes
            }
            def account = new SimpleAccount(username, shiroCrmUser.passwordHash, new SimpleByteSource(salt), ShiroDbRealm.name)
            if (!credentialMatcher.doCredentialsMatch(authToken, account)) {
                // If that didn't work, try with the weaker password hash that was used in older versions.
                // If that password match, upgrade the hash to stronger encryption.
                def password = new String(authToken.password)
                def md5hash = password.encodeAsMD5()
                def sha256hash = new Sha256Hash(password, user.email).toHex()
                if (sha256hash == shiroCrmUser.passwordHash || md5hash == shiroCrmUser.passwordHash) {
                    log.warn "Upgrading security for user ${user.username}..."
                    crmSecurityService.updateUser(user, [password: password])
                } else {
                    user.loginFailures = user.loginFailures + 1
                    user.save(flush: true)
                    if (!user.enabled) {
                        log.warn "To many login failures, account [$user] is disabled"
                        throw new UnknownAccountException("Account is disabled [${username}]")
                    }
                    log.info "Invalid password (DB realm)"
                    throw new IncorrectCredentialsException("Invalid password for user '${username}'")
                }
            }
            if (user.loginFailures > 0) {
                log.info "Login successful, resetting login failures for $user"
                user.loginFailures = 0
            }

            try {
                def availableTenants = crmSecurityService.getTenants(username)*.id
                setDefaultTenant(username, availableTenants)
            } catch (Exception e) {
                log.error("Failed to set default tenant for user [$username]", e)
            }

            return account
        }
    }

    Long setDefaultTenant(String username, Collection<Long> availableTenants) {
        def user = crmSecurityService.getUser(username)
        def tenant = availableTenants.find { user.defaultTenant == null || user.defaultTenant == it }
        if (tenant != null) {
            TenantUtils.tenant = tenant
            def session = SecurityUtils.getSubject()?.getSession(true)
            if (session) {
                session.setAttribute('tenant', tenant)
            }
        }
        tenant
    }

    def hasRole(principal, roleName) {
        def tenant = TenantUtils.getTenant()
        def roles = CrmUserRole.withCriteria {
            projections {
                property("id")
            }
            user {
                eq("username", principal)
                eq("status", CrmUser.STATUS_ACTIVE)
            }
            role {
                eq("name", roleName)
                eq('tenantId', tenant)
            }
            cache true
        }

        return !roles.isEmpty()
    }

    def hasAllRoles(principal, roles) {
        def tenant = TenantUtils.getTenant()
        def result = CrmUserRole.withCriteria {
            projections {
                property("id")
            }
            user {
                eq("username", principal)
                eq("status", CrmUser.STATUS_ACTIVE)
            }
            role {
                inList("name", roles)
                eq('tenantId', tenant)
            }
            cache true
        }

        return result.size() == roles.size()
    }

    private boolean implies(Object requiredPermission, String permString) {
        for (p in crmSecurityService.getPermissionAlias(permString)) {
            if (implies(requiredPermission, p)) {
                return true
            }
        }
        // Create a real permission instance from the database permission.
        def perm = shiroPermissionResolver.resolvePermission(permString)
        // Now check whether this permission implies the required one.
        return perm.implies(requiredPermission)
    }

    def isPermitted(userName, requiredPermission) {
        def tenant = TenantUtils.getTenant()
        // Does the user have the given permission directly associated with himself?
        def permissions = CrmUserPermission.withCriteria {
            projections {
                property('permissionsString')
            }
            user {
                eq("username", userName)
                eq("status", CrmUser.STATUS_ACTIVE)
            }
            eq('tenantId', tenant)
            cache true
        }
        // Try each of the permissions found and see whether any of
        // them confer the required permission.
        def retval = permissions.find { implies(requiredPermission, it) }

        if (retval != null) {
            if (log.isDebugEnabled()) {
                log.debug "$userName@$tenant is permitted $requiredPermission by user permission [$retval]"
            }
            return true // Found a matching permission!
        }

        // If not, does he gain it through a role?
        // Get the permissions from the roles that the user does have.
        //
        def results = CrmUserRole.withCriteria {
            user {
                eq("username", userName)
                eq("status", CrmUser.STATUS_ACTIVE)
            }
            role {
                isNotEmpty("permissions")
                eq('tenantId', tenant)
            }
            cache true
        }.collect { it.role.permissions }.flatten()

        // There may be some duplicate entries in the results, but
        // at this stage it is not worth trying to remove them. Now,
        // create a real permission from each result and check it
        // against the required one.
        retval = results.find { implies(requiredPermission, it) }

        if (retval != null) {
            if (log.isDebugEnabled()) {
                log.debug "$userName@$tenant is permitted $requiredPermission by role permission [$retval]"
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug "$userName@$tenant is NOT permitted $requiredPermission"
            }
        }
        return (retval != null)
    }
}

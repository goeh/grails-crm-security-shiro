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

import org.apache.shiro.authc.AccountException
import org.apache.shiro.authc.IncorrectCredentialsException
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.util.SimpleByteSource

import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.security.shiro.ShiroCrmUserRole
import grails.plugins.crm.security.shiro.ShiroCrmUserPermission
import grails.plugins.crm.security.shiro.ShiroCrmUser

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import javax.servlet.http.HttpSession

class ShiroDbRealm {
    static authTokenClass = org.apache.shiro.authc.UsernamePasswordToken

    def credentialMatcher
    def shiroPermissionResolver
    def shiroCrmSecurityService

    boolean supports() {
        return true
    }

    def authenticate(authToken) {
        log.debug "Attempting to authenticate ${authToken.username} in DB realm..."
        def username = authToken.username

        // Null username is invalid
        if (username == null) {
            throw new AccountException("Null usernames are not allowed by this realm.")
        }

        // Get the user with the given username. If the user is not
        // found, then they don't have an account and we throw an
        // exception.
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new UnknownAccountException("No DB account found for user [${username}]")
        }
        if (!user.enabled) {
            throw new UnknownAccountException("Account is disabled [${username}]")
        }
        log.info "Found user '${user.username}' in DB"

        // Now check the user's password against the hashed value stored
        // in the database.
        def salt
        if (user.passwordSalt) {
            salt = user.passwordSalt.decodeBase64()
        } else {
            salt = username.bytes
        }
        def account = new SimpleAccount(username, user.passwordHash, new SimpleByteSource(salt), "ShiroDbRealm")
        if (!credentialMatcher.doCredentialsMatch(authToken, account)) {
            user.loginFailures = user.loginFailures + 1
            if (user.loginFailures > 9) {
                log.info "To many login failures, disabling account $user"
                user.enabled = false
            }
            user.save(flush: true)
            if (!user.enabled) {
                throw new UnknownAccountException("Account is disabled [${username}]")
            }

            log.info "Invalid password (DB realm)"
            throw new IncorrectCredentialsException("Invalid password for user '${username}'")
        }
        if (user.loginFailures > 0) {
            log.info "Login successful, resetting login failures for $user"
            user.loginFailures = 0
        }
        if (!user.passwordSalt) {
            log.info "Upgrading security for user ${user.username}.."
            salt = shiroCrmSecurityService.generateSalt()
            user.passwordHash = shiroCrmSecurityService.hashPassword(new String(authToken.password), salt)
            user.passwordSalt = salt.encodeBase64().toString()
        }

        try {
            setDefaultTenant(username)
        } catch(Exception e) {
            log.error("Failed to set default tenant for user [$username]", e)
        }
        return account
    }

    void setDefaultTenant(String username) {
        def user = shiroCrmSecurityService.getUser(username)
        def tenant = user?.defaultTenant
        if (tenant == null) {
            def availableTenants = shiroCrmSecurityService.getTenants(username)
            if (!availableTenants.isEmpty()) {
                tenant = availableTenants.get(0).id
            }
        }
        if (tenant != null) {
            TenantUtils.tenant = tenant
            def session = httpSession
            if(session) {
                session.tenant = tenant
            }
        }
    }

    HttpSession getHttpSession() {
        def s
        try {
            s = RequestContextHolder.currentRequestAttributes().getSession(false)
        } catch(Exception e) {
            log.error(e)
        }
        return s
    }

    def hasRole(principal, roleName) {
        def tenant = TenantUtils.getTenant()
        def roles = ShiroCrmUserRole.withCriteria {
            projections {
                property("id")
            }
            user {
                eq("username", principal)
                eq("enabled", true)
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
        def result = ShiroCrmUserRole.withCriteria {
            projections {
                property("id")
            }
            user {
                eq("username", principal)
                eq("enabled", true)
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
        def namedPermissions = shiroCrmSecurityService.getNamedPermission(permString)
        if(namedPermissions) {
            for(p in namedPermissions) {
                if(implies(requiredPermission, p)) {
                    return true
                }
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
        def permissions = ShiroCrmUserPermission.withCriteria {
            projections {
                property('permissionsString')
            }
            user {
                eq("username", userName)
                eq("enabled", true)
            }
            eq('tenantId', tenant)
            cache true
        }
        // Try each of the permissions found and see whether any of
        // them confer the required permission.
        def retval = permissions.find { implies(requiredPermission, it) }

        if (retval != null) {
            log.debug "$userName@$tenant is permitted $requiredPermission by user permission [$retval]"
            return true // Found a matching permission!
        }

        // If not, does he gain it through a role?
        // Get the permissions from the roles that the user does have.
        //
        def results = ShiroCrmUserRole.withCriteria {
            user {
                eq("username", userName)
                eq("enabled", true)
            }
            role {
                isNotEmpty("permissions")
                eq('tenantId', tenant)
            }
            cache true
        }.collect {it.role.permissions}.flatten()

        // There may be some duplicate entries in the results, but
        // at this stage it is not worth trying to remove them. Now,
        // create a real permission from each result and check it
        // against the required one.
        retval = results.find { implies(requiredPermission, it) }

        if (retval != null) {
            log.debug "$userName@$tenant is permitted $requiredPermission by role permission [$retval]"
        } else {
            log.debug "$userName@$tenant is NOT permitted $requiredPermission"
        }
        return (retval != null)
    }
}

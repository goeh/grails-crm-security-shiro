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

import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.SecurityUtils
import grails.plugins.crm.core.TenantUtils

class ShiroCrmSecurityService {

    static transactional = true

    def crmSecurityService
    def credentialMatcher

    def hashPassword(String password, byte[] salt) {
        new Sha512Hash(password, salt, credentialMatcher.hashIterations ?: 1).toHex()
    }

    byte[] generateSalt() {
        new SecureRandomNumberGenerator().nextBytes().getBytes()
    }

    // Protected
    def createUser(Map props) {
        if (ShiroCrmUser.findByUsername(props.username)) {
            throw new IllegalArgumentException("user [${props.username}] already exists")
        }
        def user = new ShiroCrmUser(props)
        def salt = generateSalt()
        user.passwordHash = hashPassword(props.password, salt)
        user.passwordSalt = salt.encodeBase64().toString()
        user.save(failOnError: true, flush: true)
        return user.dao
    }

    boolean isValidTenant(Long id, String username) {
        def tenant = ShiroCrmTenant.get(id)
        return tenant != null && tenant.owner.username == username // TODO Also check other permissions
    }

    // Protected
    def createTenant(String tenantName) {
        if (!tenantName) {
            throw new IllegalArgumentException("tenantName is null")
        }
        def username = SecurityUtils.subject.principal?.toString()
        if (!username) {
            throw new IllegalArgumentException("not authenticated")
        }
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        def tenant = ShiroCrmTenant.findByOwnerAndName(user, tenantName)?.dao
        if (!tenant) {
            tenant = new ShiroCrmTenant(owner: user, name: tenantName).save(failOnError: true, flush: true)?.dao
        }
        return tenant
    }

    // Protected
    def tenantInfo(Long id) {
        def tenant = ShiroCrmTenant.get(id)
        if (!tenant) {
            throw new IllegalArgumentException("Tenant not found: $id")
        }
        return tenant.dao
    }

    void deleteTenant(Long id) {
        def tenant = ShiroCrmTenant.get(id)
        if (!tenant) {
            throw new IllegalArgumentException("Tenant not found: $id")
        }

        tenant.delete(flush:true)

        if (id == TenantUtils.tenant) {
            // Removed the current tenant
            TenantUtils.setTenant(0L)
        }
    }

    Collection<Long> getAllTenants(String username = null) {
        if (!username) {
            username = SecurityUtils.subject.principal.toString()
        }
        def result = new HashSet<Long>()
        def user = ShiroCrmUser.findByUsername(username)
        if (user) {
            // Owned tenants
            def tmp = user.accounts*.id
            if (tmp) {
                result.addAll(tmp)
            }

            // Role tenants
            tmp = user.roles*.role.tenantId
            if (tmp) {
                result.addAll(tmp)
            }

            // Permission tenants
            tmp = user.permissions*.tenantId
            if (tmp) {
                result.addAll(tmp)
            }
        }
        return result.asList()
    }

    def createRole(String rolename, List<String> permissions = []) {
        def tenant = TenantUtils.getTenant()
        def role = ShiroCrmRole.findByNameAndTenantId(rolename, tenant)
        if(role) {
            throw new IllegalArgumentException("Role [$rolename] already exists")
        }
        role = new ShiroCrmRole(name:rolename, tenantId:tenant)
        for(perm in permissions) {
            role.addToPermissions(perm)
        }
        role.save(failOnError:true, flush:true)
    }

    def addUserRole(String username, String rolename) {
        def tenant = TenantUtils.getTenant()
        def role = ShiroCrmRole.findByNameAndTenantId(rolename, tenant)
        if(!role) {
            throw new IllegalArgumentException("role [$rolename] not found")
        }
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        def userrole = ShiroCrmUserRole.findByUserAndRole(user, role)
        if(!userrole) {
            userrole = new ShiroCrmUserRole(user:user, role:role).save(failOnError:true, flush:true)
        }
        return userrole
    }

    def addUserPermission(String username, String permission) {
        def tenant = TenantUtils.getTenant()
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        def perm = ShiroCrmUserPermission.findByPermissionsStringAndTenantId(permission, tenant)
        if(!perm) {
            perm = new ShiroCrmUserPermission(tenantId:tenant, user:user, permissionsString: permission).save(failOnError:true, flush:true)
        }
        return perm
    }

}

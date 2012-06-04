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
import grails.plugins.crm.core.CrmException
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable

class ShiroCrmSecurityService {

    static transactional = true

    def grailsApplication
    def crmSecurityService

    /**
     * Create new user.
     *
     * @param props user domain properties
     *
     * @return ShiroCrmUser instance
     */
    ShiroCrmUser createUser(Map props) {
        if (ShiroCrmUser.findByUsername(props.username)) {
            throw new CrmException("createUser.user.exists.error", [props.username])
        }
        def safeProps = props.findAll {ShiroCrmUser.BIND_WHITELIST.contains(it.key)}
        def user = new ShiroCrmUser(safeProps)
        def salt = crmSecurityService.generateSalt()
        user.passwordHash = crmSecurityService.hashPassword(props.password, salt)
        user.passwordSalt = salt.encodeBase64().toString()

        user.save(failOnError: true, flush: true)

        // Use Spring Events plugin to broadcast that a new user was created.
        publishEvent(new UserCreatedEvent(user))

        return user
    }

    /**
     * Update user information.
     *
     * @param props user domain properties
     *
     * @return ShiroCrmUser instance
     */
    ShiroCrmUser updateUser(Map props) {
        def user = ShiroCrmUser.findByUsername(props.username)
        if (!user) {
            throw new CrmException("updateUser.not.found.message", [props.username])
        }

        // Arghhh why is not bindData available to services??????!!!!!!
        def safeProps = props.findAll {ShiroCrmUser.BIND_WHITELIST.contains(it.key)}
        safeProps.each {key, value ->
            user[key] = value
        }

        if (props.password) {
            def salt = crmSecurityService.generateSalt()
            user.passwordHash = crmSecurityService.hashPassword(props.password, salt)
            user.passwordSalt = salt.encodeBase64().toString()
        }

        user.save(failOnError: true, flush: true)

        // Use Spring Events plugin to broadcast that a user was updated.
        publishEvent(new UserUpdatedEvent(user))

        return user
    }

    /**
     * Get user information for a user
     * @param username username or null for current user
     * @return user properties (DAO)
     */
    def getUserInfo(String username = null) {
        getUser(username)?.dao
    }

    /**
     * Return the user domain instance for the current user.
     *
     * @param username (optional) username or null for current user
     * @return ShiroCrmUser instance
     */
    ShiroCrmUser getUser(String username = null) {
        if (!username) {
            username = crmSecurityService.currentUser?.username
        }
        username ? ShiroCrmUser.findByUsername(username) : null
    }

    /**
     * Create new tenant.
     *
     * @param tenantName name of tenant
     * @param tenantType type of tenant
     * @param parent parent tenant id
     * @return ShiroCrmTenant instance
     */
    ShiroCrmTenant createTenant(String tenantName, String tenantType, Long parent = null) {
        if (!tenantName) {
            throw new IllegalArgumentException("tenantName is null")
        }
        if (!tenantType) {
            throw new IllegalArgumentException("tenantType is null")
        }
        def user = getUser()
        if (!user) {
            throw new IllegalArgumentException("not authenticated")
        }
        def tenant = ShiroCrmTenant.findByUserAndName(user, tenantName)
        if (tenant) {
            throw new IllegalArgumentException("Tenant [$tenantName] already exists")
        }

        def parentTenant
        if (parent) {
            parentTenant = ShiroCrmTenant.get(parent)
            if (!parentTenant) {
                throw new IllegalArgumentException("Parent tenant [$parent] does not exist")
            }
        }

        user.discard()
        user = ShiroCrmUser.lock(user.id)
        user.addToAccounts(tenant = new ShiroCrmTenant(name: tenantName, type: tenantType, parent: parentTenant))
        user.save(flush: true)

        // Use Spring Events plugin to broadcast that a new tenant was created.
        // Receivers could for example assign default roles and permissions for this tenant.
        publishEvent(new TenantCreatedEvent(tenant))


        return tenant
    }

    /**
     * Get tenant information.
     * @param id tenant ID
     * @return tenant properties (DAO)
     */
    def tenantInfo(Long id) {
        def tenant = ShiroCrmTenant.get(id)
        if (!tenant) {
            throw new IllegalArgumentException("Tenant not found: $id")
        }
        return tenant.dao
    }

    /**
     * Check that a user has any permission to access a tenant.
     * This method looks for roles and permissions in the specified tenant.
     *
     * @param id tenant ID
     * @param username username to check
     * @return true if the user has a role or permission in the given tenant
     */
    boolean isValidTenant(Long id, String username) {

        // Owns this tenant
        def tenant = ShiroCrmTenant.get(id)
        if (tenant != null && tenant.user.username == username) {
            return true
        }

        // Have role in this tenant?
        if (ShiroCrmUserRole.createCriteria().count {
            user {
                eq('username', username)
            }
            role {
                eq('tenantId', id)
            }
            cache true
        } > 0) {
            return true
        }

        // Have permission in this tenant?
        if (ShiroCrmUserPermission.createCriteria().count {
            user {
                eq('username', username)
            }
            eq('tenantId', id)
            cache true
        } > 0) {
            return true
        }
        return false
    }

    /**
     * Set tenant name.
     *
     * @param id tenant ID
     * @param name tenant name
     * @return tenant properties (DAO)
     */
    def setTenantName(Long id, String name) {
        def tenant = ShiroCrmTenant.lock(id)
        if (!tenant) {
            throw new IllegalArgumentException("Tenant not found: $id")
        }

        tenant.name = name

        tenant.save(failOnError: true)

        tenant.dao
    }

    /**
     * Delete a tenant.
     * Checks will be performed to see if someone uses this tenant.
     *
     * @param id id of the tenant to delete
     * @throws CrmException if tenant is in use
     */
    void deleteTenant(Long id) {

        // Get tenant.
        def shiroCrmTenant = ShiroCrmTenant.get(id)
        if (!shiroCrmTenant) {
            throw new CrmException('shiroCrmTenant.not.found.message', ['Account', id])
        }

        // Make sure the tenant is owned by current user.
        def currentUser = getUser()
        if (shiroCrmTenant.user.id != currentUser?.id) {
            throw new CrmException('shiroCrmTenant.permission.denied', ['Account', shiroCrmTenant.name])
        }

        // Make sure it's not the active tenant being deleted.
        if (TenantUtils.tenant == shiroCrmTenant.id) {
            throw new CrmException('shiroCrmTenant.delete.current.message', ['Account', shiroCrmTenant.name])
        }

        // Make sure it's not the default tenant being deleted.
        if (currentUser.defaultTenant == shiroCrmTenant.id) {
            throw new CrmException('shiroCrmTenant.delete.start.message', ['Account', shiroCrmTenant.name])
        }

        // Make sure we don't delete a tenant that is the default tenant for other users.
        def others = ShiroCrmUser.findAllByDefaultTenant(shiroCrmTenant.id)
        if (others) {
            throw new CrmException('shiroCrmTenant.delete.others.message', ['Account', shiroCrmTenant.name, others.join(', ')])
        }

        // Make sure we don't delete a tenant that is in use by other users (via roles)
        def affectedRoles = ShiroCrmUserRole.createCriteria().list() {
            role {
                eq('tenantId', id)
            }
        }
        def otherPeopleAffected = affectedRoles.findAll {it.user.id != currentUser.id}.collect {it.user}
        if (otherPeopleAffected) {
            throw new CrmException('shiroCrmTenant.delete.others.message', ['Account', shiroCrmTenant.name, otherPeopleAffected.join(', ')])
        }

        // Make sure we don't delete a tenant that is in use by other users (via permissions)
        def affectedPermissions = ShiroCrmUserPermission.findAllByTenantId(id)
        otherPeopleAffected = affectedPermissions.findAll {it.user.id != currentUser.id}.collect {it.user}
        if (otherPeopleAffected) {
            throw new CrmException('shiroCrmTenant.delete.others.message', ['Account', shiroCrmTenant.name, otherPeopleAffected.join(', ')])
        }

        // Now we are ready to delete!
        currentUser.discard()
        currentUser = ShiroCrmUser.lock(currentUser.id)

        // Delete my roles.
        for (userrole in affectedRoles) {
            def role = userrole.role
            currentUser.removeFromRoles(userrole)
            userrole.delete()
            role.delete()
        }

        // Delete my permissions.
        for (perm in affectedPermissions) {
            currentUser.removeFromPermissions(perm)
            perm.delete()
        }

        // It's my account and nobody else uses it, lets nuke it!
        currentUser.removeFromAccounts(shiroCrmTenant)
        shiroCrmTenant.delete()

        currentUser.save(flush: true)

        // Use Spring Events plugin to broadcast that the tenant was deleted.
        // Receivers should remove any data associated with the tenant.
        publishEvent(new TenantDeletedEvent(shiroCrmTenant))
    }

    Collection<Long> getAllTenants(String username = null) {
        if (!username) {
            username = SecurityUtils.subject?.principal?.toString()
            if (!username) {
                throw new IllegalArgumentException("not authenticated")
            }
        }
        def result = new HashSet<Long>()
        def user = ShiroCrmUser.findByUsername(username)
        if (user) {
            // Owned tenants
            def tmp = ShiroCrmTenant.findAllByUser(user)*.id
            if (tmp) {
                result.addAll(tmp)
            }
            println "$username own tenants: ${tmp}"

            // Role tenants
            tmp = ShiroCrmUserRole.findAllByUser(user).collect {it.role.tenantId}
            if (tmp) {
                result.addAll(tmp)
            }
            println "$username role tenants: ${tmp}"
            // Permission tenants
            tmp = ShiroCrmUserPermission.findAllByUser(user)*.tenantId
            if (tmp) {
                result.addAll(tmp)
            }
            println "$username permission tenants: ${tmp}"
        }
        println "$username all tenants: $result"
        return result.asList()
    }

    Collection<Map> getUserTenants(String username = null) {
        if (!username) {
            username = SecurityUtils.subject?.principal?.toString()
            if (!username) {
                throw new IllegalArgumentException("not authenticated")
            }
        }
        def result
        try {
            def tenants = getAllTenants(username)
            def currentTenant = TenantUtils.tenant
            result = tenants.sort {it}.collect {
                def info = tenantInfo(it)
                def extra = [current: currentTenant == it, my: info.user.username == username]
                return info + extra
            }
        } catch (Exception e) {
            log.error("Failed to get tenants for user [$username]", e)
            result = []
        }
        return result
    }

    /**
     * Return the default tenant (ID) for a user.
     * @param username
     * @return tenant id
     */
    Long getDefaultTenant(String username = null) {
        getUser(username)?.defaultTenant
    }

    /**
     * Set the default tenant (ID) for a user.
     * @param username
     * @param tenant
     * @return
     */
    def setDefaultTenant(String username = null, Long tenant = null) {
        def user
        if (username) {
            user = getUser(username)
        } else {
            user = getUser()
            username = user.username
        }
        if (!tenant) {
            tenant = TenantUtils.getTenant()
        }
        if (tenant) {
            // Check that the user has permission to access this tenant.
            def availableTenants = getAllTenants(username)
            if (!availableTenants.contains(tenant)) {
                throw new IllegalArgumentException("Tenant [$tenant] is not a valid tenant for user [$username]")
            }
        }
        user.discard()
        user = ShiroCrmUser.lock(user.id)
        user.defaultTenant = tenant
        user.save(flush: true)
    }

    /**
     * Add a named permission to the system.
     * @param name name of permission
     * @param permissions List of Shiro Wildcard permission strings
     */
    @CacheEvict(value = 'permissions', key = '#name')
    void addNamedPermission(String name, Object permissions) {
        def perm = ShiroCrmNamedPermission.findByName(name)
        if (!perm) {
            perm = new ShiroCrmNamedPermission(name: name)
            if (!(permissions instanceof Collection)) {
                permissions = [permissions]
            }
            for (p in permissions) {
                perm.addToPermissions(p)
            }
            perm.save(failOnError: true, flush: true)
        }
    }

    @Cacheable('permissions')
    List<String> getNamedPermission(String name) {
        ShiroCrmNamedPermission.findByName(name, [cache: true])?.permissions?.toList() ?: []
    }

    /**
     * Create a new role in the current tenant with a set of named permissions.
     * @param rolename name of role
     * @param permissions list of permission names (ShiroCrmNamedPermission.name)
     * @return the created ShiroCrmRole
     */
    ShiroCrmRole createRole(String rolename, List<String> permissions = []) {
        def tenant = TenantUtils.getTenant()
        def role = ShiroCrmRole.findByNameAndTenantId(rolename, tenant)
        if (role) {
            throw new IllegalArgumentException("Role [$rolename] already exists")
        }
        role = new ShiroCrmRole(name: rolename, tenantId: tenant)
        for (perm in permissions) {
            role.addToPermissions(perm)
        }
        role.save(failOnError: true, flush: true)
    }

    ShiroCrmUserRole addUserRole(String username, String rolename, Date expires = null) {
        def tenant = TenantUtils.getTenant()
        def role = ShiroCrmRole.findByNameAndTenantId(rolename, tenant)
        if (!role) {
            throw new IllegalArgumentException("role [$rolename] not found")
        }
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        def userrole = ShiroCrmUserRole.findByUserAndRole(user, role)
        if (!userrole) {
            def expiryDate = expires != null ? new java.sql.Date(expires.time) : null
            user.discard()
            user = ShiroCrmUser.lock(user.id)
            user.addToRoles(userrole = new ShiroCrmUserRole(role: role, expires:expiryDate))
            user.save(flush: true)
        }
        return userrole
    }

    ShiroCrmUserPermission addUserPermission(String username, String permission) {
        def tenant = TenantUtils.getTenant()
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        def perm = ShiroCrmUserPermission.createCriteria().get() {
            eq('user', user)
            eq('tenantId', tenant)
            eq('permissionsString', permission)
        }
        if (!perm) {
            user.discard()
            user = ShiroCrmUser.lock(user.id)
            user.addToPermissions(perm = new ShiroCrmUserPermission(tenantId: tenant, permissionsString: permission))
            user.save(flush: true)
        }
        return perm
    }

    void setUserStatus(ShiroCrmUser user, boolean enabled) {
        if (user.enabled != enabled) {
            user.enabled = enabled
            user.save(failOnError: true, flush: true)

            publishEvent(new UserChangedStatusEvent(user))
        }
    }

    /**
     * Return a list of all permissions within a tenant.
     * @param tenant
     * @return list of ShiroCrmUserPermission and ShiroCrmUserRole instances
     */
    List getTenantPermissions(Long tenant = null) {
        if (tenant == null) {
            tenant = TenantUtils.getTenant()
        }

        def result = []
        def roles = ShiroCrmUserRole.createCriteria().list() {
            role {
                eq('tenantId', tenant)
            }
            order 'id', 'asc'
        }
        if (roles) {
            result.addAll(roles)
        }

        def permissions = ShiroCrmUserPermission.createCriteria().list() {
            eq('tenantId', tenant)
            order 'id', 'asc'
        }
        if (permissions) {
            result.addAll(permissions)
        }

        return result
    }

}

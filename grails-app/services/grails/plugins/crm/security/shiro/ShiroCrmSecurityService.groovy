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
import grails.plugins.crm.core.CrmException
import grails.plugins.crm.core.TenantUtils
import grails.plugins.crm.core.DateUtils
import grails.plugins.crm.core.CrmSecurityService
import grails.plugin.cache.CacheEvict
import grails.plugin.cache.Cacheable
import org.apache.shiro.authz.UnauthorizedException
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import grails.events.Listener
import org.grails.plugin.platform.events.EventMessage

class ShiroCrmSecurityService implements CrmSecurityService {

    static transactional = true

    def grailsApplication
    def shiroSecurityManager
    def credentialMatcher
    def crmFeatureService

    /**
     * Checks if the current user is authenticated in this session.
     *
     * @return
     */
    boolean isAuthenticated() {
        SecurityUtils.subject?.isAuthenticated()
    }

    /**
     * Checks if the current user has permission to perform an operation.
     *
     * @param permission wildcard permission
     * @return
     */
    boolean isPermitted(permission) {
        SecurityUtils.subject?.isPermitted(permission.toString())
    }

    /**
     * Execute a piece of code as a specific user.
     *
     * @param username username
     * @param closure the work to perform
     * @return whatever the closure returns
     */
    def runAs(String username, Closure closure) {
        def user = ShiroCrmUser.findByUsernameAndEnabled(username, true, [cache: true])
        if (!user) {
            throw new UnauthorizedException("[$username] is not a valid user")
        }
        def realm = shiroSecurityManager.realms.find {it}
        def bootstrapSecurityManager = new DefaultSecurityManager(realm)
        def principals = new SimplePrincipalCollection(username, realm.name)
        def subject = new Subject.Builder(bootstrapSecurityManager).principals(principals).buildSubject()
        subject.execute(closure)
    }

    /**
     * Create a new user.
     *
     * @param properties user domain properties.
     * @return info about newly created user (DAO)
     */
    Map<String, Object> createUser(Map<String, Object> props) {
        if (ShiroCrmUser.findByUsername(props.username, [cache: true])) {
            throw new CrmException("user.exists.message", [props.username])
        }
        def safeProps = props.findAll {ShiroCrmUser.BIND_WHITELIST.contains(it.key)}
        def user = new ShiroCrmUser(safeProps)
        if (props.password) {
            def salt = generateSalt()
            user.passwordHash = hashPassword(props.password, salt)
            user.passwordSalt = salt.encodeBase64().toString()
        }

        user.save(failOnError: true, flush: true)
        def userInfo = user.dao
        event(for: "crm", topic: "userCreated", data: userInfo)
        return userInfo
    }

    /**
     * Update an existing user.
     *
     * @param username username
     * @param properties key/value pairs to update
     * @return user information after update
     */
    Map<String, Object> updateUser(String username, Map<String, Object> props) {
        if (!username) {
            username = SecurityUtils.subject?.principal?.toString()
            if (!username) {
                throw new UnauthorizedException("not authenticated")
            }
        }
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new CrmException("user.not.found.message", [username])
        }

        // Arghhh why is not bindData available to services??????!!!!!!
        def safeProps = props.findAll {ShiroCrmUser.BIND_WHITELIST.contains(it.key)}
        safeProps.each {key, value ->
            user[key] = value
        }

        if (props.password) {
            def salt = generateSalt()
            user.passwordHash = hashPassword(props.password, salt)
            user.passwordSalt = salt.encodeBase64().toString()
        }

        user.save(failOnError: true, flush: true)
        def userInfo = user.dao
        // Use Spring Events plugin to broadcast that a user was updated.
        event(for: "crm", topic: "userUpdated", data: userInfo)

        return userInfo
    }

    /**
     * Get the current user information.
     *
     * @return a Map with user properties (username, name, email, ...)
     */
    Map<String, Object> getCurrentUser() {
        def username = SecurityUtils.subject?.principal
        return username ? getUserInfo(username.toString()) : null
    }

    /**
     * Get user information for a user.
     *
     * @return a Map with user properties (username, name, email, ...)
     */
    Map<String, Object> getUserInfo(String username) {
        ShiroCrmUser.findByUsername(username, [cache: true])?.dao
    }

    /**
     * Delete a user.
     *
     * @param username username
     * @return true if the user was deleted
     */
    boolean deleteUser(String username) {
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new CrmException("user.not.found.message", [username])
        }
        def userInfo = user.dao
        user.delete(flush: true)
        event(for: "crm", topic: "userDeleted", data: userInfo)
        return true
    }

    /**
     * Create new tenant.
     *
     * @param tenantName name of tenant
     * @param parent optional parent tenant
     * @param owner username of tenant owner
     * @param locale default locale for this tenant
     * @return info about newly created tenant (DAO)
     */
    Map<String, Object> createTenant(String tenantName, Long parent = null, String owner = null, Locale locale = null) {
        if (!tenantName) {
            throw new IllegalArgumentException("tenantName is null")
        }
        if (!owner) {
            owner = SecurityUtils.subject.principal?.toString()
            if (!owner) {
                throw new UnauthorizedException("not authenticated")
            }
        }
        def user = ShiroCrmUser.findByUsername(owner, [cache: true])
        if (!user) {
            throw new CrmException("user.not.found.message", [owner])
        }
        def existing = ShiroCrmTenant.findByUserAndName(user, tenantName, [cache: true])
        if (existing) {
            throw new IllegalArgumentException("Tenant [$tenantName] already exists")
        }
        def parentTenant
        if (parent) {
            parentTenant = ShiroCrmTenant.load(parent)
            if (!parentTenant) {
                throw new IllegalArgumentException("Parent tenant [$parent] does not exist")
            }
        }

        // Create new tenant.
        def tenant = new ShiroCrmTenant(user: user, name: tenantName, parent: parentTenant, locale: locale ? locale.toString() : null).save(failOnError: true, flush: true)

        addRole(user, 'admin', tenant.id)

        crmFeatureService.enableFeature('security', tenant.id)
        //setupFeaturePermissions('default', null, tenant.id)

        def tenantInfo = tenant.dao
        event(for: "crm", topic: "tenantCreated", data: tenantInfo)
        return tenantInfo
    }

    private void addRole(ShiroCrmUser user, String roleName, Long tenant = TenantUtils.tenant) {
        def role = ShiroCrmRole.findByNameAndTenantId(roleName, tenant)
        if (!role) {
            role = new ShiroCrmRole(name: roleName, param: roleName, tenantId: tenant)
        }
        role.save(failOnError: true, flush: true)
        new ShiroCrmUserRole(user: user, role: role).save(failOnError: true, flush: true)
    }

    @Listener(namespace = "*", topic = "enableFeature")
    def enableFeature(EventMessage msg) {
        def event = msg.data // [feature: feature, tenant: tenant, role:role, expires:expires]
        def feature = crmFeatureService.getApplicationFeature(event.feature)
        if(feature) {
            def securityConfig = grailsApplication.config.crm.security
            def permissions = securityConfig[feature]?.permission ?: feature.permissions
            def role = event.role
            if (permissions && role) {
                permissions = [(role): permissions[role]]
            }
            if(permissions) {
                setupFeaturePermissions(feature.name, permissions, event.tenant)
            }
        }
    }

    void setupFeaturePermissions(String feature, Map<String, List> permissionMap, Long tenant = TenantUtils.tenant) {
        permissionMap.each {roleName, permissions ->
            def role = ShiroCrmRole.findByNameAndTenantId(roleName, tenant)
            if (!role) {
                role = new ShiroCrmRole(name: roleName, param: roleName, tenantId: tenant)
            }
            def namedPermission = "${feature}.$roleName".toString()
            addNamedPermission(namedPermission, permissions)
            if (!role.permissions?.contains(namedPermission)) {
                role.addToPermissions(namedPermission)
                log.debug("Permission [$namedPermission] added to tenant [$tenant]")
            }
            role.save(failOnError: true, flush: true)
        }
    }

    /**
     * Update tenant properties.
     *
     * @param tenantId id of tenant to update
     * @param properties key/value pairs to update
     * @return tenant information after update
     */
    Map<String, Object> updateTenant(Long tenantId, Map<String, Object> properties) {
        def shiroCrmTenant = ShiroCrmTenant.get(tenantId)
        if (!shiroCrmTenant) {
            throw new CrmException('tenant.not.found.message', ['Tenant', tenantId])
        }
        if (properties.name) {
            shiroCrmTenant.name = properties.name
        }
        def expires = properties.expires
        if (expires) {
            if (!(expires instanceof Date)) {
                expires = DateUtils.parseSqlDate(expires.toString())
            }
            shiroCrmTenant.expires = expires
        }
        def parent = properties.parent
        if (parent) {
            if (parent instanceof Number) {
                parent = ShiroCrmTenant.load(parent)
            }
            shiroCrmTenant.parent = parent
        }
        properties.options.each {key, value ->
            shiroCrmTenant.setOption(key, value)
        }
        shiroCrmTenant.save(flush: true)?.dao
    }

    /**
     * Get the current executing tenant.
     *
     * @return a Map with tenant properties (id, name, type, ...)
     */
    Map<String, Object> getCurrentTenant() {
        def tenant = TenantUtils.tenant
        return tenant ? getTenantInfo(tenant) : null
    }

    /**
     * Get tenant information.
     * @param id tenant id or null for current tenant
     * @return a Map with tenant properties (id, name, type, ...)
     */
    Map<String, Object> getTenantInfo(Long id = TenantUtils.tenant) {
        ShiroCrmTenant.get(id)?.dao
    }

    /**
     * Get all tenants that a user has access to.
     *
     * @param username username
     * @return collection of tenant information
     */
    List<Map<String, Object>> getTenants(String username = null) {
        if (!username) {
            username = SecurityUtils.subject?.principal?.toString()
            if (!username) {
                throw new UnauthorizedException("not authenticated")
            }
        }
        def result = []
        try {
            def tenants = getAllTenants(username)
            if (tenants) {
                def currentTenant = TenantUtils.tenant
                result = ShiroCrmTenant.createCriteria().list() {
                    inList('id', tenants)
                }.collect {
                    def info = it.dao
                    def extra = [current: it.id == currentTenant, my: info.user.username == username]
                    return info + extra
                }
            }
        } catch (Exception e) {
            log.error("Failed to get tenants for user [$username]", e)
        }
        return result
    }

    /**
     * Check if current user can access the specified tenant.
     * @param username username
     * @param tenantId the tenant ID to check
     * @return true if user has access to the tenant (by it's roles, permissions or ownership)
     */
    boolean isValidTenant(Long tenantId, String username = null) {
        if (!username) {
            username = currentUser?.username
            if (!username) {
                throw new UnauthorizedException("not authenticated")
            }
        }
        def user = ShiroCrmUser.findByUsername(username, [cache: true])
        if (!user) {
            throw new CrmException("user.not.found.message", [username])
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

    /**
     * Delete a tenant and all information associated with the tenant.
     * Checks will be performed to see if someone uses this tenant.
     *
     * @param id tenant id
     * @return true if the tenant was deleted
     */
    boolean deleteTenant(Long id) {

        // Get tenant.
        def shiroCrmTenant = ShiroCrmTenant.get(id)
        if (!shiroCrmTenant) {
            throw new CrmException('tenant.not.found.message', ['Tenant', id])
        }

        // Make sure the tenant is owned by current user.
        def currentUser = getUser()
        if (shiroCrmTenant.user.id != currentUser?.id) {
            throw new CrmException('tenant.permission.denied', ['Tenant', shiroCrmTenant.name])
        }

        // Make sure it's not the active tenant being deleted.
        if (TenantUtils.tenant == shiroCrmTenant.id) {
            throw new CrmException('tenant.delete.current.message', ['Tenant', shiroCrmTenant.name])
        }

        // Make sure it's not the default tenant being deleted.
        if (currentUser.defaultTenant == shiroCrmTenant.id) {
            throw new CrmException('tenant.delete.start.message', ['Tenant', shiroCrmTenant.name])
        }

        // Make sure we don't delete a tenant that is the default tenant for other users.
        def others = ShiroCrmUser.findAllByDefaultTenant(shiroCrmTenant.id)
        if (others) {
            throw new CrmException('tenant.delete.others.message', ['Tenant', shiroCrmTenant.name, others.join(', ')])
        }

        // Make sure we don't delete a tenant that is in use by other users (via roles)
        def affectedRoles = ShiroCrmUserRole.createCriteria().list() {
            role {
                eq('tenantId', id)
            }
            cache true
        }
        def otherPeopleAffected = affectedRoles.findAll {it.user.id != currentUser.id}.collect {it.user}
        if (otherPeopleAffected) {
            throw new CrmException('shiroCrmTenant.delete.others.message', ['Tenant', shiroCrmTenant.name, otherPeopleAffected.join(', ')])
        }

        // Make sure we don't delete a tenant that is in use by other users (via permissions)
        def affectedPermissions = ShiroCrmUserPermission.findAllByTenantId(id)
        otherPeopleAffected = affectedPermissions.findAll {it.user.id != currentUser.id}.collect {it.user}
        if (otherPeopleAffected) {
            throw new CrmException('shiroCrmTenant.delete.others.message', ['Tenant', shiroCrmTenant.name, otherPeopleAffected.join(', ')])
        }

        // Now we are ready to delete!
        def tenantInfo = shiroCrmTenant.dao

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
        event(for: "crm", topic: "tenantDeleted", data: tenantInfo)

        return true
    }

    def hashPassword(String password, byte[] salt) {
        new Sha512Hash(password, salt, credentialMatcher.hashIterations ?: 1).toHex()
    }

    byte[] generateSalt() {
        new SecureRandomNumberGenerator().nextBytes().getBytes()
    }

    //======= END OF METHODS FROM grails.plugins.crm.core.SecurityServiceDelegate ========
    //

    /**
     * Return the user domain instance for the current user.
     *
     * @param username (optional) username or null for current user
     * @return ShiroCrmUser instance
     */
    ShiroCrmUser getUser(String username = null) {
        if (!username) {
            username = SecurityUtils.subject?.principal?.toString()
        }
        username ? ShiroCrmUser.findByUsername(username, [cache: true]) : null
    }

    private Set<Long> getAllTenants(String username = null) {
        if (!username) {
            username = SecurityUtils.subject?.principal?.toString()
            if (!username) {
                throw new UnauthorizedException("not authenticated")
            }
        }
        def result = new HashSet<Long>()
        def user = ShiroCrmUser.findByUsername(username, [cache: true])
        if (user) {
            // Owned tenants
            def tmp = ShiroCrmTenant.findAllByUser(user)*.id
            if (tmp) {
                result.addAll(tmp)
            }
            // Role tenants
            tmp = ShiroCrmUserRole.findAllByUser(user).collect {it.role.tenantId}
            if (tmp) {
                result.addAll(tmp)
            }
            // Permission tenants
            tmp = ShiroCrmUserPermission.findAllByUser(user)*.tenantId
            if (tmp) {
                result.addAll(tmp)
            }
        }
        return result
    }

    /**
     * Set the default tenant (ID) for a user.
     * @param username username or null for current user
     * @param tenant tenant id or null for current tenant
     * @return user information after updating user
     */
    Map<String, Object> setDefaultTenant(String username = null, Long tenant = null) {
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

        return user.dao
    }

    /**
     * Add a named permission to the system.
     * @param name name of permission
     * @param permissions List of Shiro Wildcard permission strings
     */
    @CacheEvict(value = 'permissions', key = '#name')
    ShiroCrmNamedPermission addNamedPermission(String name, Object permissions) {
        def perm = ShiroCrmNamedPermission.findByName(name, [cache: true])
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
        return perm
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
        def role = ShiroCrmRole.findByNameAndTenantId(rolename, tenant, [cache: true])
        if (role) {
            throw new IllegalArgumentException("Role [$rolename] already exists")
        }
        role = new ShiroCrmRole(name: rolename, tenantId: tenant)
        for (perm in permissions) {
            role.addToPermissions(perm)
        }
        role.save(failOnError: true, flush: true)
    }

    boolean hasRole(String rolename, Long tenant = TenantUtils.tenant) {
        ShiroCrmRole.findByNameAndTenantId(rolename, tenant, [cache: true]) != null
    }

    ShiroCrmUserRole addUserRole(String username, String rolename, Date expires = null) {
        def tenant = TenantUtils.getTenant()
        def role = ShiroCrmRole.findByNameAndTenantId(rolename, tenant, [cache: true])
        if (!role) {
            throw new IllegalArgumentException("role [$rolename] not found")
        }
        def user = ShiroCrmUser.findByUsername(username, [cache: true])
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        def userrole = ShiroCrmUserRole.findByUserAndRole(user, role, [cache: true])
        if (!userrole) {
            def expiryDate = expires != null ? new java.sql.Date(expires.time) : null
            user.discard()
            user = ShiroCrmUser.lock(user.id)
            user.addToRoles(userrole = new ShiroCrmUserRole(role: role, expires: expiryDate))
            user.save(flush: true)
        }
        return userrole
    }

    boolean addRolePermission(String rolename, String permission, Long tenant = null) {
        if (!tenant) {
            tenant = TenantUtils.getTenant()
        }
        def role = ShiroCrmRole.findByNameAndTenantId(rolename, tenant, [cache: true])
        if (!role) {
            throw new IllegalArgumentException("role [$rolename] not found in tenant [$tenant]")
        }
        if (role.permissions?.contains(permission)) {
            return false
        }
        role.addToPermissions(permission)
        return true
    }

    ShiroCrmUserPermission addUserPermission(String username, String permission) {
        def tenant = TenantUtils.getTenant()
        def user = ShiroCrmUser.findByUsername(username, [cache: true])
        if (!user) {
            throw new IllegalArgumentException("user [$username] not found")
        }
        def perm = ShiroCrmUserPermission.createCriteria().get() {
            eq('user', user)
            eq('tenantId', tenant)
            eq('permissionsString', permission)
            cache true
        }
        if (!perm) {
            user.discard()
            user = ShiroCrmUser.lock(user.id)
            user.addToPermissions(perm = new ShiroCrmUserPermission(tenantId: tenant, permissionsString: permission))
            user.save(flush: true)
        }
        return perm
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
            cache true
        }
        if (roles) {
            result.addAll(roles)
        }

        def permissions = ShiroCrmUserPermission.createCriteria().list() {
            eq('tenantId', tenant)
            order 'id', 'asc'
            cache true
        }
        if (permissions) {
            result.addAll(permissions)
        }

        return result
    }

    ShiroCrmRole updatePermissionsForRole(Long tenant = null, String rolename, List<String> permissions) {
        if (tenant == null) {
            tenant = TenantUtils.getTenant()
        }
        def role = ShiroCrmRole.findByNameAndTenantId(rolename, tenant, [cache: true])
        if (role) {
            role.permissions.clear()
        } else {
            role = new ShiroCrmRole(name: rolename, tenantId: tenant)
            log.warn("Created missing role [$rolename] for tenant [$tenant]")
        }
        for (perm in permissions) {
            role.addToPermissions(perm)
        }
        role.save(failOnError: true, flush: true)
    }

}

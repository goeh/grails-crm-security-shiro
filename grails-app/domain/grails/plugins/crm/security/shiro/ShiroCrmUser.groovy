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

import grails.plugins.crm.core.AuditEntity
import grails.plugins.crm.core.UuidEntity
import grails.plugins.crm.core.TenantUtils

/**
 * This domain class represents a user account.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
@AuditEntity
@UuidEntity
class ShiroCrmUser {

    String username
    String name
    String email
    String company
    String address1
    String address2
    String postalCode
    String city
    String countryCode
    String currency
    String telephone
    boolean enabled
    String passwordHash
    String passwordSalt
    int loginFailures
    Long defaultTenant

    static hasMany = [roles: ShiroCrmUserRole, permissions: ShiroCrmUserPermission, accounts: ShiroCrmTenant]

    static constraints = {
        username(size: 3..80, maxSize: 80, nullable: false, blank: false, unique: true)
        name(size: 3..80, maxSize: 80, nullable: false, blank: false)
        email(maxSize: 80, blank: false, email: true)
        company(maxSize: 80, nullable: true)
        address1(maxSize: 80, nullable: true)
        address2(maxSize: 80, nullable: true)
        postalCode(size: 2..20, maxSize: 20, nullable: true)
        city(size: 2..40, maxSize: 40, nullable: true)
        countryCode(size: 2..3, maxSize: 3, nullable: true)
        currency(maxSize: 4, nullable: true)
        telephone(size: 4..20, maxSize: 20, nullable: true)
        passwordHash(size: 25..255, blank: false)
        passwordSalt(maxSize: 255, blank: false)
        defaultTenant(nullable: true)
    }

    static mapping = {
        cache usage: 'read-write'
        sort "username"
        accounts(sort: 'name')
        //columns { enabled sqlType:"BOOLEAN" }
        roles(cascade: "all-delete-orphan")
        permissions(cascade: "all-delete-orphan")
    }

    static transients = ['dao']

    static searchable = {
        only = ['username', 'name']
    }

    static List BIND_WHITELIST = ['username', 'name', 'email', 'company', 'address1', 'address2', 'postalCode', 'city', 'countryCode', 'currency', 'telephone', 'enabled', 'defaultTenant']

    /**
     * Returns the username property.
     * @return username property
     */
    String toString() {
        username
    }

    /**
     * Clients should use this method to get user properties instead of accessing the domain instance directly.
     * The following properties are returned as a Map: [String guid, String username, String name, String email, String address1, String address2,
     * String postalCode, String city, String countryCode, String telephone, boolean enabled, boolean defaultTenant]
     * @return a data access object (Map) representing the domain instance.
     */
    def getDao() {
        def tenant = TenantUtils.tenant
        def allPerm = []
        if (permissions) {
            allPerm.addAll(permissions.findAll{it.tenantId == tenant}.collect {it.toString()})
        }
        def allRoles = []
        for (role in roles.findAll{it.role.tenantId == tenant}) {
            allRoles << role.toString()
            allPerm.addAll(role.role.permissions)
        }
        def map = properties.subMap(['guid', 'username', 'name', 'email', 'company', 'address1', 'address2', 'postalCode', 'city', 'countryCode', 'currency', 'telephone', 'enabled', 'defaultTenant'])
        map.roles = allRoles
        map.permissions = allPerm
        return map
    }
}

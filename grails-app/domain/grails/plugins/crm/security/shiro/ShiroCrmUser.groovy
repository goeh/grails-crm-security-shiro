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

@AuditEntity
class ShiroCrmUser {

    String username
    String name
    String email
    String address1
    String address2
    String postalCode
    String city
    String countryCode
    String telephone
    boolean enabled
    String passwordHash
    String passwordSalt
    int loginFailures

    static hasMany = [roles: ShiroCrmUserRole, permissions: ShiroCrmUserPermission, accounts: ShiroCrmTenant]

    static constraints = {
        username(size: 3..80, maxSize: 80, nullable: false, blank: false, unique: true)
        name(size: 3..80, maxSize: 80, nullable: false, blank: false)
        email(maxSize: 80, nullable: true, blank: false, email: true)
        address1(maxSize: 80, nullable: true)
        address2(maxSize: 80, nullable: true)
        postalCode(size: 2..20, maxSize: 20, nullable: true)
        city(size: 2..40, maxSize: 40, nullable: true)
        countryCode(size: 2..3, maxSize: 3, nullable: true)
        telephone(size: 4..20, maxSize: 20, nullable: true)
        passwordHash(size: 25..255, blank: false)
        passwordSalt(maxSize: 255, blank: false)
    }

    static mapping = {
        cache usage: 'nonstrict-read-write'
        sort "username"
        accounts(sort:'name')
        //columns { enabled sqlType:"BOOLEAN" }
        roles(cascade: "all-delete-orphan")
        permissions(cascade: "all-delete-orphan")
    }

    static transients = ['dao']

    static searchable = {
        only = ['username', 'name']
    }

    String toString() {
        username
    }

    def getDao() {
        properties.subMap(['username', 'name','email','address1','address2','postalCode','city','countryCode','telephone','enabled'])
    }
}

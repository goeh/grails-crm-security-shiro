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

import grails.plugins.crm.core.TenantEntity

/**
 * This domain class represents an account role.
 * Users can have multiple roles within an account.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
@TenantEntity
class ShiroCrmRole {

    String name
    String param
    String description
    static hasMany = [permissions: String]

    static constraints = {
        name(nullable: false, blank: false, unique: 'tenantId')
        param(maxSize:20, nullable:true)
        description(maxSize:255, nullable:true)
    }
    static mapping = {
        sort 'name'
        cache usage:'nonstrict-read-write'
        permissions(cascade: "all-delete-orphan")
    }

    @Override
    String toString() {
        return name
    }
}

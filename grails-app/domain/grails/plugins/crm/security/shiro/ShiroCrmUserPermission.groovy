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
 * This domain class represents a user's permission within a specific account (tenant).
 * Normally users get permissions from it's roles (ShiroCrmRole.permissions) but users can also have individual permissions.
 * Individual permission are stored in this domain.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
@TenantEntity
class ShiroCrmUserPermission {

    String permissionsString

    static belongsTo = [user:ShiroCrmUser]

    static mapping = {
        cache 'nonstrict-read-write'
    }

    String toString() {
        permissionsString.toString()
    }
}

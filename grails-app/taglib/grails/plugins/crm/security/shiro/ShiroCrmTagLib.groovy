/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package grails.plugins.crm.security.shiro

/**
 * CRM Account related tag libraries.
 */
class ShiroCrmTagLib {

    static namespace = "crm"

    def permissionList = {attrs, body ->
        def permissions = attrs.permission ?: attrs.permissions
        if (!(permissions instanceof Collection)) {
            permissions = [permissions]
        }
        int i = 0
        for (p in permissions) {
            def map = [(attrs.var ?: 'it'): [label: message(code: p, default: p), permission: p]]
            if (attrs.status) {
                map[attrs.status] = i++
            }
            out << body(map)
        }
    }
}

/*
 * Copyright (c) 2012 Goran Ehrsson.
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

package grails.plugins.crm.security.shiro

import org.springframework.context.ApplicationEvent

/**
 * Event triggered manually from security admin user interface when permissions for a user in a tenant is screwed up and resetting is the only way to fix it.
 * Event source is a Map [tenant:<tenant-id>, username:<username>]
 */
class ResetPermissionsEvent extends ApplicationEvent {
    ResetPermissionsEvent(Map info) {
        super(info)
    }
}

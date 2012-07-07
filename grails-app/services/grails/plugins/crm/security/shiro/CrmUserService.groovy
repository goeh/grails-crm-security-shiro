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

import grails.plugins.crm.core.TenantUtils

/**
 * User administration service.
 */
class CrmUserService {

    static transactional = true

    def selectionService

    static String wildcard(String q) {
        q = q.toLowerCase()
        if (q.contains('*')) {
            return q.replace('*', '%')
        } else if (q[0] == '=') { // Exact match.
            return q[1..-1]
        } else { // Starts with is default.
            return q + '%'
        }
    }

    /**
     * Empty query = search all records.
     *
     * @param params pagination parameters
     * @return List of ShiroCrmUser domain instances
     */
    def list(Map params) {
        list([:], params)
    }

    /**
     * Find ShiroCrmUser instances filtered by query.
     *
     * @param query filter parameters
     * @param params pagination parameters
     * @return List of ShiroCrmUser domain instances
     */
    def list(Map query, Map params) {

        ShiroCrmUser.createCriteria().list(params) {
            if (query.username) {
                ilike('username', wildcard(query.username))
            }
            if (query.name) {
                ilike('name', wildcard(query.name))
            }
            if (query.email) {
                ilike('email', wildcard(query.email))
            }
        }
    }
}

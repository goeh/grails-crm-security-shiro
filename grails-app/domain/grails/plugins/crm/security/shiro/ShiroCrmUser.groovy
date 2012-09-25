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

/**
 * This domain class represents a user account.
 *
 * @author Goran Ehrsson
 * @since 0.1
 */
class ShiroCrmUser {

    String username
    String passwordHash
    String passwordSalt

    static constraints = {
        username(size: 3..80, maxSize: 80, nullable: false, blank: false, unique: true)
        passwordHash(size: 25..255, blank: false)
        passwordSalt(maxSize: 255, blank: false)
    }

    static mapping = {
        cache 'read-write'
    }

    /**
     * Returns the username property.
     * @return username property
     */
    String toString() {
        username
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        ShiroCrmUser that = (ShiroCrmUser) o

        if (username != that.username) return false

        return true
    }

    int hashCode() {
        return (username != null ? username.hashCode() : 0)
    }
}

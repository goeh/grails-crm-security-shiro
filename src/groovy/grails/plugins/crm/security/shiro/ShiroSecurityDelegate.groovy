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

import org.apache.shiro.SecurityUtils
import org.apache.shiro.crypto.SecureRandomNumberGenerator
import org.apache.shiro.crypto.hash.Sha512Hash
import org.apache.shiro.mgt.DefaultSecurityManager
import org.apache.shiro.subject.SimplePrincipalCollection
import org.apache.shiro.subject.Subject

import grails.plugins.crm.core.CrmSecurityDelegate

/**
 * Apache Shiro security delegate.
 *
 * @author Goran Ehrsson
 * @since 1.0
 */
class ShiroSecurityDelegate implements CrmSecurityDelegate {

    def shiroSecurityManager
    def credentialMatcher

    /**
     * Checks if the current user is authenticated in this session.
     * @return
     */
    boolean isAuthenticated() {
        SecurityUtils.subject?.isAuthenticated()
    }

    /**
     * Checks if the current user has permission to perform an operation.
     * @param permission
     * @return
     */
    boolean isPermitted(Object permission) {
        SecurityUtils.subject?.isPermitted(permission.toString())
    }

    /**
     * Execute a piece of code as a specific user.
     * @param username
     * @param closure
     * @return
     */
    def runAs(String username, Closure closure) {
        def realm = shiroSecurityManager.realms.find {it}
        def bootstrapSecurityManager = new DefaultSecurityManager(realm)
        def principals = new SimplePrincipalCollection(username, realm.name)
        def subject = new Subject.Builder(bootstrapSecurityManager).principals(principals).buildSubject()
        subject.execute(closure)
    }

    void createUser(String username, String password) {
        byte[] salt = generateSalt()
        new ShiroCrmUser(username: username, passwordHash: hashPassword(password, salt), passwordSalt: salt.encodeBase64().toString()).save(failOnError: true)
    }

    void setPassword(String username, String password) {
        def user = ShiroCrmUser.findByUsername(username)
        if (!user) {
            throw new IllegalArgumentException("ShiroCrmUser [$username] not found")
        }
        byte[] salt = generateSalt()
        user.passwordHash = hashPassword(password, salt)
        user.passwordSalt = salt.encodeBase64().toString()
        user.save(failOnError: true)
    }

    String getCurrentUser() {
        SecurityUtils.subject?.principal?.toString()
    }

    boolean deleteUser(String username) {
        def user = ShiroCrmUser.findByUsername(username)
        if (user) {
            user.delete()
            return true
        }
        return false
    }

    def hashPassword(String password, byte[] salt) {
        new Sha512Hash(password, salt, credentialMatcher.hashIterations ?: 1).toHex()
    }

    byte[] generateSalt() {
        new SecureRandomNumberGenerator().nextBytes().getBytes()
    }

}

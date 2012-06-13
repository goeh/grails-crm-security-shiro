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

/**
 * Delegate for the security-questions plugin that resets user password when forgotten.
 */
class ResetPasswordDelegate {

    def shiroCrmSecurityService

    def verifyAccount(Map params) {
        def user = ShiroCrmUser.findByUsername(params.username)
        if (user) {
            println "Verifying account ${user.username} - ${user.name} ---> $params"
            if(user.email.equalsIgnoreCase(params.email) && equalsIgnoreSpace(user.postalCode, params.postalCode)) {
                return user.username
            }
        } else {
            println "User [${params.username}] not found"
        }
        return null
    }

    private boolean equalsIgnoreSpace(String arg1, String arg2) {
        if(arg1 && arg2) {
            return arg1.replaceAll(/\s/, '').equalsIgnoreCase(arg2.replaceAll(/\s/, ''))
        }
        return false
    }

    def getQuestions(username, questions) {
        return questions
    }

    def resetPassword(String username, String password) {
        println "Changing password for [$username]"
        shiroCrmSecurityService.updateUser([username: username, password: password])
        return username
    }

    def disableAccount(String username) {
        println "Disabling account [$username]"
        def user = ShiroCrmUser.findByUsername(username)
        if (user) {
            user.enabled = false
            user.save()
        }
    }
}

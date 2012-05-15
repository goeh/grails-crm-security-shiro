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

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.web.util.SavedRequest
import org.apache.shiro.web.util.WebUtils
import grails.plugins.crm.core.TenantUtils
import javax.servlet.http.HttpServletResponse

class AuthController {

    def shiroSecurityManager
    def shiroCrmSecurityService
    def userSettingsService

    def index = { redirect(action: "login", params: params) }

    def login = {
        return [username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri]
    }

    def signIn = {
        def authToken = new UsernamePasswordToken(params.username, params.password)

        // Support for "remember me"
        if (params.rememberMe) {
            authToken.rememberMe = true
        }

        // If a controller redirected to this page, redirect back
        // to it. Otherwise redirect to the root URI.
        def targetUri = params.targetUri

        // Handle requests saved by Shiro filters.
        def savedRequest = WebUtils.getSavedRequest(request)
        if (savedRequest) {
            targetUri = savedRequest.requestURI - request.contextPath
            if (savedRequest.queryString) targetUri = targetUri + '?' + savedRequest.queryString
        }

        try {
            // Perform the actual login. An AuthenticationException
            // will be thrown if the username is unrecognised or the
            // password is incorrect.
            SecurityUtils.subject.login(authToken)

            request.session.tenant = TenantUtils.tenant

            if ((!targetUri) || targetUri == "/") {
                if (userSettingsService != null) {
                    def ctrl = userSettingsService.getValue(params.username, "startController")
                    if (ctrl) {
                        targetUri = g.createLink(controller: ctrl).toString() - request.contextPath
                    }
                }
                if(! targetUri) {
                    targetUri = "/"
                }
            }
            log.info "Tenant set to ${TenantUtils.tenant} at login"
            log.info "Redirecting to '${targetUri}'."
            redirect(uri: targetUri)
        }
        catch (AuthenticationException ex) {
            log.error(ex.message)
            // Authentication failed, so display the appropriate message
            // on the login page.
            log.info "Authentication failure for user '${params.username}'."
            flash.error = message(code: "login.failed")

            // Keep the username and "remember me" setting so that the
            // user doesn't have to enter them again.
            def m = [username: params.username]
            if (params.rememberMe) {
                m["rememberMe"] = true
            }

            // Remember the target URI too.
            if (params.targetUri) {
                m["targetUri"] = params.targetUri
            }

            // Now redirect back to the login page.
            redirect(action: "login", params: m)
        }
    }

    def logout = {
        // Log the user out of the application.
        SecurityUtils.subject?.logout()

        // Redirect back to the home page.
        redirect(uri: params.targetUri ?: "/")
    }

    def unauthorized = {
        if (request.xhr || request.contentType?.equalsIgnoreCase("text/xml")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        }
    }


    def password = {
    }
}

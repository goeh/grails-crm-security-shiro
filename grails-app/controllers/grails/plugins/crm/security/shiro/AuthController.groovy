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
import org.apache.shiro.web.util.WebUtils
import grails.plugins.crm.core.TenantUtils

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

class AuthController {

    static allowedMethods = [signIn: 'POST']

    def grailsApplication
    def crmSecurityService
    def crmThemeService

    def index() { redirect(action: "login", params: params) }

    def login() {
        return [username: params.username, rememberMe: (params.rememberMe != null), targetUri: params.targetUri]
    }

    def signIn(String username, String password) {
        def authToken = new UsernamePasswordToken((String) username, (String) password)

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
            def tenant = TenantUtils.tenant
            log.debug "Tenant set to ${tenant} for ${username} at login"

            if (grailsApplication.config.crm.theme.cookie.set) {
                def cookieName = grailsApplication.config.crm.theme.cookie.name
                if (cookieName) {
                    String theme = crmThemeService.getThemeName(tenant)
                    if (theme) {
                        def cookie = new Cookie(cookieName, theme)
                        cookie.setDomain(grailsApplication.config.crm.theme.cookie.domain ?: "localhost")
                        cookie.setPath(grailsApplication.config.crm.theme.cookie.path ?: "/")
                        cookie.setMaxAge(grailsApplication.config.crm.theme.cookie.age ?: (60 * 60 * 24 * 365)) // Store cookie for 1 year
                        response.addCookie(cookie)
                        log.debug "Theme set to ${theme} for ${username} at login"
                    }
                } else {
                    log.warn "Config parameter [crm.theme.cookie.set] is set but [crm.theme.cookie.name] is not"
                }
            }
            if (!targetUri) {
                targetUri = grailsApplication.config.crm.login.targetUri ?: '/'
            }
            log.debug "Redirecting ${username} to $targetUri"
            event(for: 'crm', topic: 'login',
                    data: [tenant: tenant, user: SecurityUtils.subject?.principal?.toString(), uri: targetUri])
            redirect(uri: targetUri)
        }
        catch (AuthenticationException ex) {
            log.error(ex.message)
            // Authentication failed, so display the appropriate message
            // on the login page.
            log.info "Authentication failure for user '${username}'."
            flash.error = message(code: "auth.login.failed")

            // Keep the username and "remember me" setting so that the
            // user doesn't have to enter them again.
            def m = [username: username]
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
        catch (Exception e) {
            log.error("Login failed", e)
            SecurityUtils.subject?.logout()
            // Redirect back to the home page.
            flash.error = e.message
            redirect(uri: params.targetUri ?: "/")
        }
    }

    def logout() {
        def tenant = TenantUtils.tenant
        def username = SecurityUtils.subject?.principal?.toString()

        // Log the user out of the application.
        SecurityUtils.subject?.logout()

        if (username) {
            // Use Spring Events plugin to broadcast that a user logged out.
            event(for: 'crm', topic: 'logout', data: [tenant: tenant, user: username])
        }

        // Redirect back to the home page.
        redirect(uri: params.targetUri ?: "/")
    }

    def unauthorized() {
        if (request.xhr || request.contentType?.equalsIgnoreCase("text/xml")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
        } else if (!crmSecurityService.isValidTenant(TenantUtils.tenant)) {
            def defaultTenant = crmSecurityService.currentUser?.defaultTenant
            if (!defaultTenant) {
                defaultTenant = crmSecurityService.getTenants()?.find { it }?.id
            }
            TenantUtils.setTenant(defaultTenant)
            request.session.tenant = defaultTenant
        }
    }
}

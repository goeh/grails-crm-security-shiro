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

import grails.plugins.crm.core.TenantUtils
import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.UsernamePasswordToken
import grails.plugins.crm.core.CrmException

class ShiroCrmSecurityServiceSpec extends grails.plugin.spock.IntegrationSpec {

    def crmSecurityService
    def shiroCrmSecurityService
    def grailsApplication

    def accountType

    def "shiro security delegate is injected"() {
        expect: crmSecurityService.crmSecurityDelegate.class == ShiroCrmSecurityDelegate.class
    }

    def "create user"() {
        when:
        def user = shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        then:
        user != null
        user instanceof ShiroCrmUser
    }

    def "duplicate username is not allowed"() {
        when:
        def user = shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        then:
        user != null

        when:
        user = shiroCrmSecurityService.createUser([username: "test", name: "Test User Duplicate", email: "info@technipelago.se", password: "test789", enabled: true])
        then:
        thrown(CrmException)
    }

    def "get user information"() {
        when:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        def info = shiroCrmSecurityService.getUserInfo("test")

        then:
        info instanceof Map
        info.username == "test"
        info.name == "Test User"
        info.email == "test@test.com"
        info.enabled
    }

    def "get user instance"() {
        when:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        def user = shiroCrmSecurityService.getUser("test")

        then:
        user instanceof ShiroCrmUser
        user.username == "test"
        user.name == "Test User"
        user.email == "test@test.com"
        user.enabled
    }

    def "create tenant"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test") {
            result = crmSecurityService.getTenants()
        }
        then:
        result.isEmpty()

        when:
        crmSecurityService.runAs("test") {
            shiroCrmSecurityService.createTenant("My First Tenant")
            shiroCrmSecurityService.createTenant("My Second Tenant", "test")
            result = crmSecurityService.getTenants()
        }
        then:
        result.size() == 2

    }

    def "runAs changes current user"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test") {
            result = crmSecurityService.getCurrentUser()
        }
        then:
        result != null
        result.username == "test"
    }

    def "runAs will not authenticate the user"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test") {
            result = crmSecurityService.isAuthenticated()
        }
        then:
        result == false
    }

    def "authenticate the user with login"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test") {
            SecurityUtils.subject.login(new UsernamePasswordToken("test", "test123"))
            result = crmSecurityService.isAuthenticated()
            SecurityUtils.subject.logout()
        }
        then:
        result == true
    }

    def "runAs with non-existing username should throw exception"() {
        def result
        // No user created here.
        when:
        crmSecurityService.runAs("test") {
            result = crmSecurityService.getCurrentUser()
        }
        then:
        thrown(org.apache.shiro.authz.UnauthorizedException)
    }

    def "runAs with disabled user should throw exception"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: false])

        when:
        crmSecurityService.runAs("test") {
            result = crmSecurityService.getCurrentUser()
        }
        then:
        thrown(org.apache.shiro.authz.UnauthorizedException)
    }

    def "wildcard permissions"() {

        def tenant
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])


        when:
        crmSecurityService.runAs("test") {
            tenant = shiroCrmSecurityService.createTenant("Test Tenant")
        }
        then:
        tenant != null

        when:
        TenantUtils.withTenant(tenant.id) {
            crmSecurityService.runAs("test") {
                SecurityUtils.subject.checkPermission("test:protected")
            }
        }
        then:
        thrown(org.apache.shiro.authz.UnauthorizedException)

        when:
        TenantUtils.withTenant(tenant.id) {
            crmSecurityService.runAs("test") {
                result = crmSecurityService.isPermitted("test:protected")
            }
        }
        then:
        result == false


        when:
        TenantUtils.withTenant(tenant.id) {
            shiroCrmSecurityService.addUserPermission("test", "test:*")
            crmSecurityService.runAs("test") {
                result = crmSecurityService.isPermitted("test:protected")
                SecurityUtils.subject.checkPermission("test:protected")
            }
        }
        then:
        result == true

    }

    def "permission check with invalid tenant"() {

        def tenant
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("test") {
            tenant = shiroCrmSecurityService.createTenant("Test Tenant")
            TenantUtils.withTenant(tenant.id) {
                shiroCrmSecurityService.addUserPermission("test", "test:*")
            }
        }
        then:
        tenant != null

        when:
        crmSecurityService.runAs("test") {
            TenantUtils.withTenant(tenant.id) {
                result = crmSecurityService.isPermitted("test:protected")
            }
        }
        then:
        result == true

        when:
        crmSecurityService.runAs("test") {
            TenantUtils.withTenant(42L) {
                result = crmSecurityService.isPermitted("test:protected")
            }
        }
        then:
        result == false
    }

}

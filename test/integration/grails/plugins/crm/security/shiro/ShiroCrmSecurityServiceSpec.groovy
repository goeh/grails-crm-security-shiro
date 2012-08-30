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

    def shiroCrmSecurityService
    def grailsApplication
    def crmFeatureService

    def "create user"() {
        when:
        def user = shiroCrmSecurityService.createUser([username: "test1", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        then:
        user != null
        user instanceof Map
    }

    def "duplicate username is not allowed"() {
        when:
        def user = shiroCrmSecurityService.createUser([username: "test2", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        then:
        user != null

        when:
        user = shiroCrmSecurityService.createUser([username: "test2", name: "Test User Duplicate", email: "info@technipelago.se", password: "test789", enabled: true])
        then:
        thrown(CrmException)
    }

    def "get user information"() {
        when:
        shiroCrmSecurityService.createUser([username: "test3", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        def info = shiroCrmSecurityService.getUserInfo("test3")

        then:
        info instanceof Map
        info.username == "test3"
        info.name == "Test User"
        info.email == "test@test.com"
        info.enabled
    }

    def "get user instance"() {
        when:
        shiroCrmSecurityService.createUser([username: "test4", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        def user = shiroCrmSecurityService.getUser("test4")

        then:
        user instanceof ShiroCrmUser
        user.username == "test4"
        user.name == "Test User"
        user.email == "test@test.com"
        user.enabled
    }

    def "create tenant"() {
        given:
        def result
        def features
        shiroCrmSecurityService.createUser([username: "test5", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        shiroCrmSecurityService.runAs("test5") {
            result = shiroCrmSecurityService.getTenants()
        }
        then:
        result.isEmpty()

        when:
        shiroCrmSecurityService.runAs("test5") {
            def t1 = shiroCrmSecurityService.createTenant("My First Tenant")
            def t2 = shiroCrmSecurityService.createTenant("My Second Tenant")
            result = shiroCrmSecurityService.getTenants()
            features = crmFeatureService.getFeatures(t1.id)
        }
        then:
        result.size() == 2
        features.find {it.name == 'security'}
    }

    def "update tenant"() {
        def tenant

        given:
        shiroCrmSecurityService.createUser([username: "test6", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        shiroCrmSecurityService.runAs("test6") {
            tenant = shiroCrmSecurityService.createTenant("My Tenant")
        }
        then:
        shiroCrmSecurityService.getTenantInfo(tenant.id)?.name == "My Tenant"

        when:
        shiroCrmSecurityService.updateTenant(tenant.id, [name: "Our Tenant"])

        then:
        shiroCrmSecurityService.getTenantInfo(tenant.id)?.name == "Our Tenant"
    }

    def "set tenant options"() {
        def tenant

        given:
        shiroCrmSecurityService.createUser([username: "test7", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        shiroCrmSecurityService.runAs("test7") {
            tenant = shiroCrmSecurityService.createTenant("My Tenant")
        }
        then:
        tenant.options.foo == null

        when:
        tenant = shiroCrmSecurityService.updateTenant(tenant.id, [options: [foo: 42]])

        then:
        tenant.options.foo == 42
        shiroCrmSecurityService.getTenantInfo(tenant.id).options.foo == 42
    }

    def "runAs changes current user"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test8", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        shiroCrmSecurityService.runAs("test8") {
            result = shiroCrmSecurityService.getCurrentUser()
        }
        then:
        result != null
        result.username == "test8"
    }

    def "runAs will not authenticate the user"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test9", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        shiroCrmSecurityService.runAs("test9") {
            result = shiroCrmSecurityService.isAuthenticated()
        }
        then:
        result == false
    }

    def "authenticate the user with login"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test10", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        shiroCrmSecurityService.runAs("test10") {
            SecurityUtils.subject.login(new UsernamePasswordToken("test10", "test123"))
            result = shiroCrmSecurityService.isAuthenticated()
            SecurityUtils.subject.logout()
        }
        then:
        result == true
    }

    def "runAs with non-existing username should throw exception"() {
        def result
        // No user created here.
        when:
        shiroCrmSecurityService.runAs("test11") {
            result = shiroCrmSecurityService.getCurrentUser()
        }
        then:
        thrown(org.apache.shiro.authz.UnauthorizedException)
    }

    def "runAs with disabled user should throw exception"() {
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test12", name: "Test User", email: "test@test.com", password: "test123", enabled: false])

        when:
        shiroCrmSecurityService.runAs("test12") {
            result = shiroCrmSecurityService.getCurrentUser()
        }
        then:
        thrown(org.apache.shiro.authz.UnauthorizedException)
    }

    def "wildcard permissions"() {

        def tenant
        def result

        given:
        shiroCrmSecurityService.createUser([username: "test13", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        shiroCrmSecurityService.addPermissionAlias("test", ["test:*"])

        when:
        shiroCrmSecurityService.runAs("test13") {
            tenant = shiroCrmSecurityService.createTenant("Test Tenant")
        }
        then:
        tenant != null

        when:
        TenantUtils.withTenant(tenant.id) {
            shiroCrmSecurityService.runAs("test13") {
                SecurityUtils.subject.checkPermission("test:protected")
            }
        }
        then:
        thrown(org.apache.shiro.authz.UnauthorizedException)

        when:
        TenantUtils.withTenant(tenant.id) {
            shiroCrmSecurityService.runAs("test13") {
                result = shiroCrmSecurityService.isPermitted("test:protected")
            }
        }
        then:
        result == false


        when:
        TenantUtils.withTenant(tenant.id) {
            shiroCrmSecurityService.addPermissionToUser("test", "test13")
            shiroCrmSecurityService.runAs("test13") {
                result = shiroCrmSecurityService.isPermitted("test:protected")
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
        shiroCrmSecurityService.createUser([username: "test14", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        shiroCrmSecurityService.addPermissionAlias("test", ["test:*"])

        when:
        shiroCrmSecurityService.runAs("test14") {
            tenant = shiroCrmSecurityService.createTenant("Test Tenant")
            TenantUtils.withTenant(tenant.id) {
                shiroCrmSecurityService.addPermissionToUser("test", "test14")
            }
        }
        then:
        tenant != null

        when:
        shiroCrmSecurityService.runAs("test14") {
            TenantUtils.withTenant(tenant.id) {
                result = shiroCrmSecurityService.isPermitted("test:protected")
            }
        }
        then:
        result == true

        when:
        shiroCrmSecurityService.runAs("test14") {
            TenantUtils.withTenant(42L) {
                result = shiroCrmSecurityService.isPermitted("test:protected")
            }
        }
        then:
        result == false
    }

    def "create a new role"() {
        def tenant
        def result = []

        given:
        shiroCrmSecurityService.createUser([username: "test15", name: "Test User", email: "test@test.com", password: "test123", enabled: true])
        shiroCrmSecurityService.addPermissionAlias("foo", ["foo:*"])
        shiroCrmSecurityService.addPermissionAlias("bar", ["bar:*"])
        shiroCrmSecurityService.addPermissionAlias("baz", ["baz:*"])

        when:
        shiroCrmSecurityService.runAs("test15") {
            tenant = shiroCrmSecurityService.createTenant("Test Tenant")
            TenantUtils.withTenant(tenant.id) {
                shiroCrmSecurityService.createRole("tester", ["foo", "bar"])
                shiroCrmSecurityService.addUserRole("test15", "tester")

                result << SecurityUtils.subject.hasRole("tester")
                result << shiroCrmSecurityService.isPermitted("foo:index")
                result << shiroCrmSecurityService.isPermitted("bar:index")
                result << shiroCrmSecurityService.isPermitted("baz:index") // Not included in this role
                result << shiroCrmSecurityService.isPermitted("xxx:index") // Non existing permission
            }
        }
        then:
        result == [true, true, true, false, false]

        when:
        result = []
        shiroCrmSecurityService.runAs("test15") {
            TenantUtils.withTenant(tenant.id) {
                shiroCrmSecurityService.addPermissionToRole("foo", "tester")
                shiroCrmSecurityService.addPermissionToRole("bar", "tester")
                shiroCrmSecurityService.addPermissionToRole("baz", "tester")

                result << SecurityUtils.subject.hasRole("tester")
                result << shiroCrmSecurityService.isPermitted("foo:index")
                result << shiroCrmSecurityService.isPermitted("bar:index")
                result << shiroCrmSecurityService.isPermitted("baz:index") // This time baz is included
                result << shiroCrmSecurityService.isPermitted("xxx:index") // Non existing permission
            }
        }

        then:
        result == [true, true, true, true, false]
    }

    def "create tenant with locale"() {

        given:
        def result = []
        def locale = Locale.default
        def swedish = new Locale("sv", "SE")
        def spanish = new Locale("es", "ES")
        shiroCrmSecurityService.createUser([username: "test16", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        shiroCrmSecurityService.runAs("test16") {
            result << shiroCrmSecurityService.createTenant("Default")
            result << shiroCrmSecurityService.createTenant("Svenska", [locale: swedish])
            result << shiroCrmSecurityService.createTenant("Español", [locale: spanish])
        }
        then:
        result[0].locale == locale
        result[1].locale == swedish
        result[2].locale == spanish
    }

    def "test default permissions"() {
        given:
        def securityConfig = grailsApplication.config.crm.security
        def tenant
        securityConfig.default.permission.guest = ["shiroCrmTenant:index,activate"]
        securityConfig.default.permission.user = ["shiroCrmTenant:index,activate,create,edit"]
        securityConfig.default.permission.admin = ["shiroCrmTenant:*"]
        shiroCrmSecurityService.createUser([username: "test17", name: "Test User", email: "test@test.com", password: "test123", enabled: true])

        when:
        shiroCrmSecurityService.runAs("test17") {
            tenant = shiroCrmSecurityService.createTenant("Default")
        }

        then:
        tenant != null
        ShiroCrmRole.countByTenantId(tenant.id) == 3
    }
}

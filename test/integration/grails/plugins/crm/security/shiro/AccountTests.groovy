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

class AccountTests extends GroovyTestCase {

    def crmSecurityService
    def shiroCrmSecurityService

    protected void setUp() {
        super.setUp()

        def user = shiroCrmSecurityService.createUser([username: "test", name: "Test User", password: "test123", enabled:true])
        assert user != null
        assert "test" == user.username
        assert "Test User" == user.name

        crmSecurityService.runAs("test") {
            def account = shiroCrmSecurityService.createTenant("Test Account")
            assert account != null
            assert account.id != null
            assert "Test Account" == account.name
            assert "test" == account.owner
            
            TenantUtils.withTenant(account.id) {
                shiroCrmSecurityService.createRole("admin", ["*:*"])
                shiroCrmSecurityService.createRole("guest")
            }
        }
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testDelegateIsCorrect() {
        assert crmSecurityService.crmSecurityDelegate.class == ShiroCrmSecurityDelegate.class
    }

    void testAccounts() {
        crmSecurityService.runAs("test") {
            def result = crmSecurityService.getTenants()
            assert result.size() == 1
            assert "Test Account" == result.find {it}.name
        }
    }

    void testRunAs() {
        crmSecurityService.runAs("test") {
            def p = crmSecurityService.getCurrentUser()
            assert "test" == p.username
            assert crmSecurityService.isAuthenticated() == false
        }
    }

    void testAuthenticate() {
        crmSecurityService.runAs("test") {
            SecurityUtils.subject.login(new UsernamePasswordToken("test","test123"))
            assert crmSecurityService.isAuthenticated()
            SecurityUtils.subject.logout()
        }
    }

    void testPermissions() {
        def account = ShiroCrmTenant.withCriteria {
            owner {
                eq('username', "test")
            }
        }.find {it} // Find first available account owned by user 'test'.

        assert account != null

        TenantUtils.withTenant(account.id) {
            shouldFail {
                crmSecurityService.runAs("test") {
                    SecurityUtils.subject.checkPermission("test:protected")
                }
            }

            shiroCrmSecurityService.addUserRole("test", "admin")
            shiroCrmSecurityService.addUserPermission("test", "test:*")

            crmSecurityService.runAs("test") {
                SecurityUtils.subject.checkPermission("test:protected")
            }
        }

        TenantUtils.withTenant(42L) {
            shouldFail {
                crmSecurityService.runAs("test") {
                    SecurityUtils.subject.checkPermission("test:protected")
                }
            }
        }
    }
}

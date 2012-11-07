package grails.plugins.crm.security.shiro

import grails.plugins.crm.security.CrmTenant
import grails.plugins.crm.security.CrmUser

/**
 * Test ShiroCrmTenant and ShiroCrmTenantOption.
 */
class ShiroCrmTenantSpec extends grails.plugin.spock.IntegrationSpec {

    def crmSecurityService

    def "set and get option"() {
        given:
        def user = crmSecurityService.createUser(username: "inttest", name: "Integration Test", email: "test@test.com", password: "secret", enabled: true)
        def t = crmSecurityService.runAs(user.username) {
            def a = crmSecurityService.createAccount()
            crmSecurityService.createTenant(a, "test")
        }

        when:
        t.setOption("foo", 42)
        t.setOption("bar", 43)

        then:
        t.getOption("foo") == 42
        t.getOption("bar") == 43
        t.dao.options.foo == 42
        t.dao.options.bar == 43

        when:
        t.removeOption("bar")

        then:
        t.getOption("foo") == 42
        t.dao.options.foo == 42
        t.getOption("bar") == null
        t.dao.options.bar == null
    }
}

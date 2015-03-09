package grails.plugins.crm.security.shiro

import grails.plugins.crm.core.CrmException

/**
 * Test ShiroCrmTenant and ShiroCrmTenantOption.
 */
class ShiroCrmTenantSpec extends grails.test.spock.IntegrationSpec {

    def crmAccountService
    def crmSecurityService

    def "create new tenant requires an active account"() {
        given:
        crmSecurityService.createUser([username: "blocked", name: "Blocked User", email: "blocked@test.com", password: "test123", enabled: true])

        when:
        crmSecurityService.runAs("blocked") {
            def blockedAccount = crmAccountService.createAccount(expires: new Date() -7, status: "blocked", name: "This account was blocked a week ago")
            crmSecurityService.createTenant(blockedAccount, "Blocked Tenant") // This should not be possible.
        }

        then:
        def exception = thrown(org.apache.shiro.subject.ExecutionException)
        exception.cause.class == CrmException
        exception.cause.message == 'account.not.active.message'
    }

    def "set and get option"() {
        given:
        def user = crmSecurityService.createUser(username: "inttest", name: "Integration Test", email: "test@test.com", password: "secret", enabled: true)
        def t = crmSecurityService.runAs(user.username) {
            def a = crmAccountService.createAccount(status: "active")
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

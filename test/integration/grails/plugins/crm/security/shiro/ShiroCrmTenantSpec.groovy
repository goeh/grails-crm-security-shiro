package grails.plugins.crm.security.shiro

/**
 * Test ShiroCrmTenant and ShiroCrmTenantOption.
 */
class ShiroCrmTenantSpec extends grails.plugin.spock.IntegrationSpec {

    def crmSecurityService

    def "set and get option"() {
        given:
        def userDAO = crmSecurityService.createUser(username: "inttest", name: "Integration Test", email: "test@test.com", password: "secret")
        def user = ShiroCrmUser.load(userDAO.id)
        def t = new ShiroCrmTenant(name: "test", features: ["test", "integration"], user: user).save(failOnError: true, flush: true)

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

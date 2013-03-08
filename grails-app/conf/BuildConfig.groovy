grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.repos.default = "crm"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn"
    repositories {
        grailsHome()
        mavenRepo "http://labs.technipelago.se/repo/crm-releases-local/"
        mavenRepo "http://labs.technipelago.se/repo/plugins-releases-local/"
        grailsCentral()
    }
    dependencies {
    }

    plugins {
        build(":tomcat:$grailsVersion",
                ":release:2.2.1") {
            export = false
        }
        runtime ":hibernate:$grailsVersion"

        test(":spock:0.7") { export = false }
        test(":codenarc:0.17") { export = false }
        compile(":platform-core:1.0.RC5") { excludes 'resources' }
        compile ':shiro:1.1.4'

        compile "grails.crm:crm-core:latest.integration"
        compile "grails.crm:crm-security:latest.integration"
        runtime "grails.crm:crm-feature:latest.integration"
    }
}

codenarc {
    reports = {
        CrmXmlReport('xml') {
            outputFile = 'CodeNarcReport.xml'
            title = 'Grails CRM CodeNarc Report'
        }
        CrmHtmlReport('html') {
            outputFile = 'target/test-reports/CodeNarcReport.html'
            title = 'Grails CRM CodeNarc Report'
        }
    }
    processTestUnit = false
    processTestIntegration = false
}

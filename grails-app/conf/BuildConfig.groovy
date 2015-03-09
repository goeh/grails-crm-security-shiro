grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6

<<<<<<< HEAD
=======
grails.project.fork = [
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve:false],
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven"
>>>>>>> grails-2.4
grails.project.dependency.resolution = {
    inherits("global") {}
    log "warn"
    repositories {
        grailsCentral()
        mavenCentral()
        mavenLocal()
    }
    dependencies {
        // See https://jira.grails.org/browse/GPHIB-30
        test("javax.validation:validation-api:1.1.0.Final") { export = false }
        test("org.hibernate:hibernate-validator:5.0.3.Final") { export = false }
    }
    plugins {
        build(":release:3.0.1",
                ":rest-client-builder:1.0.3") {
            export = false
        }
        test(":hibernate4:4.3.6.1") {
            excludes "net.sf.ehcache:ehcache-core"  // remove this when http://jira.grails.org/browse/GPHIB-18 is resolved
            export = false
        }
<<<<<<< HEAD
        test(":codenarc:0.21") { export = false }
        test(":code-coverage:1.2.7") { export = false }
=======
>>>>>>> grails-2.4

        test(":codenarc:0.22") { export = false }
        test(":code-coverage:2.0.3-3") { export = false }

<<<<<<< HEAD
        compile ":crm-core:2.0.2"
        compile ":crm-security:2.0.0"
        compile ":crm-feature:2.0.0"
    }
}
=======
        compile(":shiro:1.2.1") {
            excludes 'hibernate'
        }

        compile ":crm-security:2.4.0-SNAPSHOT"
    }
}
>>>>>>> grails-2.4

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
import grails.plugins.crm.security.shiro.ShiroCrmSecurityDelegate

class CrmSecurityShiroGrailsPlugin {
    // Dependency group
    def groupId = "grails.crm"
    // the plugin version
    def version = "0.8"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "Shiro Security for Grails CRM" // Headline display name of the plugin
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''
This plugin leverage the shiro plugin to authenticate/authorize Grails CRM users.
'''

    // URL to the plugin's documentation
    def documentation = "https://github.com/goeh/grails-crm-security-shiro"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPCRMSHIRO" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/goeh/grails-crm-security-shiro" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        crmSecurityDelegate(ShiroCrmSecurityDelegate) {
            shiroSecurityManager = ref('shiroSecurityManager')
        }
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        initSecurity(event.ctx)
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}

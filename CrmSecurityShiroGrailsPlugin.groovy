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

class CrmSecurityShiroGrailsPlugin {
    // Dependency group
    def groupId = "grails.crm"
    // the plugin version
    def version = "1.0-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // Load after crm-core
    def loadAfter = ['crmCore', 'shiro']
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/conf/ApplicationResources.groovy"
    ]

    def title = "Shiro Security for Grails CRM" // Headline display name of the plugin
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''
This plugin leverage the shiro plugin to authenticate/authorize Grails CRM users.
'''

    def documentation = "https://github.com/goeh/grails-crm-security-shiro"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-security-shiro/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-security-shiro"]

    def features = {
        security {
            description "Apache Shiro Security"
            link controller: 'shiroCrmTenant'
            enabled true
            required true
            hidden true
            permissions {
                guest "shiroCrmTenant:index,activate"
                user "shiroCrmTenant:index,activate,create,edit"
                admin "shiroCrmTenant:*"
            }
        }
        register {
            description "User Registration"
            link controller: 'crmRegister'
            enabled true
            hidden true
        }
        password {
            description "Reset Password"
            link controller: 'resetPassword'
            enabled true
            hidden true
        }
    }

    def doWithSpring = {
        springConfig.addAlias("crmSecurityService", "shiroCrmSecurityService")

        credentialMatcher(org.apache.shiro.authc.credential.Sha512CredentialsMatcher) {
            storedCredentialsHexEncoded = true
            hashSalted = true
            hashIterations = 1000
        }
        resetPasswordDelegate(grails.plugins.crm.security.shiro.ResetPasswordDelegate) {bean ->
            bean.autowire = "byName"
        }
    }

}

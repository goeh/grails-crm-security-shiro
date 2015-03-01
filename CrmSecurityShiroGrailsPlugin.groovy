/*
*  Copyright 2015 Goran Ehrsson.
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

import grails.plugins.crm.security.shiro.ShiroSecurityDelegate

class CrmSecurityShiroGrailsPlugin {
    def groupId = ""
    def version = "2.4.0-SNAPSHOT"
    def grailsVersion = "2.2 > *"
    def dependsOn = [:]
    def loadAfter = ['crmSecurity', 'shiro']
    def pluginExcludes = [
            "grails-app/views/error.gsp",
            "grails-app/conf/ApplicationResources.groovy"
    ]
    def title = "Apache Shiro Security for GR8 CRM"
    def author = "Goran Ehrsson"
    def authorEmail = "goran@technipelago.se"
    def description = '''
This plugin leverage the shiro plugin to authenticate and authorize GR8 CRM users.
'''
    def documentation = "https://github.com/goeh/grails-crm-security-shiro"
    def license = "APACHE"
    def organization = [name: "Technipelago AB", url: "http://www.technipelago.se/"]
    def issueManagement = [system: "github", url: "https://github.com/goeh/grails-crm-security-shiro/issues"]
    def scm = [url: "https://github.com/goeh/grails-crm-security-shiro"]

    def doWithSpring = {
        crmSecurityDelegate(ShiroSecurityDelegate) {bean->
            bean.autowire = "byName"
        }
        credentialMatcher(org.apache.shiro.authc.credential.HashedCredentialsMatcher) {
            hashAlgorithmName = "SHA-512"
            storedCredentialsHexEncoded = true
            // TODO HashedCredentialsMatcher.html#setHashSalted(boolean) is deprecated!
            hashSalted = true
            hashIterations = 1000
        }
    }

}

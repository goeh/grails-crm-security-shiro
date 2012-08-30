/*
 * Copyright (c) 2012 Goran Ehrsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package grails.plugins.crm.security.shiro

import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import javax.servlet.http.HttpServletResponse
import grails.plugins.crm.core.TenantUtils

/**
 * User Registration.
 */
class CrmRegisterController {

    def grailsApplication
    def simpleCaptchaService
    def textTemplateService

    def shiroCrmSecurityService

    LinkGenerator grailsLinkGenerator

    private Map countryCodes

    /**
     * Convert between ISO3166-alpha2 and ISO3166-alpha3 codes.
     * If argument is three letters, the two letter representation is returned.
     * If argument is two letters, the three letter representation is returned.
     *
     * @params countryCode two or three letter ISO3166 country code.
     */
    private String convertISO3166(String countryCode) {
        if (countryCode == null) {
            throw new IllegalArgumentException("countryCode is null")
        }
        switch (countryCode.length()) {
            case 2:
                return new Locale("", countryCode).getISO3Country()
            case 3:
                if (!countryCodes) {
                    for (twoLetterCode in Locale.getISOCountries()) {
                        Locale l = new Locale("", twoLetterCode)
                        countryCodes[l.getISO3Country()] = l.getCountry()
                    }
                }
                return countryCodes[countryCode]
            default:
                throw new IllegalArgumentException("Invalid length of country code: $countryCode")
        }
    }

    def index(RegisterUserCommand cmd) {
        if (request.method == "POST") {
            if (!cmd.hasErrors()) {
                if (simpleCaptchaService.validateCaptcha(params.captcha)) {
                    def success = false
                    withForm {
                        try {
                            def props = cmd.properties
                            def countryCode = props.country
                            if (countryCode) {
                                // Country codes can be stored as 2- or 3-letter ISO3166 codes.
                                def config = grailsApplication.config
                                if ((config.crm.register.countryCode.length == 2 && countryCode.length() == 3)
                                        || (config.crm.register.countryCode.length == 3 && countryCode.length() == 2)) {
                                    props.country = convertISO3166(countryCode)
                                }
                            }
                            def user = shiroCrmSecurityService.createUser(props)
                            TenantUtils.withTenant(1) {
                                sendVerificationEmail(user)
                            }
                            success = true
                        } catch (Exception e) {
                            log.error("Could not create user ${cmd.name} (${cmd.username}) <${cmd.email}>", e)
                            flash.error = 'register.error.message'
                            flash.args = [e.message]
                            flash.defaultMessage = "An error occured while creating the account"
                        }

                    }.invalidToken {
                        cmd.clearErrors()
                        flash.error = "form.submit.twice"
                        flash.defaultMessage = "Form submitted twice!"
                    }
                    if (success) {
                        render(view: 'verify', model: [user: cmd])
                        return
                    }
                } else {
                    cmd.errors.rejectValue("captcha", "captcha.invalid.message")
                }
            }
        } else {
            if (params.i) {
                request.session.crmRegisterInvitation = params.i
            }
            if (params.c) {
                request.session.crmRegisterCampaign = params.c
                if(! cmd.campaign) {
                    cmd.campaign = params.c
                }
            }
            cmd.clearErrors()
        }

        return [cmd: cmd]
    }

    private void sendVerificationEmail(params) {
        def config = grailsApplication.config.crm.register.email
        def binding = params as Map
        binding.url = grailsLinkGenerator.link(controller: controllerName, action: 'confirm', id: params.guid, absolute: true)
        def bodyText = textTemplateService.applyTemplate("register-verify-email", "text/plain", binding)
        def bodyHtml = textTemplateService.applyTemplate("register-verify-email", "text/html", binding)
        if (!(bodyText || bodyHtml)) {
            throw new RuntimeException("Template not found: [name=register-verify-email]")
        }
        sendMail {
            if(bodyText && bodyHtml) {
                multipart true
            }
            if(config.from) {
                from config.from
            }
            to params.email
            if (config.cc) {
                cc config.cc
            }
            if (config.bcc) {
                bcc config.bcc
            }
            subject config.subject ?: "Confirm registration"
            if (bodyText) {
                text bodyText
            }
            if (bodyHtml) {
                html bodyHtml
            }
        }
    }

    def verify() {

    }

    def confirm(String id) {
        def user = ShiroCrmUser.findByGuid(id)
        if (user) {
            def userInfo = shiroCrmSecurityService.updateUser(user.username, [enabled:true])
            //SecurityUtils.subject.login(new UsernamePasswordToken(cmd.username, cmd.password))
            def targetUri = grailsApplication.config.crm.register.welcome.url ?: "/welcome"
            return [user: userInfo, targetUri: targetUri]
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND)
        }
    }
}

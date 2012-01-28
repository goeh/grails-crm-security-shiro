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

import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.SecurityUtils

class RegisterController {

    static allowedMethods = [save:'POST']
    
    def grailsApplication
    def shiroCrmSecurityService

    private Map countryCodes

    def index() {
        [cmd: new RegisterUserCommand()]
    }

    def save(RegisterUserCommand cmd) {
        if (cmd.hasErrors()) {
            render(view: 'index', model: [cmd: cmd])
            return
        }
        withForm {
            try {
                def props = cmd.properties
                def countryCode = props.country
                if(countryCode) {
                    // Country codes can be stored as 2- or 3-letter ISO3166 codes.
                    def config = grailsApplication.config
                    if((config.crm.register.countryCode.length==2 && countryCode.length()==3)
                    || (config.crm.register.countryCode.length==3 && countryCode.length()==2)) {
                        props.country = convertISO3166(countryCode)
                    }
                }
                def user = shiroCrmSecurityService.createUser(props)
                SecurityUtils.subject.login(new UsernamePasswordToken(cmd.username, cmd.password))
                redirect(action: 'thanks')
            } catch (Exception e) {
                flash.error = 'default.error'
                flash.args = [e]
                flash.defaultMessage = e.message
                render(view: 'index', model: [cmd: cmd])
            }
        }.invalidToken {
            flash.error = "form.submit.twice"
            flash.defaultMessage = "Form submitted twice!"
            render(view: 'index', model: [cmd: cmd])
        }
    }

    def thanks() {
    }

    /**
     * Convert between ISO3166-alpha2 and ISO3166-alpha3 codes.
     * If argument is three letters, the two letter representation is returned.
     * If argument is two letters, the three letter representation is returned.
     *
     * @params countryCode two or three letter ISO3166 country code.
     */
    private String convertISO3166(String countryCode) {
        if(countryCode == null) {
            throw new IllegalArgumentException("countryCode is null")
        }
        switch(countryCode.length()) {
            case 2:
                return new Locale("", countryCode).getISO3Country()
            case 3:
                if(!countryCodes) {
                    for(twoLetterCode in Locale.getISOCountries()) {
                        Locale l = new Locale("", twoLetterCode)
                        countryCodes[l.getISO3Country()] = l.getCountry()
                    }
                }
                return countryCodes[countryCode]
            default:
                throw new IllegalArgumentException("Invalid length of country code: $countryCode")
        }
    }
}

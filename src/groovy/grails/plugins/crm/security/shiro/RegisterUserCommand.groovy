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

import grails.validation.Validateable

/**
 * Command Object used in user registration.
 */
@Validateable
class RegisterUserCommand implements Serializable {

    String username
    String name
    String email
    String password
    String telephone
    String postalCode
    String country
    String campaign
    String captcha

    static constraints = {
        username(size: 3..80, maxSize: 80, nullable: false, blank: false, validator: {val, obj->
            if(grails.plugins.crm.security.shiro.ShiroCrmUser.findByUsername(val)) {
                return ['register.not.unique.message', 'username', 'User', val]
            }
        })
        name(size: 3..80, maxSize: 80, nullable: false, blank: false)
        email(maxSize: 80, nullable: false, blank: false, email: true)
        password(maxSize: 80, nullable: false, blank: false)
        postalCode(size: 2..20, maxSize: 20, blank: false)
        telephone(maxSize:20, nullable:true)
        campaign(maxSize:80, nullable:true)
        captcha(maxSize:10, blank:false)
    }
}

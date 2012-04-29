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
 * under the License.
 */

package grails.plugins.crm.security.shiro

/**
 * Maps controller names
 */
class ControllerGroupMapper {

    private static final Map<String, String> mapping = [:]

    def grailsApplication

    void add(String controllerName, String controllerGroup) {
        mapping[controllerName] = controllerGroup
    }

    String map(String controllerName) {
        def group = mapping[controllerName]
        if(group == null) {
            def artefact = grailsApplication.getArtefactByLogicalPropertyName("Controller", controllerName)
            def bean = grailsApplication.mainContext.getBean(artefact.clazz.name)
            if(bean.hasProperty('controllerGroup')) {
                group = bean.controllerGroup
            }
            mapping[controllerName] = (group ?: false)
        }
        group ?: controllerName
    }

    void clear() {
        mapping.clear()
    }
}

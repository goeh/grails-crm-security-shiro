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

import org.springframework.dao.DataIntegrityViolationException

/**
 * User administration.
 */
class ShiroCrmPermissionController {

    static allowedMethods = [list: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    static navigation = [
            [group: 'admin',
                    order: 70,
                    title: 'shiroCrmNamedPermission.index.label',
                    action: 'list'
            ]
    ]

    def grailsApplication

    def list() {

        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        def result = ShiroCrmNamedPermission.list(params)
        [result: result, totalCount: result.totalCount]
    }

    def create() {
        switch (request.method) {
            case 'GET':
                return [shiroCrmNamedPermission: new ShiroCrmNamedPermission(params)]
            case 'POST':
                def shiroCrmNamedPermission = new ShiroCrmNamedPermission()
                def permissions = params.list('permissions').findAll {it}
                def data = [name: params.name, permissions: permissions]
                bindData(shiroCrmNamedPermission, data)
                if (!shiroCrmNamedPermission.save(flush: true)) {
                    render view: 'create', model: [shiroCrmNamedPermission: shiroCrmNamedPermission]
                    return
                }

                flash.success = message(code: 'shiroCrmNamedPermission.created.message', args: [message(code: 'shiroCrmNamedPermission.label', default: 'Permission'), shiroCrmNamedPermission.toString()])
                redirect action: 'list'
                break
        }
    }

    def edit() {
        def shiroCrmNamedPermission = ShiroCrmNamedPermission.get(params.id)
        if (!shiroCrmNamedPermission) {
            flash.error = message(code: 'shiroCrmNamedPermission.not.found.message', args: [message(code: 'shiroCrmNamedPermission.label', default: 'Permission'), params.id])
            redirect action: 'list'
            return
        }

        switch (request.method) {
            case 'GET':
                return [shiroCrmNamedPermission: shiroCrmNamedPermission]
            case 'POST':
                if (params.version && shiroCrmNamedPermission.version) {
                    def version = params.version.toLong()
                    if (shiroCrmNamedPermission.version > version) {
                        shiroCrmNamedPermission.errors.rejectValue('version', 'shiroCrmNamedPermission.optimistic.locking.failure',
                                [message(code: 'shiroCrmNamedPermission.label', default: 'User')] as Object[],
                                "Another user has updated this user while you were editing")
                        render view: 'edit', model: [shiroCrmNamedPermission: shiroCrmNamedPermission]
                        return
                    }
                }
                def permissions = params.list('permissions').findAll {it}
                def data = [name: params.name, permissions: permissions]
                bindData(shiroCrmNamedPermission, data)

                if (!shiroCrmNamedPermission.save(flush: true)) {
                    render view: 'edit', model: [shiroCrmNamedPermission: shiroCrmNamedPermission]
                    return
                }

                flash.success = message(code: 'shiroCrmNamedPermission.updated.message', args: [message(code: 'shiroCrmNamedPermission.label', default: 'Permission'), shiroCrmNamedPermission.toString()])
                redirect action: 'list', id: shiroCrmNamedPermission.id
                break
        }
    }

    def delete() {
        def shiroCrmNamedPermission = ShiroCrmNamedPermission.get(params.id)
        if (!shiroCrmNamedPermission) {
            flash.error = message(code: 'shiroCrmNamedPermission.not.found.message', args: [message(code: 'shiroCrmNamedPermission.label', default: 'Permission'), params.id])
            redirect action: 'list'
            return
        }

        try {
            def tombstone = shiroCrmNamedPermission.toString()
            shiroCrmNamedPermission.delete(flush: true)
            flash.warning = message(code: 'shiroCrmNamedPermission.deleted.message', args: [message(code: 'shiroCrmNamedPermission.label', default: 'Permission'), tombstone])
            redirect action: 'list'
        }
        catch (DataIntegrityViolationException e) {
            flash.error = message(code: 'shiroCrmNamedPermission.not.deleted.message', args: [message(code: 'shiroCrmNamedPermission.label', default: 'Permission'), params.id])
            redirect action: 'edit', id: params.id
        }
    }

}

package grails.plugins.crm.security.shiro

/**
 * System wide controller for security administration.
 */
class CrmSecurityAdminController {

    def index() {

        def namedPermissions = ShiroCrmNamedPermission.list()
        [namedPermissions:namedPermissions]
    }
}

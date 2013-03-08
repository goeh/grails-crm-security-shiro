import org.apache.shiro.SecurityUtils
import grails.plugins.crm.core.TenantUtils

class SecurityFilters {

    def grailsApplication

    def dependsOn = [CrmTenantFilters]

    def blockedControllers
    def publicControllers
    def protectedControllers

    def onNotAuthenticated(subject, filter) {
        // Check if the request is an Ajax one.
        if (filter.request.xhr || filter.request.contentType?.equalsIgnoreCase("text/xml")) {
            filter.response.sendError(401) // TODO Render Ajax login
            return false
        }
        return true
    }

    def filters = {
        // Ensure that all controllers and actions require an authenticated user,
        // except for the "public" controller
        auth(uri: "/**") {
            before = {
                // Ignore direct views (e.g. the default main index page).
                if (!controllerName) return true

                // Check blocked controllers in Config.groovy
                if(blockedControllers == null) {
                    blockedControllers = grailsApplication.config.crm.security.controllers.blocked ?: false
                }
                if (blockedControllers?.find {match(it, controllerName, actionName)}) {
                    response.sendError(grailsApplication.config.crm.security.blocked.status ?: 404)
                    return false
                }

                // Exclude public controllers in Config.groovy
                if(publicControllers == null) {
                    publicControllers = grailsApplication.config.crm.security.controllers.public ?: false
                }
                if (publicControllers?.find {match(it, controllerName, actionName)}) {
                    return true
                }

                // Check protected controllers in Config.groovy
                if(protectedControllers == null) {
                    protectedControllers = grailsApplication.config.crm.security.controllers.protected ?: false
                }
                if (protectedControllers?.find {match(it, controllerName, actionName)} && SecurityUtils.subject?.authenticated) {
                    return true
                }

                // Access control by convention.
                accessControl {
                    // Check that the user has the required permission for the target controller/action.
                    def permString = new StringBuilder()
                    permString << controllerName << ':' << (actionName ?: "index")

                    // Add the ID if it's in the web parameters.
                    if (params.id) permString << ':' << params.id
                    def subject = SecurityUtils.subject
                    def t0 = System.nanoTime()
                    def status = subject?.isPermitted(permString.toString())
                    if(log.isDebugEnabled()) {
                        log.debug "${status ? '+' : '!'} ${subject?.principal}@${TenantUtils.tenant} $controllerName/$actionName -> $permString (${((System.nanoTime() - t0) / 1000000).intValue()} ms)"
                    }
                    return status
                }
            }
        }
    }

    private boolean match(String ctrl, String controllerName, String actionName) {
        def (c, a) = ctrl.split(':').toList()
        if (c == controllerName) {
            if (a) {
                if (a.split(',').contains(actionName)) {
                    return true
                }
            } else {
                return true
            }
        }
        return false
    }
}

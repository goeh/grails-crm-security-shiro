import org.apache.shiro.SecurityUtils
import grails.plugins.crm.core.TenantUtils

class SecurityFilters {

    def grailsApplication

    def dependsOn = [CrmTenantFilters]

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

                // Exclude public controllers in Config.groovy
                def configControllers = grailsApplication.config.crm.security.controllers.public
                if (configControllers?.find {match(it, controllerName, actionName)}) {
                    return true
                }

                // Check protected controllers in Config.groovy
                configControllers = grailsApplication.config.crm.security.controllers.protected
                if (configControllers?.find {match(it, controllerName, actionName)} && SecurityUtils.subject?.authenticated) {
                    return true
                }

                // Access control by convention.
                accessControl {
                    // Check that the user has the required permission for the target controller/action.
                    def permString = new StringBuilder()
                    permString << controllerName << ':' << (actionName ?: "index")

                    // Add the ID if it's in the web parameters.
                    if (params.id) permString << ':' << params.id
                    def status = SecurityUtils.subject.isPermitted(permString.toString())
                    println "${SecurityUtils.subject?.principal}@${TenantUtils.tenant} $controllerName/$actionName ---> $permString = $status"
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

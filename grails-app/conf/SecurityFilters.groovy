import org.apache.shiro.SecurityUtils
import grails.plugins.crm.core.TenantUtils

class SecurityFilters {

    def grailsApplication
    def controllerGroupMapper

    def dependsOn = [CrmTenantFilters]

    def publicControllers = []
    def authenticatedControllers = ["welcome", "account", "settings"]

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

                // Exclude the "public" controller.
                if (publicControllers.contains(controllerName)) return true

                // Exclude public controllers in Config.groovy
                def configControllers = grailsApplication.config.crm.security.controllers.public ?: []
                if (configControllers?.contains(controllerName)) {
                    return true
                }

                if(authenticatedControllers.contains(controllerName) && SecurityUtils.subject?.authenticated) {
                    // TODO This is a hack. I spent an hour trying to add a separate pattern/filter
                    // for 'welcome' controller but I could not get is to pass permission check.
                    return true
                }

                // Check protected controllers in Config.groovy
                configControllers = grailsApplication.config.crm.security.controllers.protected ?: []
                if(configControllers?.contains(controllerName) && SecurityUtils.subject?.authenticated) {
                    return true
                }

                // Access control by convention.
                accessControl {
                    // Check that the user has the required permission for the target controller/action.
                    def permString = new StringBuilder()
                    def controllerAlias = controllerName//controllerGroupMapper(controllerName)
                    permString << /*TenantUtils.tenant.toString() << ':' << */controllerAlias << ':' << (actionName ?: "index")

                    // Add the ID if it's in the web parameters.
                    if (params.id) permString << ':' << params.id
                    def status = SecurityUtils.subject.isPermitted(permString.toString())
                    println "${SecurityUtils.subject?.principal}@${TenantUtils.tenant} $controllerName/$actionName ---> $permString = $status"
                    return status
                }
            }
        }
    }
}

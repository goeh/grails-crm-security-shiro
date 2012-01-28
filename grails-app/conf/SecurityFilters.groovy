class SecurityFilters {
    def publicControllers = ["register", "auth"]

    def onNotAuthenticated(subject, filter) {
        // Check if the request is an Ajax one.
        if (filter.request.xhr) {
            // Render Ajax login
            filter.response.sendError(401)
        } else {
            // Redirect to the usual login page.
            def request = filter.request
            def targetURI = request.forwardURI - request.contextPath
            if (request.queryString) {
                if (!request.queryString.startsWith('?')) {
                    targetURI += '?'
                }
                targetURI += request.queryString
            }
            filter.redirect(controller: "auth", action: "login", params: [targetUri: targetURI])
        }
    }

    def filters = {
        // Ensure that all controllers and actions require an authenticated user,
        // except for the "public" controller
        auth(controller: "*", action: "*") {
            before = {
                // Exclude the "public" controller.
                if (publicControllers.contains(controllerName)) return true
                // This just means that the user must be authenticated. He does
                // not need any particular role or permission.
                accessControl { true }
            }
        }
    }
}

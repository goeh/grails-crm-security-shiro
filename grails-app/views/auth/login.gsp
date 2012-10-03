<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="auth.login.title" default="Login"/></title>
    <r:script>
        $(document).ready(function() {
            $("input[name='username']:visible:first").focus();
        })
    </r:script>
</head>

<body>

<div class="hero-unit clearfix visible-desktop">
    <crm:header title="auth.login.title" default="Please sign in"/>
</div>

<g:form action="signIn" name="loginForm" class="hidden-desktop visible-phone">

    <crm:header title="auth.login.title" default="Please sign in"/>

    <input type="hidden" name="targetUri" value="${targetUri}"/>

    <div class="control-group">
        <label class="control-label" for="username"><g:message code="auth.login.username"
                                                               default="Username"/></label>

        <div class="controls">
            <g:textField name="username" value="${username}" autocapitalize="off"/>
        </div>
    </div>

    <div class="control-group">
        <label class="control-label" for="password"><g:message code="auth.login.password"
                                                               default="Password"/></label>

        <div class="controls">
            <g:passwordField name="password" value="${password}"/>
        </div>
    </div>

    <div class="form-actions">
        <crm:button visual="primary" icon="icon-ok icon-white" label="auth.login.button.submit.label"/>
    </div>

</g:form>

<div style="margin-left: 2em;">
    <crm:featureLink feature="register">&raquo; <g:message code="auth.register.label"
                                                  default="No account yet? Create one now!"/></crm:featureLink><br/>
    <crm:featureLink feature="password">&raquo; <g:message code="auth.password.forgot.label"/></crm:featureLink>
</div>

</body>
</html>

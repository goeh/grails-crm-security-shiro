<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="auth.login.title" default="Login"/></title>
    <r:script>
        $(document).ready(function() {
            $("#login-username").focus();
        });
    </r:script>
</head>

<body>

<div class="hero-unit clearfix visible-desktop">
    <crm:header title="auth.login.title" default="Please sign in"/>
</div>

<g:form action="signIn" name="loginForm" class="hidden-desktop">

    <crm:header title="auth.login.title" default="Please sign in"/>

    <input type="hidden" name="targetUri" value="${targetUri}"/>

    <div class="control-group">
        <label class="control-label" for="username"><g:message code="auth.login.username"
                                                               default="Username"/></label>

        <div class="controls">
            <g:textField name="username" value="${username}" autofocus=""/>
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

<g:content tag="sidebar">

    <div class="well">

        <ul class="nav nav-list">
            <li><g:link mapping="register"><g:message code="register.invite.label"
                                                      default="No account yet? Create one now!"/></g:link></li>
            <li><g:link mapping="password"><g:message code="auth.password.forgot.label"
                                                      default="Forgot password?"/></g:link></li>
        </ul>
    </div>

</g:content>

</body>
</html>

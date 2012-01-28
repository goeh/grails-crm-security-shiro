<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Sign up for Grails CRM</title>
</head>

<body>

<div class="page-header">
    <h1>Sign up for for Grails CRM</h1>
</div>

<g:hasErrors bean="${cmd}">
    <div class="alert-message block-message error">
        <g:renderErrors bean="${cmd}" as="list"/>
    </div>
</g:hasErrors>

<g:form action="save" useToken="true">

    <fieldset>

        <div class="clearfix ${hasErrors(bean: cmd, field: 'username', 'error')}">
            <label for="username">Username*</label>

            <div class="input">
                <g:textField class="xlarge default-text" name="username" size="40" maxlength="40"
                             value="${cmd.username}" title="Your User Name..."/>
            </div>
        </div>

        <div class="clearfix ${hasErrors(bean: cmd, field: 'name', 'error')}">
            <label for="name">Name*</label>

            <div class="input">
                <g:textField class="xlarge default-text" name="name" size="40" maxlength="40" value="${cmd.name}"
                             title="Your Real Name..."/>
            </div>
        </div>

        <div class="clearfix ${hasErrors(bean: cmd, field: 'email', 'error')}">
            <label for="email">Email*</label>

            <div class="input">
                <g:textField class="xlarge default-text" name="email" size="40" maxlength="40" value="${cmd.email}"
                             title="Email Address..."/>
            </div>
        </div>

        <div class="clearfix ${hasErrors(bean: cmd, field: 'password', 'error')}">
            <label for="password">Password*</label>

            <div class="input">
                <g:passwordField class="xlarge" name="password" size="40" maxlength="40" value="${cmd.password}"
                                 title="Choose Password..."/>
            </div>
        </div>

        <div class="clearfix">
            <label for="country">Country*</label>

            <div class="input">
                <g:countrySelect class="xlarge" name="country" value="${cmd.country}"/>
            </div>
        </div>

        <div class="actions">
            <g:actionSubmit value="Create Account" action="save" class="btn primary"/>
        </div>
    </fieldset>
</g:form>

</body>
</html>

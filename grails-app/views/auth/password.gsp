<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="auth.password.forgot.title" default="Reset Password"/></title>
</head>

<body>

<div class="span3"><p>&nbsp;</p></div>

<div class="span6 well">

    <g:hasErrors bean="${cmd}">
        <bootstrap:alert class="alert-error">
            <ul>
                <g:eachError bean="${cmd}" var="error">
                    <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                            error="${error}"/></li>
                </g:eachError>
            </ul>
        </bootstrap:alert>
    </g:hasErrors>

    <crm:header title="register.title"/>

    <g:form action="save" useToken="true">

        <f:with bean="${cmd}">
            <f:field property="username" label="register.username.label">
                <g:textField name="username" value="${cmd.username}" autofocus=""
                             placeholder="${message(code:'register.username.placeholder')}"/>
            </f:field>
            <f:field property="name" label="register.name.label">
                <g:textField name="name" value="${cmd.name}"
                             placeholder="${message(code:'register.name.placeholder')}"/>
            </f:field>
            <f:field property="email" label="register.email.label">
                <g:textField name="email" value="${cmd.email}"
                             placeholder="${message(code:'register.email.placeholder')}"/>
            </f:field>
            <f:field property="password" label="register.password.label">
                <g:passwordField name="password" value="${cmd.password}"
                                 placeholder="${message(code:'register.password.placeholder')}"/>
            </f:field>
            <f:field property="country" label="register.country.label">
                <g:countrySelect name="country" value="${cmd.country}" noSelection="[null:'']"/>
            </f:field>
        </f:with>

        <div class="form-actions">
            <crm:button visual="primary" icon="icon-ok icon-white" label="register.button.create.label"/>
        </div>

    </g:form>

</div>

</body>
</html>

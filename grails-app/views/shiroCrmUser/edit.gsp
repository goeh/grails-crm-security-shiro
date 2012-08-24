<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'shiroCrmUser.label', default: 'User')}"/>
    <title><g:message code="shiroCrmUser.edit.title" args="[entityName, shiroCrmUser]"/></title>
</head>

<body>

<crm:header title="shiroCrmUser.edit.title" subtitle="${shiroCrmUser.name}" args="[entityName, shiroCrmUser]"/>

<g:hasErrors bean="${shiroCrmUser}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${shiroCrmUser}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="edit">

    <f:with bean="shiroCrmUser">

        <g:hiddenField name="id" value="${shiroCrmUser?.id}"/>
        <g:hiddenField name="version" value="${shiroCrmUser?.version}"/>

        <div class="row-fluid">

            <div class="span4">
                <f:field property="name" input-autofocus=""/>
                <f:field property="address1"/>
                <f:field property="address2"/>
                <f:field property="address3"/>
                <f:field property="postalCode"/>
                <f:field property="city"/>
                <f:field property="telephone"/>
                <f:field property="mobile"/>
            </div>

            <div class="span4">
                <f:field property="enabled"/>
                <f:field property="loginFailures" input-class="span1"/>
            </div>

            <div class="span4">
            </div>

        </div>

        <div class="form-actions">
            <crm:button visual="primary" icon="icon-ok icon-white" label="shiroCrmUser.button.update.label"/>
        </div>

    </f:with>

</g:form>

</body>
</html>

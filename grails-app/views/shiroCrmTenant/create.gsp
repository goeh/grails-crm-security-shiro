<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'shiroCrmTenant.label', default: 'Account')}"/>
    <title><g:message code="shiroCrmTenant.create.title" subtitle="shiroCrmTenant.create.subtitle"
                      args="[entityName, shiroCrmUser.name]" default="Create Account"/></title>
</head>

<body>

<crm:header title="shiroCrmTenant.create.title" subtitle="shiroCrmTenant.create.subtitle"
            args="[entityName, shiroCrmUser.name]"/>

<g:hasErrors bean="${shiroCrmTenant}">
    <crm:alert class="alert-error">
        <ul>
            <g:eachError bean="${shiroCrmTenant}" var="error">
                <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                        error="${error}"/></li>
            </g:eachError>
        </ul>
    </crm:alert>
</g:hasErrors>

<g:form action="create">

    <div class="row-fluid">
        <div class="span6">

            <div class="row-fluid">
                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><g:message code="shiroCrmTenant.name.label"/></label>

                        <div class="controls">
                            <g:textField name="name" maxlength="40" autofocus="" required="" value="${shiroCrmTenant.name}"
                                         placeholder="${message(code:'shiroCrmTenant.name.placeholder')}"/>
                        </div>
                    </div>
                </div>

                <div class="span6">
                    <div class="control-group">
                        <label class="control-label"><g:message code="shiroCrmUser.defaultTenant.label"/></label>

                        <div class="controls">
                            <label class="checkbox">
                                <g:checkBox name="defaultTenant" value="${!shiroCrmUser.defaultTenant}"/>
                                <g:message code="shiroCrmUser.defaultTenant.help"/>
                            </label>
                        </div>
                    </div>
                </div>
            </div>

            <h3><g:message code="shiroCrmTenant.features.select.title"/></h3>

            <g:render template="features"/>
        </div>

        <div class="span6">
            <tt:html name="account-create-help"></tt:html>
        </div>
    </div>

    <div class="form-actions">
        <crm:button icon="icon-ok icon-white" visual="primary"
                    label="shiroCrmTenant.button.save.label" accesskey="S"/>
        <crm:button type="link" action="index" icon="icon-remove"
                    label="shiroCrmTenant.button.cancel.label"
                    accesskey="B"/>
    </div>

</g:form>

</body>
</html>

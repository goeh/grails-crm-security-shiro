<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'shiroCrmNamedPermission.label', default: 'Named Permission')}"/>
    <title><g:message code="shiroCrmNamedPermission.edit.title" args="[entityName, shiroCrmNamedPermission]"/></title>
</head>

<body>

<crm:header title="shiroCrmNamedPermission.edit.title" args="[entityName, shiroCrmNamedPermission]"/>

<div class="row-fluid">
    <div class="span9">

        <g:hasErrors bean="${shiroCrmNamedPermission}">
            <crm:alert class="alert-error">
                <ul>
                    <g:eachError bean="${shiroCrmNamedPermission}" var="error">
                        <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                                error="${error}"/></li>
                    </g:eachError>
                </ul>
            </crm:alert>
        </g:hasErrors>

        <g:form class="form-horizontal" action="edit"
                id="${shiroCrmNamedPermission?.id}">
            <g:hiddenField name="version" value="${shiroCrmNamedPermission?.version}"/>

            <f:with bean="shiroCrmNamedPermission">
                <f:field property="name" input-autofocus=""/>
            </f:with>

            <div class="control-group">
                <label class="control-label">Behörighet</label>

                <div class="controls">
                    <g:each in="${shiroCrmNamedPermission.permissions}" var="perm">
                        <input type="text" name="permissions" maxlength="255" class="span9" value="${perm.encodeAsHTML()}"/><br/>
                    </g:each>
                    <input type="text" name="permissions" maxlength="255" class="span9" value=""/><br/>
                </div>
            </div>

            <div class="form-actions">
                <crm:button visual="primary" icon="icon-ok icon-white"
                            label="shiroCrmNamedPermission.button.update.label"/>
                <crm:button action="delete" visual="danger" icon="icon-trash icon-white"
                            label="shiroCrmNamedPermission.button.delete.label"
                            confirm="shiroCrmNamedPermission.button.delete.confirm.message"
                            permission="shiroCrmNamedPermission:delete"/>
            </div>
        </g:form>
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>

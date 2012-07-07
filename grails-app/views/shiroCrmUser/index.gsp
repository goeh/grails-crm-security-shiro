<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'shiroCrmUser.label', default: 'User')}"/>
    <title><g:message code="shiroCrmUser.find.title" args="[entityName]"/></title>
</head>

<body>

<div class="row-fluid">
    <div class="span9">

        <crm:header title="shiroCrmUser.find.title" args="[entityName]"/>

        <g:form action="list">

            <f:with bean="cmd">
                <f:field property="username" label="shiroCrmUser.username.label" input-autofocus=""/>
                <f:field property="name" label="shiroCrmUser.name.label"/>
                <f:field property="email" label="shiroCrmUser.email.label"/>
                <f:field property="postalCode" label="shiroCrmUser.postalCode.label"/>
                <f:field property="campaign" label="shiroCrmUser.campaign.label"/>
                <div class="control-group">
                    <label class="control-label"><g:message code="shiroCrmUser.status.label" default="Status"/></label>

                    <div class="controls">
                        <label class="checkbox inline">
                            <g:checkBox name="status" value="true"/>
                            <g:message code="shiroCrmUser.enabled.true.label" default="Enabled"/>
                        </label>
                        <label class="checkbox inline">
                            <g:checkBox name="status" value="false"/>
                            <g:message code="shiroCrmUser.enabled.false.label" default="Disabled"/>
                        </label>
                    </div>
                </div>
            </f:with>

            <div class="form-actions btn-toolbar">
                <crm:selectionMenu visual="primary">
                    <crm:button action="list" icon="icon-search icon-white" visual="primary"
                                label="shiroCrmUser.button.find.label"/>
                </crm:selectionMenu>
            </div>

        </g:form>
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>

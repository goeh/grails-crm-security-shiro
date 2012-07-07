<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'shiroCrmUser.label', default: 'User')}"/>
    <title><g:message code="shiroCrmUser.list.title" args="[entityName]"/></title>
</head>

<body>

<crm:header title="shiroCrmUser.list.title" subtitle="Sökningen resulterade i ${totalCount} st användare"
            args="[entityName]">
</crm:header>

<div class="row-fluid">
    <div class="span9">
        <table class="table table-striped">
            <thead>
            <tr>
                <g:sortableColumn property="username"
                                  title="${message(code: 'shiroCrmUser.username.label', default: 'Username')}"/>

                <g:sortableColumn property="name"
                                  title="${message(code: 'shiroCrmUser.name.label', default: 'Name')}"/>

                <g:sortableColumn property="email"
                                  title="${message(code: 'shiroCrmUser.email.label', default: 'Email')}"/>

                <g:sortableColumn property="postalCode"
                                  title="${message(code: 'shiroCrmUser.postalCode.label', default: 'Postal Code')}"/>
                <g:sortableColumn property="campaign"
                                  title="${message(code: 'shiroCrmUser.campaign.label', default: 'Campaign')}"/>
            </tr>
            </thead>
            <tbody>
            <g:each in="${result}" var="shiroCrmUser">
                <tr>

                    <td>
                        <g:link action="show" id="${shiroCrmUser.id}">
                            ${fieldValue(bean: shiroCrmUser, field: "username")}
                        </g:link>
                    </td>

                    <td>
                        <g:link action="show" id="${shiroCrmUser.id}">
                            ${fieldValue(bean: shiroCrmUser, field: "name")}
                        </g:link>
                    </td>
                    <td>
                        <g:link action="show" id="${shiroCrmUser.id}">
                            ${fieldValue(bean: shiroCrmUser, field: "email")}
                        </g:link>
                    </td>
                    <td>${fieldValue(bean: shiroCrmUser, field: "postalCode")}</td>
                    <td>${fieldValue(bean: shiroCrmUser, field: "campaign")}</td>

                </tr>
            </g:each>
            </tbody>
        </table>

        <crm:paginate total="${totalCount}"/>

        <div class="form-actions btn-toolbar">
            <crm:selectionMenu visual="primary"/>
        </div>
    </div>

    <div class="span3">
        <crm:submenu/>
    </div>

</div>

</body>
</html>

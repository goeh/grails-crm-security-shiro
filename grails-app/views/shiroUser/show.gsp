<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'shiroUser.label', default: 'User')}" />
  <title><g:message code="shiroUser.show.label" default="Show User" args="[entityName, shiroUser]" /></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="shiroUser.show.label" default="Show User" args="[entityName, shiroUser]" /></h1>

    <div class="golden-left">
      <fieldset>
        <legend><g:message code="shiroUser.details.label" default="Details"/></legend>
        <label><g:message code="shiroUser.username.label" default="Username" /></label>
        <span class="value">${fieldValue(bean:shiroUser, field:'username')}</span>

        <label><g:message code="shiroUser.name.label" default="Name" /></label>
        <span class="value">${fieldValue(bean:shiroUser, field:'name')}</span>

        <label><g:message code="shiroUser.email.label" default="Email" /></label>
        <span class="value">${fieldValue(bean:shiroUser, field:'email')}</span>
      </fieldset>
    </div>

    <div class="golden-right">
      <fieldset>
        <legend><g:message code="shiroUser.permissions.label" default="Permissions"/></legend>
        <label><g:message code="shiroUser.enabled.label" default="Enabled" /></label>
        <span class="value">${shiroUser.enabled ? 'Ja' : 'Nej'}</span>

        <label><g:message code="shiroUser.roles.label" default="User Roles" /></label>
        <span class="value"><g:each in="${shiroUser.roles}" var="role" status="i"><g:if test="${i}">, </g:if>${role.encodeAsHTML()}</g:each></span>
      </fieldset>
    </div>
    <div class="clear"></div>

    <div class="buttons">
      <crm:link class="positive" action="edit" id="${shiroUser.id}" icon="pencil" message="shiroUser.button.edit.label"/>
    </div>
  </div>
</body>
</html>

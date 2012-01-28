<html>
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'shiroRole.label', default: 'Role')}" />
  <title><g:message code="shiroRole.show.title" args="[entityName, shiroRole]"/></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="shiroRole.show.title" args="[entityName, shiroRole]"/></h1>

    <fieldset>
      <legend><g:message code="shiroRole.name.label" default="Name"/></legend>
      <label><g:message code="shiroRole.name.label" /></label>
      <span class="value">${fieldValue(bean:shiroRole, field:'name')}</span>
    </fieldset>

    <fieldset>
      <legend><g:message code="shiroRole.permissions.label" default="Permissions"/></legend>
      <label><g:message code="shiroRole.permissions.label" /></label>
      <span class="value">${shiroRole.permissions ? shiroRole.permissions.join('\n').encodeAsHTML().replace('\n', '<br/>') : message(code:'shiroRole.permissions.empty')}</span>
    </fieldset>

    <div class="buttons">
      <crm:link class="positive" action="edit" id="${shiroRole.id}" icon="pencil" message="shiroRole.button.edit.label"/>
    </div>
  </div>

</body>
</html>

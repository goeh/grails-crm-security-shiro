<html>
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'shiroRole.label', default: 'Role')}" />
  <title><g:message code="shiroRole.create.title" args="[entityName]"/></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="shiroRole.create.title" args="[entityName]"/></h1>

  <g:hasErrors bean="${shiroRole}">
    <g:content tag="crm-alert">
      <div class="errors">
        <g:renderErrors bean="${shiroRole}" as="list"/>
      </div>
    </g:content>
  </g:hasErrors>

    <g:form action="save" name="inputForm">

      <fieldset>
        <legend><g:message code="shiroRole.name.label" default="Name"/></legend>

        <label for="name"><g:message code="shiroRole.name.label"/></label>
        <g:textField name="name" value="${shiroRole.name}"/>
      </fieldset>

      <fieldset>
        <legend><g:message code="shiroRole.permissions.label" default="Permissions"/></legend>
        <label><g:message code="shiroRole.permissions.label" /></label>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/>
      </fieldset>

      <div class="buttons">
        <crm:button class="positive" action="save" icon="disk" message="shiroRole.button.save.label"/>
      </div>

    </g:form>
  </div>

</body>
</html>

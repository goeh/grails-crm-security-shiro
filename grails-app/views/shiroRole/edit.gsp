<html>
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'shiroRole.label', default: 'Role')}" />
  <title><g:message code="shiroRole.edit.title" args="[entityName, shiroRole]" /></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="shiroRole.edit.title" args="[entityName, shiroRole]" /></h1>

  <g:hasErrors bean="${shiroRole}">
    <g:content tag="crm-alert">
      <div class="errors">
        <g:renderErrors bean="${shiroRole}" as="list"/>
      </div>
    </g:content>
  </g:hasErrors>

    <g:form name="inputForm">
      <input type="hidden" name="id" value="${shiroRole?.id}" />
      <input type="hidden" name="version" value="${shiroRole?.version}" />

      <fieldset>
        <legend><g:message code="shiroRole.name.label" default="Name"/></legend>

        <label for="name"><g:message code="shiroRole.name.label"/></label>
        <g:textField name="name" value="${shiroRole.name}"/>
      </fieldset>

      <fieldset>
        <legend><g:message code="shiroRole.permissions.label" default="Permissions"/></legend>
        <label><g:message code="shiroRole.permissions.label" /></label>
        <g:each in="${shiroRole.permissions}" var="p">
          <g:textField name="permission" size="100" maxlength="255" value="${p}"/><br/>
        </g:each>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/><br/>
        <g:textField name="permission" size="100" maxlength="255" value=""/>
      </fieldset>

      <div class="buttons">
        <crm:button class="positive" action="update" icon="disk" message="shiroRole.button.save.label"/>
        <crm:button class="negative" action="delete" icon="delete" message="shiroRole.button.delete.label" confirm="true"/>
      </div>

    </g:form>
  </div>

</body>
</html>

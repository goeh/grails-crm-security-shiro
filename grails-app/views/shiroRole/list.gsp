<html>
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'shiroRole.label', default: 'Role')}" />
  <title><g:message code="shiroRole.list.title" default="Roles" args="[entityName]"/></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="shiroRole.list.title" default="Roles" args="[entityName]"/></h1>

    <table class="list">
      <thead>
        <tr>
      <g:sortableColumn property="name" titleKey="shiroRole.name.label" />
      <th><g:message code="shiroRole.permissions.label" /></th>
      </tr>
      </thead>
      <tbody>
      <g:each in="${result}" status="i" var="role">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
          <td><g:link action="show" id="${role.id}">${fieldValue(bean:role, field:'name')}</g:link></td>
        <td>[${role.permissions ? role.permissions.join('], [').encodeAsHTML() : ''}]</td>
        </tr>
      </g:each>
      </tbody>
      <tfoot class="paginateButtons">
        <tr>
          <td colspan="2"><g:paginate total="${totalCount}"/><span class="totalCount"><g:message code="shiroRole.totalCount.label" default="{0} records" args="[totalCount]"/></span></td>
      </tr>
      </tfoot>
    </table>

  </div>

</body>
</html>

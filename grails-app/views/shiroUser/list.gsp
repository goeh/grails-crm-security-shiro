<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'shiroUser.label', default: 'User')}" />
  <title><g:message code="shiroUser.list.title" default="Users" args="[entityName]"/></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="shiroUser.list.title" default="Users" args="[entityName]"/></h1>

    <table class="list">
      <thead>
        <tr>
      <g:sortableColumn property="username" title="Username" titleKey="shiroUser.username.label" />
      <g:sortableColumn property="name" title="Name" titleKey="shiroUser.name.label" />
      <g:sortableColumn property="email" title="Email" titleKey="shiroUser.email.label" />
      <g:sortableColumn property="enabled" title="Enabled" titleKey="shiroUser.enabled.label" />
      </tr>
      </thead>
      <tbody>
      <g:each in="${result}" status="i" var="shiroUser">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
          <td ${shiroUser.enabled ? '' : 'style="text-decoration:line-through"'}><g:link action="show" id="${shiroUser.id}">${fieldValue(bean:shiroUser, field:'username')}</g:link></td>
        <td ${shiroUser.enabled ? '' : 'style="text-decoration:line-through"'}><g:link action="show" id="${shiroUser.id}">${fieldValue(bean:shiroUser, field:'name')}</g:link></td>
        <td><a href="mailto:${fieldValue(bean:shiroUser, field:'email')}">${fieldValue(bean:shiroUser, field:'email')}</a></td>
        <td>${shiroUser.enabled ? 'Ja' : 'Nej'}</td>
        </tr>
      </g:each>
      </tbody>
      <tfoot class="paginateButtons">
        <tr>
          <td colspan="4"><g:paginate total="${totalCount}"/><span class="totalCount"><g:message code="shiroUser.totalCount.label" default="{0} records" args="[totalCount]"/></span></td>
      </tr>
      </tfoot>
    </table>

  </div>
</body>
</html>

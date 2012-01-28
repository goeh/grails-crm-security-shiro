<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta name="layout" content="main" />
  <g:set var="entityName" value="${message(code: 'shiroUser.label', default: 'User')}" />
  <title><g:message code="shiroUser.create.title" default="Create User" args="[entityName]"/></title>
</head>
<body>

  <div class="dialog">
    <h1><g:message code="shiroUser.create.title" default="Create User" args="[entityName]"/></h1>

  <g:hasErrors bean="${shiroUser}">
    <g:content tag="crm-alert">
      <div class="errors">
        <g:renderErrors bean="${shiroUser}" as="list"/>
      </div>
    </g:content>
  </g:hasErrors>

    <g:form action="save" name="inputForm">

      <div class="golden-left">
        <fieldset>
          <legend><g:message code="shiroUser.details.label" default="Details"/></legend>
          <label for="username"><g:message code="shiroUser.username.label" default="Username" /></label>
          <input type="text" id="username" name="username" value="${fieldValue(bean:shiroUser,field:'username')}"/>

          <label for="name"><g:message code="shiroUser.name.label" default="Real Name" /></label>
          <input type="text" size="40" id="name" name="name" value="${fieldValue(bean:shiroUser,field:'name')}"/>

          <label for="email"><g:message code="shiroUser.email.label" default="Email" /></label>
          <input type="text" size="40" id="email" name="email" value="${fieldValue(bean:shiroUser,field:'email')}"/>

          <label for="password1"><g:message code="shiroUser.password1.label" default="Password" /></label>
          <input type="password" id="password1" name="password1"/>

          <label for="password2"><g:message code="shiroUser.password2.label" default="Repeat password" /></label>
          <input type="password" id="password2" name="password2"/>

          <label for="telephone"><g:message code="shiroUser.telephone.label" default="Telephone" /></label>
          <input type="text" id="telephone" name="telephone" value="${fieldValue(bean:shiroUser,field:'telephone')}"/>

          <label for="prefix"><g:message code="shiroUser.prefix.label" default="Prefix" /></label>
          <input type="text" id="prefix" name="prefix" value="${fieldValue(bean:shiroUser,field:'prefix')}"/>
        </fieldset>
      </div>

      <div class="golden-right">
        <fieldset>
          <legend><g:message code="shiroUser.misc.label" default="Misc"/></legend>

          <label for="type"><g:message code="shiroUser.type.label" default="Type" />:</label>
          <g:select id="type" name="type" from="${shiroUser.constraints.type.inList}" value="${shiroUser.type}" valueMessagePrefix="shiroUser.type" ></g:select>

          <label for="enabled"><g:message code="shiroUser.enabled.label" default="Enabled" /></label>
          <g:checkBox id="enabled" name="enabled" value="${shiroUser?.enabled}" ></g:checkBox>
        </fieldset>
        <fieldset>
          <legend><g:message code="shiroUser.roles.label" default="Roles" /></legend>
          <p>
          <g:each in="${roleList}" var="role" status="i">
            <input type="hidden" name="_role_${role.id}"/>
            <input type="checkbox" name="role_${role.id}"/>
              ${fieldValue(bean:role,field:'name')}<br/>
          </g:each>
          </p>
        </fieldset>
      </div>

      <div class="clear"></div>

      <div class="buttons">
        <crm:button class="positive" action="save" icon="disk" message="shiroUser.button.save.label"/>
      </div>

    </g:form>
  </div>
</body>
</html>

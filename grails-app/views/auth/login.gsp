<html>
<head>
  <meta name="layout" content="main"/>
  <title>
    <g:message code="application.name.1" default="Grails"/>
    <g:message code="application.name.2" default="CRM"/>
    -
    <g:message code="login.title" default="Login"/>
  </title>
  <style type="text/css">
  #loginForm .buttons {
    margin-top: 10px;
  }
  </style>
</head>

<body>

<div class="dialog">
  <h1>
    <g:message code="application.name.1" default="Grails"/>
    <g:message code="application.name.2" default="CRM"/>
    -
    <g:message code="login.title" default="Login"/>
  </h1>

  <g:form action="signIn" id="loginForm" name="loginForm">

      <fieldset>
        <legend><g:message code="login.caption" default="Please sign in..."/></legend>

        <input type="hidden" name="targetUri" value="${targetUri}"/>

        <label><g:message code="login.username" default="Username"/></label>
        <input type="text" name="username" value="${username}" size="25"/>

        <label><g:message code="login.password" default="Password"/></label>
        <input type="password" name="password" value="" size="25"/>

        <br/>

        <g:checkBox name="rememberMe" value="${rememberMe}"/>
        <label class="inline"><g:message code="login.rememberMe" default="Remember me"/></label>

        <g:if test="${flash.message}">
          <div class="message"><g:message code="${flash.message}" args="${flash.args}"
                                          default="${flash.defaultMessage ?: flash.message}"/></div>
        </g:if>

      </fieldset>

    <div class="buttons">
      <crm:button class="positive" action="signIn" icon="accept" message="login.submit"/>
    </div>

  </g:form>
</div>

<script type="text/javascript">
  <!--
  (function() {
    var elem = document.forms["loginForm"].elements["username"];
    if (elem != null) {
      elem.focus();
    }
  })();
  // -->
</script>

</body>
</html>

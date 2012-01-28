<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Congratulations!</title>
</head>

<body>
<div class="container">

    <div class="span2"></div>

    <div class="span12" style="padding-top:40px;">

        <h1>Congratulations!</h1>

        <h2>You are now a registered Grails CRM user.</h2>

        <h3>Start by creating your first CRM account.</h3>

        <p>All information you enter into Grails CRM will be associated with an account. You can have more than one account per user.</p>

        <blockquote>Example: If you are running two different businesses and also want to keep track of your private contacts, you typically create three CRM accounts.</blockquote>

    </div>

    <div class="actions">
        <g:link class="btn primary" controller="crmUser" action="index">Create my first CRM account</g:link>
        <g:link class="btn info" controller="crmUser" action="edit">Change my settings</g:link>
        <g:link class="btn danger" controller="auth" action="logout">Logout</g:link>
    </div>
</div>
</body>
</html>

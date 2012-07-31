<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="register.confirm.title" default="Your registration is now confirmed"/></title>
</head>

<body>

<g:content tag="hero">
    <div class="hero-unit">
        <h1><g:message code="register.confirm.title"/></h1>
    </div>
</g:content>


<div class="row-fluid">

    <div class="span5">
        <tt:html name="register-confirm-left"/>
    </div>

    <div class="span7">

        <tt:html name="register-confirm-main">
            <p>
                Your registration is now confirmed and you can start using our services.
            </p>
        </tt:html>

        <div class="form-actions">
            <crm:button type="link" controller="auth" action="login"
                        params="${[username: user.username, targetUri: targetUri]}" visual="primary"
                        icon="icon-arrow-right icon-white" label="register.button.continue.label"/>
        </div>

    </div>

</div>

</body>
</html>

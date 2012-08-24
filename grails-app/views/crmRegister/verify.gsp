<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="register.verify.title" default="Confirm registration"/></title>
</head>

<body>

<g:content tag="hero">
    <div class="hero-unit">
        <h1><g:message code="register.verify.title"/></h1>
    </div>
</g:content>

<tt:html name="register-verify-main">

    <div class="row-fluid">

        <div class="span5">
            <p>&nbsp;</p>
        </div>

        <div class="span7">
            <p>
                An email has been sent to <strong>${user.email?.encodeAsHTML()}</strong> with a confirmation link.
            </p>

            <p>
                You have 24 hours to click the link to confirm your registration.
            </p>
        </div>

    </div>

</tt:html>

</body>
</html>

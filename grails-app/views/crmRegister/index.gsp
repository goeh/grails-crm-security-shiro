<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="register.title" default="Sign up"/></title>
</head>

<body>

<g:content tag="hero">
    <div class="hero-unit">
        <h1><g:message code="register.title" default="Please Register"/></h1>
    </div>
</g:content>

<div class="row-fluid">

    <div class="span4">
        <tt:html name="register-index-left"/>
    </div>

    <div class="span4">

        <g:hasErrors bean="${cmd}">
            <crm:alert class="alert-error">
                <ul>
                    <g:eachError bean="${cmd}" var="error">
                        <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                                error="${error}"/></li>
                    </g:eachError>
                </ul>
            </crm:alert>
        </g:hasErrors>

        <g:form useToken="true">

            <f:with bean="${cmd}">
                <f:field property="username" label="register.username.label">
                    <g:textField name="username" value="${cmd.username}" autofocus="" required=""
                                 placeholder="${message(code:'register.username.placeholder')}"/>
                </f:field>
                <f:field property="name" label="register.name.label">
                    <g:textField name="name" value="${cmd.name}" required=""
                                 placeholder="${message(code:'register.name.placeholder')}"/>
                </f:field>
                <f:field property="email" label="register.email.label">
                    <input type="email" name="email" id="email" value="${cmd.email?.encodeAsHTML()}"
                            required="" placeholder="${message(code: 'register.email.placeholder')}"/>
                </f:field>
                <f:field property="password" label="register.password.label">
                    <g:passwordField name="password" value="${cmd.password}" required=""
                                     placeholder="${message(code:'register.password.placeholder')}"/>
                </f:field>
                <f:field property="telephone" label="register.telephone.label">
                    <g:textField name="telephone" value="${cmd.telephone}"
                                 placeholder="${message(code:'register.telephone.placeholder')}"/>
                </f:field>
                <f:field property="postalCode" label="register.postalCode.label">
                    <g:textField name="postalCode" value="${cmd.postalCode}" required=""
                                 placeholder="${message(code:'register.postalCode.placeholder')}"/>
                </f:field>
                <f:field property="country" label="register.country.label">
                    <g:countrySelect name="country" value="${cmd.country}" noSelection="[null:'']"/>
                </f:field>

                <f:field property="campaign" label="register.campaign.label">
                    <g:textField name="campaign" value="${cmd.campaign}"
                                 placeholder="${message(code:'register.campaign.placeholder')}"/>
                </f:field>
            </f:with>

            <div class="control-group ${hasErrors(bean: cmd, field: 'captcha', 'error')}">
                <label class="control-label" for="captcha"><g:message code="register.captcha.label" default="Enter Security Code"/></label>

                <div class="controls">
                    <g:textField name="captcha" size="8" title="${message(code:'register.captcha.title', default:'')}" required=""/>
                    <img src="${createLink(controller: 'simpleCaptcha', action: 'captcha')}"/>
                </div>
            </div>

            <div class="form-actions">
                <crm:button visual="primary" icon="icon-ok icon-white" label="register.button.create.label"/>
            </div>

        </g:form>

    </div>

    <div class="span4">
        <tt:html name="register-index-right"/>
    </div>

</div>

</body>
</html>

<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'shiroCrmUser.label', default: 'User')}"/>
    <title><g:message code="shiroCrmUser.show.title" args="[entityName, shiroCrmUser]"/></title>
    <style type="text/css">
    li.perm {
        font-weight: bold;
    }
    </style>
</head>

<body>

<div class="row-fluid">
    <div class="span9">

        <header class="page-header clearfix">
            <crm:user>
                <h1 class="pull-left">
                    ${shiroCrmUser.name.encodeAsHTML()}
                    <small>${shiroCrmUser.username?.encodeAsHTML()}</small>
                    ${shiroCrmUser.enabled ? '' : '<i class="icon-ban-circle"></i>'}
                </h1>
            </crm:user>
        </header>

        <div class="tabbable">
            <ul class="nav nav-tabs">
                <li class="active"><a href="#main" data-toggle="tab"><g:message code="shiroCrmUser.tab.main.label"/></a>
                </li>

                <g:each in="${tenantList}" var="tenant">
                    <li><a href="#t${tenant.id}" data-toggle="tab">${tenant.name.encodeAsHTML()}</a></li>
                </g:each>

                <crm:pluginViews location="tabs" var="view">
                    <li><a href="#${view.id}" data-toggle="tab">${view.label.encodeAsHTML()}</a></li>
                </crm:pluginViews>
            </ul>

            <div class="tab-content">
                <div class="tab-pane active" id="main">
                    <div class="row-fluid">
                        <div class="span4">
                            <dl>
                                <dt><g:message code="shiroCrmUser.username.label" default="Username"/></dt>
                                <dd><g:fieldValue bean="${shiroCrmUser}" field="username"/></dd>

                                <dt><g:message code="shiroCrmUser.name.label" default="Name"/></dt>
                                <dd><g:fieldValue bean="${shiroCrmUser}" field="name"/></dd>

                                <dt><g:message code="shiroCrmUser.email.label" default="Email"/></dt>
                                <dd><g:fieldValue bean="${shiroCrmUser}" field="email"/></dd>
                            </dl>
                        </div>

                        <div class="span4">
                            <dl>
                                <g:if test="${shiroCrmUser.address1}">
                                    <dt><g:message code="shiroCrmUser.address1.label" default="Address 1"/></dt>
                                    <dd>${shiroCrmUser.address1}</dd>
                                </g:if>
                                <g:if test="${shiroCrmUser.address2}">
                                    <dt><g:message code="shiroCrmUser.address2.label" default="Address 2"/></dt>
                                    <dd>${shiroCrmUser.address1}</dd>
                                </g:if>
                                <g:if test="${shiroCrmUser.address3}">
                                    <dt><g:message code="shiroCrmUser.address3.label" default="Address 3"/></dt>
                                    <dd>${shiroCrmUser.address3}</dd>
                                </g:if>
                                <g:if test="${shiroCrmUser.postalCode}">
                                    <dt><g:message code="shiroCrmUser.postalCode.label" default="Postal code"/></dt>
                                    <dd>${shiroCrmUser.postalCode} ${shiroCrmUser.city}</dd>
                                </g:if>
                                <g:if test="${shiroCrmUser.telephone}">
                                    <dt><g:message code="shiroCrmUser.telephone.label" default="Telephone"/></dt>
                                    <dd>${shiroCrmUser.telephone}</dd>
                                </g:if>
                                <g:if test="${shiroCrmUser.mobile}">
                                    <dt><g:message code="shiroCrmUser.mobile.label" default="Mobile"/></dt>
                                    <dd>${shiroCrmUser.mobile}</dd>
                                </g:if>
                            </dl>
                        </div>

                        <div class="span4">
                            <dl>
                                <g:if test="${shiroCrmUser.campaign}">
                                    <dt><g:message code="shiroCrmUser.campaign.label" default="Campaign"/></dt>
                                    <dd>${shiroCrmUser.campaign}</dd>
                                </g:if>
                            </dl>
                        </div>

                    </div>

                    <g:form>
                        <g:hiddenField name="id" value="${shiroCrmUser?.id}"/>
                        <div class="form-actions btn-toolbar">
                            <crm:button type="link" group="true" action="edit" id="${shiroCrmUser?.id}" visual="primary"
                                        icon="icon-pencil icon-white"
                                        label="shiroCrmUser.button.edit.label" permission="shiroCrmUser:edit"/>
                        </div>
                    </g:form>

                </div>

                <g:each in="${tenantList}" var="tenant">
                    <div class="tab-pane" id="t${tenant.id}">
                        <div class="row-fluid">
                            <div class="span4">
                                <h4>Vyuppgifter</h4>
                                <dl>
                                    <dt>Id</dt>
                                    <dd>${tenant.id}</dd>
                                    <dt>Vynamn</dt>
                                    <dd>${tenant.name.encodeAsHTML()}</dd>
                                    <dt>Funktioner</dt>
                                    <dd>${tenant.features?.join(', ')}</dd>
                                    <g:if test="${tenant.user.id != shiroCrmUser.id}">
                                        <dt>Ägare</dt>
                                        <dd><g:link action="show"
                                                    id="${tenant.user.id}">${tenant.user.name.encodeAsHTML()} (${tenant.user.username})</g:link>
                                        </dd>
                                    </g:if>
                                    <g:if test="${tenant.parent}">
                                        <dt>Ingår i</dt>
                                        <dd>${tenant.parent}</dd>
                                    </g:if>
                                    <dt>Skapad</dt>
                                    <dd><g:formatDate date="${tenant.dateCreated}" type="date"/></dd>
                                    <dt>Löper ut</dt>
                                    <dd><g:if test="${tenant.expires}"><g:formatDate date="${tenant.expires}"
                                                                                     type="date"/></g:if><g:else>Aldrig</g:else></dd>
                                </dl>
                            </div>

                            <div class="span4">
                                <h4>Funktioner</h4>
                                <dl>
                                    <crm:eachFeature tenant="${tenant.id}" var="feature">
                                        <dt>${feature.name.encodeAsHTML()}</dt>
                                        <dd class="${feature.enabled ? 'enabled' : 'disabled'}">
                                            ${feature.description?.encodeAsHTML()}
                                            ${feature.enabled ? '' : '(disabled)'}
                                            <g:formatDate date="${feature.expires}"/>
                                            <span style="background-color:yellow;">${feature.dump()}</span>
                                        </dd>
                                    </crm:eachFeature>
                                </dl>

                                <h4>Parametrar</h4>
                                <dl>
                                    <g:each in="${tenant.options}" var="o">
                                        <dt>${o.key}</dt>
                                        <dd>${o.value}</dd>
                                    </g:each>
                                </dl>

                            </div>

                            <div class="span4">

                                <h4>Roller</h4>
                                <dl>
                                    <crm:userRoles tenant="${tenant.id}"
                                                         username="${shiroCrmUser.username}">
                                        <dt>${it.role.encodeAsHTML()} <g:formatDate date="${it.expires}"/></dt>
                                        <dd>
                                            <ul>
                                            <g:each in="${it.permissions.sort()}" var="perm">
                                                <li>${perm}</li>
                                            </g:each>
                                            </ul>
                                        </dd>
                                    </crm:userRoles>
                                </dl>

                                <h4>Individuella behörigheter</h4>
                                <ul>
                                    <crm:userPermissions tenant="${tenant.id}"
                                                         username="${shiroCrmUser.username}">
                                        <li>${it.encodeAsHTML()}</li>
                                    </crm:userPermissions>
                                </ul>

                            </div>
                        </div>

                        <div class="form-actions">
                            <g:form>
                                <input type="hidden" name="id" value="${shiroCrmUser.id}"/>
                                <input type="hidden" name="tenantId" value="${tenant.id}"/>
                                <crm:button action="reset" visual="danger" label="Återställ behörigheter"
                                            icon="icon-refresh icon-white"
                                            confirm="Är du säker på att du vill återställa behörigheterna?"/>
                            </g:form>
                        </div>
                    </div>
                </g:each>
            </div>
        </div>

    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>

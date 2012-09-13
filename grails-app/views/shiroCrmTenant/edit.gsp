<%@ page import="grails.plugins.crm.security.shiro.ShiroCrmUserPermission; grails.plugins.crm.security.shiro.ShiroCrmUserRole" %>
<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'shiroCrmTenant.label', default: 'Account')}"/>
    <title><g:message code="shiroCrmTenant.edit.label" args="[entityName]"/></title>
    <r:script>
    $(document).ready(function() {
        $(".statistics").each(function(idx) {
            var feature = $(this).data("crm-feature");
            $(this).load("${createLink(controller: 'crmFeature', action: 'statistics')}", {id:"${shiroCrmTenant.id}", name:feature, template:"badge"});
        });
    });
    </r:script>
</head>

<body>

<crm:header title="shiroCrmTenant.edit.title" subtitle="${shiroCrmTenant.name.encodeAsHTML()}"
            args="[entityName, shiroCrmTenant]"/>

<div class="tabbable">

<ul class="nav nav-tabs">
    <li class="active"><a href="#main" data-toggle="tab">Vyuppgifter</a></li>
    <li><a href="#features" data-toggle="tab">Funktioner<crm:countIndicator
            count="${features.findAll{!it.hidden}.size()}"/></a></li>
    <li><a href="#perm" data-toggle="tab">Behörigheter<crm:countIndicator
            count="${permissions.size() + invitationList.size()}"/></a></li>
</ul>

<div class="tab-content">
<div id="main" class="tab-pane active">
    <g:form class="form-horizontal" action="edit" id="${shiroCrmTenant.id}">
        <g:hiddenField name="version" value="${shiroCrmTenant.version}"/>

        <g:hasErrors bean="${shiroCrmTenant}">
            <crm:alert class="alert-error">
                <ul>
                    <g:eachError bean="${shiroCrmTenant}" var="error">
                        <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message
                                error="${error}"/></li>
                    </g:eachError>
                </ul>
            </crm:alert>
        </g:hasErrors>

        <div class="row-fluid">
            <f:with bean="shiroCrmTenant">

                <div class="span7">

                    <f:field property="name" input-autofocus=""/>

                    <div class="control-group">
                        <label class="control-label">Startvy efter inloggning</label>

                        <div class="controls">
                            <label class="checkbox inline">
                                <g:radio value="true" name="defaultTenant"
                                         checked="${shiroCrmTenant.id == user.defaultTenant}"/>
                                Ja
                            </label>
                            <label class="checkbox inline">
                                <g:radio value="false" name="defaultTenant"
                                         checked="${shiroCrmTenant.id != user.defaultTenant}"/>
                                Nej
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">Använd kostnader i avtalsbilden</label>

                        <div class="controls">
                            <label class="checkbox inline">
                                <g:radio value="true" name="showCosts"
                                         checked="${showCosts}"/>
                                Ja
                            </label>
                            <label class="checkbox inline">
                                <g:radio value="false" name="showCosts"
                                         checked="${!showCosts}"/>
                                Nej
                            </label>
                        </div>
                    </div>

                    <div class="control-group">
                        <label class="control-label">Antal parter för avtal</label>

                        <div class="controls">
                            <label class="checkbox">
                                <g:radio value="false" name="partner2"
                                         checked="${!partner2}"/>
                                Använd en avtalspart
                            </label>
                            <label class="checkbox">
                                <g:radio value="true" name="partner2"
                                         checked="${partner2}"/>
                                Använd två avtalsparter
                            </label>
                        </div>
                    </div>

                </div>

                <div class="span5">

                    <f:field property="user">
                        <g:textField name="user" value="${shiroCrmTenant.user.name}" disabled="disabled"
                                     class="span6"/>
                    </f:field>

                    <f:field property="dateCreated">
                        <g:textField name="dateCreated"
                                     value="${formatDate(date:shiroCrmTenant.dateCreated, type:'date')}"
                                     disabled="disabled"
                                     class="span6"/>
                    </f:field>

                    <f:field property="expires">
                        <g:textField name="expires"
                                     value="${formatDate(date:shiroCrmTenant.expires, type:'date')}"
                                     disabled="disabled"
                                     class="span6"/>
                        <g:if test="${shiroCrmTenant.expires}">
                            <g:set var="today" value="${new Date()}"/>
                            <div>
                                <g:if test="${shiroCrmTenant.expires >= today}">
                                    (<g:message code="default.days.left.message"
                                                args="${[shiroCrmTenant.expires - today]}"
                                                default="{0} days left"/>)
                                </g:if>
                                <g:else>
                                    (<g:message code="account.ended.message" default="Closed"/>)
                                </g:else>
                            </div>
                        </g:if>
                    </f:field>

                </div>

            </f:with>

        </div>

        <div class="form-actions">
            <crm:button visual="primary" icon="icon-ok icon-white" label="shiroCrmTenant.button.update.label"
                        permission="shiroCrmTenant:update"/>
            <crm:button action="delete" visual="danger" icon="icon-trash icon-white"
                        label="shiroCrmTenant.button.delete.label" permission="shiroCrmTenant:delete"
                        confirm="shiroCrmTenant.button.delete.confirm.message"/>
            <crm:button type="link" icon="icon-remove" label="shiroCrmTenant.button.cancel.label"
                        action="index"/>
        </div>

    </g:form>
</div>

<div id="features" class="tab-pane">

    <div class="row-fluid">
        <div class="span6">

            <h2>Installerade funktioner</h2>

            <g:each in="${features.findAll{!it.hidden}}" var="f">
                <div class="well well-small clearfix ${f.enabled ? 'enabled' : 'disabled'}">
                    <%--
                    <img src="${resource(dir: 'images/avtala', file: 'arrow-e-y.png')}" width="32" height="32"
                         class="pull-left" style="margin-right:10px;"/>
                    --%>
                    <h3>
                        ${message(code: 'feature.' + f.name + '.label', default: f.name)}
                        <span class="statistics" data-crm-feature="${f.name}"></span>
                    </h3>

                    <div>${message(code: 'feature.' + f.name + '.description', default: f.description)}</div>

                    <a href="${createLink(controller: 'crmFeature', action: 'info', params: [id: shiroCrmTenant.id, name: f.name])}"
                       data-toggle="modal" data-target="#modal-feature-info">Läs mer...</a>

                    <g:unless test="${f.required}">
                        <crm:hasPermission permission="crmFeature:install:${shiroCrmTenant.id}">
                            <g:link controller="crmFeature" action="uninstall"
                                    params="${[id:shiroCrmTenant.id, name:f.name]}" class="pull-right">
                                <i class="icon icon-trash"></i>
                            </g:link>
                        </crm:hasPermission>
                    </g:unless>
                </div>
            </g:each>
        </div>

        <div class="span6">

            <h2>Ej installerade funktioner</h2>

            <g:each in="${moreFeatures}" var="f">
                <div class="well well-small clearfix">
                    <%--
                    <img src="${resource(dir: 'images/avtala', file: 'arrow-e-y.png')}" width="32" height="32"
                         class="pull-left" style="margin-right:10px;"/>
                    --%>
                    <h3>${message(code: 'feature.' + f.name + '.label', default: f.name)}</h3>

                    <div>${message(code: 'feature.' + f.name + '.description', default: f.description)}</div>

                    <g:link controller="crmFeature" action="info"
                            params="${[id:shiroCrmTenant.id, name:f.name]}">Läs mer...</g:link>

                    <crm:button type="link" controller="crmFeature" action="install"
                                params="${[id:shiroCrmTenant.id, name:f.name]}"
                                visual="success btn-mini" icon="icon-download icon-white" class="pull-right"
                                label="Installera" permission="crmFeature:install:${shiroCrmTenant.id}"/>

                </div>
            </g:each>

            <div class="alert alert-info">
                <p><span class="badge badge-success">543</span> Grön etikett betyder att funktionen används regelbundet.
                </p>

                <p><span class="badge badge-warning">21</span> Gul etikett betyder att funktionen används mer sällan.
                </p>

                <p><span class="badge badge-important">0</span> Röd etikett betyder att funktionen inte används alls.</p>

                <p><span class="badge">?</span> Grå etikett betyder att användningen inte kan mätas med precision.</p>

                <p>Siffran i etiketten anger hur många objekt (t.ex. företag eller avtal) som för närvarande hanteras av funktionen i den aktuella vyn.</p>
            </div>
        </div>
    </div>

</div>

<div id="perm" class="tab-pane">
    <table class="table table-bordered">
        <thead>
        <tr>
            <th>Användare</th>
            <th>Roll</th>
            <th>Behörigheter</th>
            <th>Gäller t.o.m</th>
            <th>Status</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${permissions}" var="perm">
            <tr>
                <td>${perm.user.name}</td>
                <g:if test="${perm instanceof ShiroCrmUserRole}">
                    <td>${message(code: 'role.' + perm.toString() + '.label', default: perm.toString())}</td>
                    <td>
                        <crm:permissionList permission="${perm.role.permissions}">
                            ${it.label}<br/>
                        </crm:permissionList>
                    </td>
                    <td><g:formatDate type="date" date="${perm.expires}"/></td>
                    <td>${perm.expires == null || perm.expires > new Date() ? 'Aktiv' : 'Inaktiv'}</td>
                </g:if>
                <g:if test="${perm instanceof ShiroCrmUserPermission}">
                    <td></td>
                    <td>
                        <crm:permissionList permission="${perm.permissionsString}">
                            ${it.label}<br/>
                        </crm:permissionList>
                    </td>
                    <td></td>
                    <td>Aktiv</td>
                </g:if>
            </tr>
        </g:each>

        <g:each in="${invitationList}" var="inv">
            <tr>
                <td>${inv.receiver?.encodeAsHTML()}</td>
                <td>${inv.param?.encodeAsHTML()}</td>
                <td></td>
                <td><g:formatDate format="yyyy-MM-dd" date="${inv.dateCreated}"/></td>
                <td>Inbjudan skickad</td>
            </tr>
        </g:each>
        </tbody>
    </table>

    <div class="form-actions">
        <crm:button type="url" visual="primary" icon="icon-share-alt icon-white" data-toggle="modal"
                    href="#modal-share-account"
                    label="account.button.share.label" permission="crmInvitation:share"/>
    </div>
</div>

</div>
</div>

<div id="modal-delete-account" class="modal hide">
    <div class="modal-header">
        <a href="#" class="close" data-dismiss="modal">&times;</a>

        <h3><g:message code="shiroCrmTenant.delete.confirm.title" args="${[shiroCrmTenant.name]}"/></h3>
    </div>

    <div class="modal-body">
        <p><g:message code="shiroCrmTenant.delete.confirm.message" args="${[shiroCrmTenant.name]}"/></p>
    </div>

    <div class="modal-footer">
        <g:form action="delete">
            <input type="hidden" name="id" value="${shiroCrmTenant.id}"/>

            <crm:button type="url" visual="primary" icon="icon-remove icon-white" href="#"
                        label="shiroCrmTenant.delete.confirm.no" args="${[shiroCrmTenant.name]}"
                        data-dismiss="modal"/>
            <crm:button visual="danger" icon="icon-trash icon-white" action="delete"
                        label="shiroCrmTenant.delete.confirm.yes" args="${[shiroCrmTenant.name]}"/>
        </g:form>
    </div>
</div>

<div id="modal-share-account" class="modal hide">
    <div class="modal-header">
        <a href="#" class="close" data-dismiss="modal">&times;</a>

        <h3><g:message code="account.share.title" args="${[shiroCrmTenant.name]}"/></h3>
    </div>

    <div class="well">
        <g:form action="share">
            <input type="hidden" name="id" value="${shiroCrmTenant.id}"/>

            <div class="modal-body">
                <p><g:message code="account.share.message" args="${[shiroCrmTenant.name]}"/></p>
            </div>

            <div class="control-group">
                <label class="control-label">E-postadress</label>

                <div class="controls">
                    <input type="email" name="email" class="span4"
                           placeholder="E-postadress till den du vill bjuda in..."/>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">Behörighet</label>

                <div class="controls">
                    <label class="radio inline"><g:radio value="guest" name="role" checked="checked"/>Läsa</label>
                    <label class="radio inline"><g:radio value="user" name="role"/>Ändra</label>
                    <label class="radio inline"><g:radio value="admin" name="role"/>Administrera</label>
                </div>
            </div>

            <div class="control-group">
                <label class="control-label">Meddelande (frivilligt)</label>

                <div class="controls">
                    <textarea name="msg" placeholder="Personligt meddelande..." class="span5" cols="40"
                              rows="3"></textarea>
                </div>
            </div>

            <div class="modal-footer">

                <crm:button visual="primary" icon="icon-ok icon-white" action="share"
                            label="account.button.share.confirm.yes" args="${[shiroCrmTenant.name]}"/>
                <crm:button type="url" icon="icon-remove" href="#"
                            label="account.button.share.confirm.no" args="${[shiroCrmTenant.name]}"
                            data-dismiss="modal"/>
            </div>
        </g:form>
    </div>
</div>

<div id="modal-feature-info" class="modal hide">

    <div class="modal-header">
        <a class="close" data-dismiss="modal">×</a>

        <h3><g:message code="crmFeature.info.title" default="Information"/></h3>
    </div>

    <div class="modal-body"></div>

    <div class="modal-footer">
        <a href="#" class="btn btn-primary" data-dismiss="modal"><i class="icon-ok icon-white"></i> <g:message
                code="default.button.close.label" default="Close"/></a>
    </div>

</div>

</body>
</html>


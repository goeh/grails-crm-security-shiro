<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>

<g:set var="today" value="${new Date()}"/>

<table class="table table-striped">
    <thead>
    <tr>

        <th style="width:16px;"></th>
        <th><g:message code="shiroCrmTenant.name.label"/></th>
        <th><g:message code="shiroCrmTenant.user.label"/></th>
        <th><g:message code="shiroCrmTenant.locale.label"/></th>
        <th><g:message code="shiroCrmTenant.dateCreated.label"/></th>
        <th><g:message code="shiroCrmTenant.expires.label"/></th>
        <th style="width:16px;"></th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${result}" var="m">
        <tr class="${m.current ? 'active' : ''}">

            <td style="width:16px;">
                <g:if test="${m.id == user.defaultTenant}"><i class="icon-step-forward"></i></g:if>
            </td>

            <td>
                <g:if test="${!m.current}">
                    <g:link action="activate" params="${[id:m.id, referer:createLink(action:'index')]}"
                            title="${message(code:'shiroCrmTenant.activate.label', default:'Switch to this view')}">
                        ${m.name}
                    </g:link>
                </g:if>
                <g:else>
                    ${m.name}
                </g:else>
            </td>

            <td>
                ${m.user.name}
            </td>

            <td>
                ${m.locale.getDisplayName(request.locale ?: Locale.default)}
            </td>

            <td>
                <g:formatDate type="date" date="${m.dateCreated}"/>
            </td>

            <td>
                <g:formatDate type="date" date="${m.expires}"/>
                <g:if test="${m.expires}">
                    <g:if test="${m.expires >= today}">
                        (<g:message code="default.days.left.message" args="${[m.expires - today]}"
                                    default="{0} days left"/>)
                    </g:if>
                    <g:else>
                        <span class="label label-important"><g:message code="shiroCrmTenant.expires.expired" default="Closed"/></span>
                    </g:else>
                </g:if>
                <g:else>
                    <g:message code="shiroCrmTenant.expires.never" default="Never"/>
                </g:else>
            </td>

            <td style="width:16px;">
                <g:if test="${m.my}"><g:link action="edit" id="${m.id}"><i class="icon-pencil"></i></g:link></g:if>
            </td>

        </tr>
    </g:each>
    </tbody>
</table>

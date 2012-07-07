<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main">
    <g:set var="entityName" value="${message(code: 'shiroCrmNamedPermission.label', default: 'CrmTaskType')}"/>
    <title><g:message code="shiroCrmNamedPermission.list.title" args="[entityName]"/></title>
    <r:script>
        $(document).ready(function() {
            $(".table-striped tr").hover(function() {
                $("i", $(this)).removeClass('hide');
            }, function() {
                $("i", $(this)).addClass('hide');
            });
        });
    </r:script>
</head>

<body>

<crm:header title="shiroCrmNamedPermission.list.title" args="[entityName]"/>

<div class="row-fluid">
    <div class="span9">

        <table class="table table-striped">
            <thead>
            <tr>

                <g:sortableColumn property="name"
                                  title="${message(code: 'shiroCrmNamedPermission.name.label', default: 'Name')}"/>
            </tr>
            </thead>
            <tbody>
            <g:each in="${result}" var="shiroCrmNamedPermission">
                <tr>
                    <td>
                        <g:link action="edit" id="${shiroCrmNamedPermission.id}">
                            ${fieldValue(bean: shiroCrmNamedPermission, field: "name")}
                        </g:link>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

        <crm:paginate total="${totalCount}"/>

    </div>

    <div class="span3">
        <crm:submenu/>
    </div>
</div>

</body>
</html>

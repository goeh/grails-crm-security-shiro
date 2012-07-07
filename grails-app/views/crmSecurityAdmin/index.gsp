<!DOCTYPE html>
<html lang="en">
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="crmSecurityAdmin.title" default="Security Administration"/></title>
</head>

<body>

<div class="row-fluid">

    <div class="span9">
        <table class="table table-striped">
            <thead>
            <tr>
                <th colspan="2"><g:message code="shiroCrmNamedPermission.name.label" default="Named Permissions"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${namedPermissions}" var="np">
                <tr>
                    <td>
                        <g:link action="edit" id="${np.id}">
                            ${fieldValue(bean: np, field: "name")}
                        </g:link>
                    </td>
                    <td>
                        ${np.permissions?.join(', ')}
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="span3">
    </div>
</div>

</body>
</html>

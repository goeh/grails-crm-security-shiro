<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Grails CRM - Create new Account</title>
    <r:script type="text/javascript">

        function listTenants() {
            $.get("${createLink(controller: 'crmUser', action: 'listTenants')}", function(data) {
                if (data.success) {
                    var list = data.list;
                    var $wrap = $('<div>').attr('id', 'accountsWrap');
                    var $tbl = $('<table>').attr('id', 'accountsTable').addClass('zebra-striped');
                    var $hd = $('<thead>').append($('<tr><th>Account Name</th><th>Created</th><th>Operation</th></tr>'));
                    var $bdy = $('<tbody>');
                    var current = 'No account selected';

                    for (var i = 0; i < list.length; i++) {
                        var a = list[i];
                        var $currBtn = $('<a href="${createLink(controller:'crmUser', action:'changeTenant')}/' + a.id + '">').text('Select').addClass('btn primary').css('margin-right', '4px')
                        var $editBtn = $('<input type="submit" value="Edit" data-crm-id="' + a.id + '"/>').addClass('btn success').css('margin-right', '4px');
                        var $delBtn = $('<input type="submit" value="Delete" data-crm-id="' + a.id + '"/>').addClass('btn danger delete').click(function(event) {
                            event.stopPropagation();
                            $("#delete-account-id").val($(this).data('crm-id'));
                            $('#modal-delete-account').modal('show');
                            return false;
                        });
                        var $txtTd = $('<td>');
                        var $link = $('<a href="${createLink(controller:'crmUser', action:'changeTenant')}/' + a.id + '">').text(a.name)
                        if(a.current) {
                            $link.addClass('green');
                            current = a.name;
                        }

                        $bdy.append($('<tr>').append($('<td>').append($link), $('<td>').text(a.created), $('<td>').append($currBtn).append($editBtn).append($delBtn)));
                    }
                    
                    $tbl.append($hd);
                    $tbl.append($bdy);
                    $wrap.append($tbl);
                    $('#accountList').empty();
                    $('#accountList').append($wrap);

                    $("#current-account").text(current);
                } else {
                    $("#accountList").html("<p>" + data.error.message + "</p>");
                    $("#current-account").text("");
                }
            }, 'json');
        }

        $(document).ready(function() {

            // Add submit handler to the create-new-account form.
            $("#create-account-form").submit(function(event) {
                event.stopPropagation();
                var form = $(this);
                $.post("${createLink(controller: 'crmUser', action: 'createTenant')}", form.serialize(), function(data) {
                    $("input[name='name']", form).val("");
                    listTenants();
                }, "json");
                return false;
            });

            // Add submit handler to the delete-account-form.
            $("#delete-account-form").submit(function(event) {
                event.stopPropagation();
                $("#modal-delete-account").modal('hide');
                var form = $(this);
                $.post("${createLink(controller: 'crmUser', action: 'deleteTenant')}", form.serialize(), function(data) {
                    listTenants();
                }, "json");
                return false;
            });

            // Setup modal dialogs.
            $('.modal').modal({backdrop:'static'});
            $('.modal').bind('shown', function() {
                $('.cancel', $(this)).focus(); // Make sure cancel button get focus when modal is shown.
            });
            $('.modal .cancel').click(function(event) {
                event.stopPropagation();
                $(this).closest('.modal').modal('hide'); // Close modal dialog when cancel button is clicked.
                return false;
            });
            
            // Load list of accounts when we enter the page.
            listTenants();
        });
    </r:script>
</head>

<body>

<div class="page-header">
    <h1>My Accounts <span id="current-account" style="float:right;"></span></h1>
</div>

<crm:user>
    <g:form action="createTenant" id="create-account-form">
        <fieldset>
            <div class="clearfix">
                <label for="name">Create New Account</label>

                <div class="input">
                    <g:textField class="xlarge default-text" name="name" size="40" maxlength="40" value=""
                                 title="New Account Name..."/>
                    <input type="submit" class="btn primary" value="Create"/>
                </div>
            </div>
        </fieldset>
    </g:form>
</crm:user>

<div id="accountList"></div>

<div id="modal-delete-account" class="modal hide fade" style="display:none;">
    <div class="modal-header">
        <a href="#" class="close">&times;</a>

        <h3>Confirm delete account</h3>
    </div>

    <div class="modal-body">
        <p>Are you sure you was to completely delete account <span id="delete-account-name">XXX</span>?</p>

        <p>Deleting an account removes all information associated with the account</p>

        <p>Deleting an account cannot be undone.</p>
    </div>

    <div class="modal-footer">
        <form id="delete-account-form">
            <input type="hidden" id="delete-account-id" name="id" value=""/>
            <input type="submit" class="btn primary cancel" value="No way!"/>
            <input type="submit" class="btn danger secondary" value="Yes, please remove it"/>
        </form>
    </div>
</div>
</body>
</html>

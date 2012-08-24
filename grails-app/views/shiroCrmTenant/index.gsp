<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <g:set var="entityName" value="${message(code: 'shiroCrmTenant.label', default: 'Account')}"/>
    <title><g:message code="shiroCrmTenant.index.title" subtitle="shiroCrmTenant.index.subtitle" args="[entityName, shiroCrmUser.name]" default="My Accounts"/></title>
</head>

<body>

<crm:header title="shiroCrmTenant.index.title" subtitle="shiroCrmTenant.index.subtitle" args="[entityName, shiroCrmUser.name]"/>

<g:if test="${shiroCrmTenantList}">
    <g:render template="list" model="[user:shiroCrmUser, result:shiroCrmTenantList]"/>
</g:if>

<div class="form-actions">
   <crm:button type="link" action="create" visual="success" icon="icon-file icon-white"
               label="shiroCrmTenant.button.create.label" permission="shiroCrmTenant:create"/>
</div>

<tt:html name="account-index">
<div class="row-fluid">

    <div class="span6">
        <div class="alert alert-info">
            <h4>Vad är en vy?</h4>

            <p>En vy är som en egen isolerad databas där du samlar information om avtal, leverantörer och dokument.</p>

            <p>Du kan skapa flera vyer om du till exempel vill skilja på dina privata avtal och firmans avtal,
            eller om du använder Avtala.se på ett större företag med verksamhet på flera orter.
            Då kan du skapa flera vyer för att hålla isär informationen på ett smidigt sätt.</p>
        </div>
    </div>

    <div class="span6">
        <div class="alert alert-error">
            <h4>Vad kostar det?</h4>

            <p>Alla vytyper har har en <strong>kostnadsfri testperiod om 30 dagar</strong>.
            Du kan när som helst under testperioden radera din vy om du känner att du inte har behov av funktionerna.
            </p>

            <p>För att kunna fortsätta använda din vy efter testperioden måste du beställa ett abonnemang.
            Vissa funktioner är dock kostnadsfria för privat bruk.
            Se menyn <strong>Favoriter &raquo; Priser</strong> för information om de olika abonnemangen.</p>
        </div>
    </div>

</div>
</tt:html>

</body>
</html>

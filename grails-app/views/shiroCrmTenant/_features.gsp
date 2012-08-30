<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>

<g:each in="${allFeatures.findAll{!it.hidden}.sort{it.name}}" var="f">
    <div class="well well-small clearfix ${f.enabled ? 'enabled' : 'disabled'}${f.required ? ' feature-required' : ''}">

        <h4>
            <g:if test="${f.required}">
                <input type="hidden" id="feature-${f.name}-required" name="features" value="${f.name}"/>
                <g:checkBox id="feature-${f.name}-checkbox" name="feature" value="${f.name}" disabled="disabled"
                            checked="${true}"/>
            </g:if>
            <g:else>
                <g:checkBox id="feature-${f.name}-checkbox" name="features" value="${f.name}"
                            checked="${f.required || features?.contains(f.name)}"/>
            </g:else>

            ${message(code: 'feature.' + f.name + '.label', default: f.name)}
        </h4>

        <div>${message(code: 'feature.' + f.name + '.description', default: f.description)}</div>

        <a href="${createLink(controller: 'crmFeature', action: 'info', params: [id: shiroCrmTenant.id, name: f.name])}"
           data-toggle="modal" data-target="#modal-feature-info">Läs mer...</a>

    </div>
</g:each>

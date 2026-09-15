<#import "template.ftl" as layout>
<@layout.registrationLayout displayMessage=true; section>
    <#if section = "header">
        ${msg("reviewConfirmTitle")}
    <#elseif section = "form">
        <form id="kc-review-confirm-form" class="${properties.kcFormClass!}" action="${url.loginAction}" method="post">
            <p>${msg("reviewConfirmInstruction")}</p>

            <input type="hidden" name="accepted" value="true"/>

            <div class="${properties.kcFormGroupClass!}">
                <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                    <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}"
                           type="submit" value="${msg("doContinue")}"/>
                </div>
            </div>
        </form>
    </#if>
</@layout.registrationLayout>

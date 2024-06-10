<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                Your Change Order document for ${taskDetailsDto.inputParam.CUSTOMER_BUSINESS_NAME} is in the approval stage. Once you have received
                and signed the Change Order please upload the signed document to the wholesale order <a href="${taskDetailsDto.inputParam.WS_CSM_ORDER_LINK}" class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.WS_ORDER_ID}</a>.
            </div>
        </@row>
    </@section>
</div>

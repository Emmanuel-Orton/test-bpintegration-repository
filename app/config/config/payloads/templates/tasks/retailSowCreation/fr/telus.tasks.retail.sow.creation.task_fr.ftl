<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info_fr.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">The Wholesale SOW for a(n) ${taskDetailsDto.inputParam.PRODUCT_NAME}
                order has been reviewed for the draft order
                <a href="${taskDetailsDto.inputParam.DRAFT_ORDER_LINK}" class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.DRAFT_ORDER_ID}</a>.
                Please create the Retail Statement of Work and send it to the sales agent <a href = "mailto: ${taskDetailsDto.inputParam.OPERATOR_EMAIL}">${taskDetailsDto.inputParam.OPERATOR_EMAIL}</a>.
            </div>
        </@row>
    </@section>
</div>
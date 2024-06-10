<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info_fr.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                The Wholesale Change Order document for ${taskDetailsDto.inputParam.CUSTOMER_BUSINESS_NAME} has been reviewed. Please refer to the attachment on
                wholesale order <a href="${taskDetailsDto.inputParam.WS_ORDER_LINK}" class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.WS_ORDER_ID}</a>
                to assist with drafting the retail Change Order and send it to the sales agent ${taskDetailsDto.inputParam.OPERATOR_EMAIL} to initiate
                the retail contract amendment. Once the signed retail document is attached to the customer profile, please close the task.
            </div>
        </@row>
    </@section>
</div>
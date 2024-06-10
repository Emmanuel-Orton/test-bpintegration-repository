<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                A DAF for a(n) ${taskDetailsDto.inputParam.PRODUCT_NAME} wholesale order <a
                        href="${taskDetailsDto.inputParam.WS_CSM_ORDER_LINK}"
                        class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.WS_ORDER_ID}</a> has
                been reviewed and is
                ready to be sent to the customer ${taskDetailsDto.inputParam.CUSTOMER_BUSINESS_NAME} for approval.
            </div>
            <daf-approve-task-date-select task="task"/>
        </@row>
    </@section>
    <#if taskDetailsDto.context.DAF_SERVICE_ITEMS??>
        <@section id="deliveredServices" label="i18n.taskmgmt.taskdetail.delivered.services.ftl">
            <@row id="trDeliveredServices">
                <daf-delivered-services task="task" disable-dates-editing="true"/>
            </@row>
        </@section>
    </#if>
</div>

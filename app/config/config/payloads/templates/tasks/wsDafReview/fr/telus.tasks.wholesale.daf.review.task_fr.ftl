<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                A(n) ${taskDetailsDto.inputParam.PRODUCT_NAME} is in fulfillment for
                ${taskDetailsDto.inputParam.CUSTOMER_BUSINESS_NAME}. A DAF has been drafted
                and is available as an attachment on the wholesale order <a
                        href="${taskDetailsDto.inputParam.WS_ORDER_LINK}"
                        class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.WS_ORDER_ID}</a>, which correlates
                to the OPI ${taskDetailsDto.inputParam.CUSTOMER_OPI_NUMBER}. In case of partial delivery please modify
                the retail order <a href="${taskDetailsDto.inputParam.RT_ORDER_LINK}"
                                   class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.RT_ORDER_ID}</a> prior
                to closing this task.
            </div>
        </@row>
    </@section>
    <#if taskDetailsDto.context.DAF_SERVICE_ITEMS??>
        <@section id="deliveredServices" label="i18n.taskmgmt.taskdetail.delivered.services.ftl">
            <@row id="trDeliveredServices">
                <daf-delivered-services task="task"/>
            </@row>
        </@section>
    </#if>
</div>

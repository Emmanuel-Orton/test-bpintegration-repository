<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info_fr.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                A(n) ${taskDetailsDto.inputParam.PRODUCT_NAME} is in fulfillment for ${taskDetailsDto.inputParam.CUSTOMER_BUSINESS_NAME}.
                A Change Order has been drafted. The document is available as an attachment on the wholesale order
                <a href="${taskDetailsDto.inputParam.WS_ORDER_LINK}" class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.WS_ORDER_ID}</a>, which correlates to the OPI ${taskDetailsDto.inputParam.CUSTOMER_OPI_NUMBER}.
            </div>
        </@row>
    </@section>
</div>

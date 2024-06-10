<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">The Wholesale SOW for a ${taskDetailsDto.inputParam.PRODUCT_NAME!?html} order has been created and is available for review. Please validate the SOW for customer ${taskDetailsDto.inputParam.CUSTOMER_BUSINESS_NAME!?html}.
            </div>
        </@row>
    </@section>
</div>

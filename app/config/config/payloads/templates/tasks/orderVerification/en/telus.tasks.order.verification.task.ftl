<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">A(n) ${taskDetailsDto.context.PRODUCT_NAME!?html} order has
                been raised. Please validate the order <a href="${taskDetailsDto.inputParam.DRAFT_ORDER_LINK}" class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.DRAFT_ORDER_ID}</a> to initiate the Wholesale Statement of Work creation process.
            </div>
        </@row>
    </@section>
</div>

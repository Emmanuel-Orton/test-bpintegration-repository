<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">The e.contract and Retail SOW for
                a(n) ${taskDetailsDto.inputParam.PRODUCT_NAME!?html}
                has been signed and uploaded customer <a
                        href="${taskDetailsDto.inputParam.CUSTOMER_ACCOUNT_LINK!?html}">${taskDetailsDto.inputParam.CUSTOMER_BUSINESS_NAME!?html}</a>
                and draft order <a href="${taskDetailsDto.inputParam.DRAFT_ORDER_LINK}"
                                   class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.DRAFT_ORDER_ID}</a>.
                Please complete the Wholesale SOW signature with Partner and upload signed document to the order and
                partner profile.
            </div>
        </@row>
    </@section>
</div>

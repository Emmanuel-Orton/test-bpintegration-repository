<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info_fr.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">A(n) ${taskDetailsDto.inputParam['PRODUCT_NAME']} order has been raised. Please create the Wholesale Statement of Work and attach it to the task.</div>
        </@row>
    </@section>

    <@section id="order2" label="i18n.taskmgmt.taskdetail.order.ftl">
        <@row id="trDraftOrderId">
            <@plaintext id="orderId" name="orderId" label="i18n.taskmgmt.taskdetail.draft_order_id.ftl" value="${taskDetailsDto.inputParam['DRAFT_ORDER_ID']!}"/>
        </@row>
    </@section>

    <@section id="orderInfo" label="i18n.taskmgmt.taskdetail.orderInfo.ftl">
        <@row id="trOrderInfo">
            <telus-provisioning-task-customer-details retail-billing-account-id="${taskDetailsDto.inputParam['RETAIL_BILLING_ACCOUNT_ID']}"/>
        </@row>
    </@section>

</div>

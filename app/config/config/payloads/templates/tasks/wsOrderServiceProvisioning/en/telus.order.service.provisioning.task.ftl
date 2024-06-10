<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">This task is for the Fulfillment of a(n) ${taskDetailsDto.inputParam['PRODUCT_NAME']} which is part of the Order <a href="${taskDetailsDto.inputParam['WS_ORDER_LINK']}" >${taskDetailsDto.inputParam['WS_ORDER_ID']}</a>
            </div>
        </@row>
    </@section>
    <@section id="order" label="i18n.taskmgmt.taskdetail.order.ftl">
        <@row id="trOrderId">
            <@plaintext id="orderId" name="orderId" label="i18n.taskmgmt.taskdetail.orderid.ftl" value="${taskDetailsDto.inputParam['WS_ORDER_ID']}"/>
        </@row>
        <@row id="trRetailOrderId">
            <@plaintext id="retailOrderId" name="retailOrderId" label="i18n.taskmgmt.taskdetail.retail.orderid.ftl" value="${taskDetailsDto.inputParam['RETAIL_ORDER_ID']}"/>
        </@row>
        <@row id="trProvisioningOrderId">
            <@plaintext id="provisioningOrderId" name="provisioningOrderId" label="i18n.taskmgmt.taskdetail.provisioningOrderId.ftl" value="${taskDetailsDto.inputParam['PROVISIONING_ORDER_ID']}"/>
        </@row>
    </@section>
    <@section id="orderInfo" label="i18n.taskmgmt.taskdetail.orderInfo.ftl">
        <h3><#include "i18n.telus.tasks.new.text.ftl"> ${taskDetailsDto.inputParam['PRODUCT_NAME']} <#include "i18n.telus.tasks.in.progress.text.ftl">.</h3>
        <@row id="trOrderInfo">
            <telus-provisioning-task-customer-details retail-billing-account-id="${taskDetailsDto.inputParam['RETAIL_BILLING_ACCOUNT_ID']}" order="${taskDetailsDto.inputParam['WS_ORDER_ID']}"/>
        </@row>
    </@section>
</div>
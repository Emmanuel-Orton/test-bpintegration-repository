<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                A Change Order (Day 2) ${taskDetailsDto.context.CUSTOMER_NAME} has been drafted. The document is available as an attachment on this task
            </div>
        </@row>
        <@row id="trCustomerInformation">
            <div class="formElement" id="divCustomerInformation">
                <co-day2-customer-information task="task" disable-inputs="true" hide-additional-fields="true"/>
            </div>
        </@row>
    </@section>
</div>
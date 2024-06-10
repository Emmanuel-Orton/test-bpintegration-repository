<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                The Change Order (Day 2) document for ${taskDetailsDto.context.CUSTOMER_NAME} needs to be updated.
                Please attach the updated document, with a file name containing a new version reference before closing the task.
            </div>
        </@row>
        <@row id="trCustomerInformation">
            <div class="formElement" id="divCustomerInformation">
                <co-day2-customer-information task="task" disable-inputs="true" hide-reason="true"/>
            </div>
        </@row>
    </@section>
    <@section id="reviewerNotes" label="i18n.taskmgmt.taskdetail.reviewers_notes.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divErrors">${taskDetailsDto.inputParam['TASK_RESOLUTION_COMMENT']}</div>
        </@row>
    </@section>
</div>
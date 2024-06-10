<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                A Change Order (Day 2) is required for the following Customer
            </div>
        </@row>
        <@row id="trCustomerInformation">
            <div class="formElement" id="divCustomerInformation">
                <co-day2-customer-information task="task"/>
            </div>
        </@row>
    </@section>
</div>

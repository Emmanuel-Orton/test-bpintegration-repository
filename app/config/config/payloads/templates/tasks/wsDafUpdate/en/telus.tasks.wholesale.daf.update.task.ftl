<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                The DAF for wholesale order <a href="${taskDetailsDto.inputParam.WS_CSM_ORDER_LINK}"
                                               class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.WS_ORDER_ID}</a>
                needs to be updated.
            </div>
        </@row>
    </@section>

    <@section id="reviewerNotes" label="i18n.taskmgmt.taskdetail.reviewers_notes.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divErrors">${taskDetailsDto.inputParam['TASK_RESOLUTION_COMMENT']}</div>
        </@row>
    </@section>
</div>

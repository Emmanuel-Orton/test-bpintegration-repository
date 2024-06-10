<div>
    <#include "macro.form.ftl">
    <#include "telus.tasks.sales.agent.info_fr.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                A change order has been approved and attached to the wholesale order. Please modify retail order
                <a href="${taskDetailsDto.inputParam.RT_ORDER_LINK}"
                   class="telusGreenTextUnderlined">${taskDetailsDto.inputParam.RT_ORDER_ID}</a> according to the change
                order document.
            </div>
        </@row>
    </@section>
</div>
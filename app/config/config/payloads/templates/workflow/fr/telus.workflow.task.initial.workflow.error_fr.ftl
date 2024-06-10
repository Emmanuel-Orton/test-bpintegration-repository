<div><#include "macro.form.ftl"><@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription" style="padding-left:17px;left">
                <pre><b><#include "i18n.telus.global.payments.taskmgmt.taskdetail.workflow.camunda.act.id_fr.ftl">: </b>${taskDetailsDto.inputParam.CAMUNDA_ACT_ID!?html}</pre>
                <pre><b><#include "i18n.telus.global.payments.taskmgmt.taskdetail.workflow.camunda.proc.inst.id_fr.ftl">: </b>${taskDetailsDto.inputParam.CAMUNDA_PROC_INST_ID!?html}</pre>
                <pre><b><#include "i18n.telus.global.payments.taskmgmt.taskdetail.workflow.error.msg_fr.ftl">: </b>${taskDetailsDto.inputParam.ERROR_MSG!?html}</pre>
            </div>
        </@row></@section>
</div>


<div><#include "macro.form.ftl"><@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription" style="padding-left:17px;left">
                <pre><b><#include "i18n.telus.workflow.taskmgmt.taskdetail.workflow.camunda.workflow.name.ftl">: </b>${taskDetailsDto.inputParam.WORKFLOW_NAME!?html}</pre>
                <pre><b><#include "i18n.telus.workflow.taskmgmt.taskdetail.workflow.camunda.act.id.ftl">: </b>${taskDetailsDto.inputParam.CAMUNDA_ACT_ID!?html}</pre>
                <pre><b><#include "i18n.telus.workflow.taskmgmt.taskdetail.workflow.camunda.proc.inst.id.ftl">: </b>${taskDetailsDto.inputParam.CAMUNDA_PROC_INST_ID!?html}</pre>
                <pre><b><#include "i18n.telus.workflow.taskmgmt.taskdetail.workflow.error.msg.ftl">: </b>${taskDetailsDto.inputParam.ERROR_MSG!?html}</pre>
                <pre><b><#include "i18n.telus.workflow.taskmgmt.taskdetail.workflow.error.stack.trace.ftl">: </b>${taskDetailsDto.inputParam.ERROR_STACK_TRACE_1!?html}${taskDetailsDto.inputParam.ERROR_STACK_TRACE_2!?html}${taskDetailsDto.inputParam.ERROR_STACK_TRACE_3!?html}${taskDetailsDto.inputParam.ERROR_STACK_TRACE_4!?html}</pre>
            </div>
        </@row></@section>
</div>

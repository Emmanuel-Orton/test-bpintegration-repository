<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
              "The Wholesale Change Order document for ${taskDetailsDto.inputParam.CUSTOMER_NAME} has been reviewed.
              Please refer to the attachment on this Change Order (Day 2) Review task ${taskDetailsDto.inputParam.REVIEW_TASK_ID} to assist with drafting the Retail Change Order (Day 2) and send it to the sales agent to initiate the retail contract amendment document.
              Once the signed contract amendment document has been uploaded to the customer account documents, please close this task.</div>
        </@row>
        <@row id="trCustomerInformation">
            <div class="formElement" id="divCustomerInformation">
                                      <table class="r6table r6table--fixed">
                                    <tr>
                                        <td>Customer Legal Name</td>
                                        <td>${taskDetailsDto.context.customerLegalName}</td>
                                        <td>Partner Name</td>
                                        <td>${taskDetailsDto.context.PARTNER_NAME}</td>
                                    </tr>
                                    <tr>
                                        <td>Service Name</td>
                                        <td>${taskDetailsDto.context.serviceName}</td>
                                        <td>Partner User Name</td>
                                        <td>${taskDetailsDto.context.csmCreator}</td>
                                    </tr>
                                    <tr>
                                        <td>Change Order (Day 2) Reference</td>
                                        <td>${taskDetailsDto.context.coReference}</td>
                                        <td>Partner User Email</td>
                                        <td>${taskDetailsDto.context.userEmail}</td>
                                    </tr>
                           </table>
                             <p></p>
                              <co-day2-retailer-checkbox task="task"/>
            </div>
        </@row>
    </@section>
</div>
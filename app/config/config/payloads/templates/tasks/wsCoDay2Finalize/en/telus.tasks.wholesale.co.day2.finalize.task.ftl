<div>
    <#include "macro.form.ftl">
    <@section id="description" label="i18n.taskmgmt.taskdetail.description.ftl">
        <@row id="trTaskDescription">
            <div class="formElement" id="divtxtDescription">
                A Change Order (Day 2) is ready to finalize for the following Customer
            </div>
        </@row>
        <@row id="trCustomerInformation">
            <div class="formElement" id="divCustomerInformation">
                <co-day2-customer-information task="task" disable-inputs="true" hide-reason="true" hide-additional-fields="true"/>
                    <p>
                        A Change Order (Day 2) document for ${taskDetailsDto.context.CUSTOMER_NAME} is ready to be finalized. Please complete the following before resolving the task:
                    </p>
                    <ul>
                        <li>Sign and upload the WS Change Order (Day 2) document to the Partner Account Documents Center.</li>
                        <li>On Retail Tenant configure and submit a Modify Offer associated with the original WS Order Number ${taskDetailsDto.context.WS_ORDER_ID}.</li>
                        <li>Attach the signed Retail Documents to the new Retail Order and to the Customer’s Account Documents.</li>
                        <li>On Wholesale Tenant please review and modify the Wholesale Pricing on the new Wholesale Order as required to align to the Wholesale Change Order document then upload to the new Wholesale Order.</li>
                    </ul>
                <co-day2-finalize-checkbox task="task"/>
            </div>
        </@row>
    </@section>
</div>

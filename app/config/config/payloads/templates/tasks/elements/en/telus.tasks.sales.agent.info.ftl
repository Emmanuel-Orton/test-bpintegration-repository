<@row id="trSalesAgent">
    <div class="formElement" id="divSalesAgent">
        Sales Agent Name:
            <#if taskDetailsDto.context.SALES_AGENT_NAME?has_content>
              ${taskDetailsDto.context.SALES_AGENT_NAME}
            <#else>
              ${taskDetailsDto.context.SALES_AGENT_USERNAME}
            </#if> <br>
        Sales Agent Email: ${taskDetailsDto.context.SALES_AGENT_EMAIL} <br>
    </div>
</@row>
<#assign templateBody>
    <table width="100%" border="0" cellspacing="0" cellpadding="0">
        <tr>
            <td>
                <span class="telusHeadline">You have a pending order</span>
            </td>
        </tr>
        <tr>
            <td>
                <span>Hi ${CUSTOMER_NAME},</span>
            </td>
        </tr>
        <tr>
            <td>
                <span>
                    <#include "notification.telus_draft_order_approval.email_my_messages.raw.body.html.ftl">
                </span>
            </td>
        </tr>
        <tr>
            <td>
                <span>
                   If you have any questions, please <b>contact us</b> and we'll help you get what you need.
                </span>
            </td>
        </tr>
    </table>
</#assign>

<#include "layout.telus.main.template.ftl">
<#outputformat 'HTML'>
    <@customerFacingTemplate templateBody=templateBody renderContactUs=true/>
</#outputformat>

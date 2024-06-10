package com.bearingpoint.beyond.test-bpintegration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.JSONStringUtils;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.StringTokenizer;

@Slf4j
@Service
public class SendGridService {

    @Value("${notification.smtp.password}")
    private String apiKey;

    @Value("${notification.smtp.marketplace.email}")
    private String marketplaceEmail;

    @Value("${notification.smtp.replyTo}")
    private String replyToEmail;

    public void sendEmail(String recipient, String ccs, String subject, String body) throws IOException {
        Email from = new Email(marketplaceEmail);
        Email to = new Email(recipient);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, to, content);
        mail.setReplyTo(new Email(replyToEmail));

        Personalization personalization = mail.getPersonalization().get(0);
        if (ccs != null && !ccs.isBlank()) {
            StringTokenizer tokenizer = new StringTokenizer(ccs, ",;");
            while (tokenizer.hasMoreTokens()) {
                personalization.addCc(new Email(StringUtils.trim(tokenizer.nextToken())));
            }
        }

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            log.debug("SendGrid response: " + response.getStatusCode() + " with body: "+ response.getBody());
            if (response.getStatusCode() > 299) {
                log.error("Error sending the email: "+ response.getStatusCode());
                throw new IOException("Error sending the email: "+ response.getBody());
            }
        } catch (Exception ex) {
            log.error("Error sending email to {}", recipient, ex);
            throw ex;
        }
    }

}
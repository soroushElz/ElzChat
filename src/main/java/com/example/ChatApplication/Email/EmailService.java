package com.example.ChatApplication.Email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static io.jsonwebtoken.lang.Strings.UTF_8;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendActivationEmail(String to,
                          String username,
                          EmailTemplateName emailTemplate,
                          String redirectUrl,
                          String Code, String subject
    ) throws MessagingException{
      String templateName;
        if (emailTemplate == null) {
            templateName = "confirm-email";
        } else {
            templateName = emailTemplate.name();
        }
        MimeMessage mimeMessage=mailSender.createMimeMessage();
        MimeMessageHelper helper=new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                UTF_8.name()
        );
        Map<String,Object> properties=new HashMap<>();
        properties.put("username",username);
        properties.put("redirectUrl",redirectUrl);
        properties.put("code", Code);

        Context context=new Context();
        context.setVariables(properties);

        helper.setFrom("soroush.yz.97@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);

        String template=templateEngine.process(templateName,context);
        helper.setText(template,true);
        mailSender.send(mimeMessage);
    }
}

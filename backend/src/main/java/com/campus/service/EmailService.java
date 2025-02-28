package com.campus.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // 创建Thymeleaf上下文
        Context context = new Context();
        context.setVariable("code", code);

        // 处理模板
        String htmlContent = templateEngine.process("verification-email", context);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject("验证码邮件");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
} 
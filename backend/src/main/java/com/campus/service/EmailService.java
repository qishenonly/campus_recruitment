package com.campus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String to, String code) throws MessagingException {
        // 打印验证码到控制台
        logger.info("发送验证码到 {}: {}", to, code);

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

    /**
     * 发送面试邀请邮件
     */
    public void sendInterviewInvitation(String to, String candidateName, String companyName, 
                                       String jobTitle, LocalDateTime interviewTime, 
                                       String location, String interviewType, String description) throws MessagingException {
        String subject = companyName + " - 面试邀请：" + jobTitle;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm");
        String formattedTime = interviewTime.format(formatter);
        
        Context context = new Context();
        context.setVariable("candidateName", candidateName);
        context.setVariable("companyName", companyName);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("interviewTime", formattedTime);
        context.setVariable("location", location);
        context.setVariable("interviewType", "ONLINE".equals(interviewType) ? "线上面试" : "现场面试");
        context.setVariable("description", description);
        
        String content = templateEngine.process("interview-invitation", context);
        sendHtmlMail(to, subject, content);
    }

    private void sendHtmlMail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
} 
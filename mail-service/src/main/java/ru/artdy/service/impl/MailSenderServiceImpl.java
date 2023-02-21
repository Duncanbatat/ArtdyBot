package ru.artdy.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.artdy.dto.MailParams;
import ru.artdy.service.MailSenderService;

@Service
public class MailSenderServiceImpl implements MailSenderService {
    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Value("${service.activation.uri}")
    private String serviceActivationUri;

    public MailSenderServiceImpl(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void send(MailParams mailParams) {
        String mailSubject = "Profile activation";
        String messageBody = getActivationMailBody(mailParams.getId());
        String emailTo = mailParams.getMailTo();

        SimpleMailMessage mailMessage = getMailMessage(mailSubject, messageBody, emailTo);
        javaMailSender.send(mailMessage);
    }

    private SimpleMailMessage getMailMessage(String mailSubject, String messageBody, String emailTo) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(mailSubject);
        mailMessage.setText(messageBody);
        return mailMessage;
    }

    private String getActivationMailBody(String id) {
        String message = String.format("To complete your profile activation follow the link:\n%s",
                serviceActivationUri);
        return message.replace("{id}", id);
    }
}

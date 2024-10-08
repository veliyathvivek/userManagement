package com.project.userManagement.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.*;

import static com.project.userManagement.constants.EmailConstants.EMAIL_SUBJECT;

@Service
public class EmailService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final JavaMailSender emailSender;

    @Autowired
    public EmailService(JavaMailSender emailSender, Environment environment) {
        this.emailSender = emailSender;
    }

    public void sendNewPasswordEmail(String firstName, String username,String password, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(EMAIL_SUBJECT);
        message.setText("Hello " + firstName + "!\n\nYour new account password is: " + password + "\n\nFor the username: "+username+"\n\nThe Support Team");
        message.setFrom("v3@demomailtrap.com");
        emailSender.send(message);
    }

}

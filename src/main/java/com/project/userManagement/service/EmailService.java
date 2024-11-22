package com.project.userManagement.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender emailSender;

    @Autowired
    public EmailService(JavaMailSender emailSender, Environment environment) {
        this.emailSender = emailSender;
    }

    public void sendNewPasswordEmail(String firstName, String username, String password, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Welcome To The Platform");
        message.setText("Hello " + firstName + "!\n\nYour new password is: " + password + "\n\nfor the username: " + username+"," + "\n\nThe Support Team");
        message.setFrom("veliyathvivek@gmail.com");
        emailSender.send(message);
    }
}

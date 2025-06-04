package com.exe201.project.service;

import jakarta.mail.MessagingException;

public interface EmailService {
    String bodyResetPassword(String email);
    void sendEmail(String to, String subject, String body) throws MessagingException;
    String subjectRegister();
    String bodyRegister(String email ,String fullName, String phone, String address);
    String subjectResetPassword();
}

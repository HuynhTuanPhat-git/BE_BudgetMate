package com.exe201.project.service.impl;

import com.exe201.project.configuration.security.jwt.JwtUtils;
import com.exe201.project.repository.UserRepository;
import com.exe201.project.service.EmailService;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserRepository userRepository;
    @Value("${spring.mail.username}")
    private String systemMail;
    @Value("${client.domain}")
    private String clientUrl;
    @Autowired
    private JwtUtils jwtUtils;

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        // Send email with HTML

        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(systemMail);
        message.setRecipients(Message.RecipientType.TO, to);
        message.setSubject(subject);
        message.setContent(body, "text/html; charset=utf-8");

        mailSender.send(message);
    }

    public String subjectRegister(){
        return "Welcome to BudgetMate System";
    }

    public String bodyRegister(String email ,String fullName, String phone, String address){
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"utf-8\">\n"
                + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <style>\n"
                + "    body {\n"
                + "      font-family: Arial, sans-serif;\n"
                + "      background-color: #f4f4f4;\n"
                + "      margin: 0;\n"
                + "      padding: 0;\n"
                + "    }\n"
                + "\n"
                + "    .email-container {\n"
                + "      max-width: 600px;\n"
                + "      margin: 20px auto;\n"
                + "      background-color: #fff;\n"
                + "      border-radius: 8px;\n"
                + "      overflow: hidden;\n"
                + "      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n"
                + "    }\n"
                + "\n"
                + "    .header {\n"
                + "      background-color: #00796B;\n"
                + "      color: #fff;\n"
                + "      padding: 20px;\n"
                + "      text-align: center;\n"
                + "    }\n"
                + "\n"
                + "    .content {\n"
                + "      padding: 20px;\n"
                + "    }\n"
                + "\n"
                + "    .button{\n" +
                "        background-color: #A3D9A5;\n" +
                "        color: #fff;\n" +
                "        margin: 24px 24px 24px 24px;\n" +
                "        padding: 10px 20px;\n" +
                "        border: none;\n" +
                "        cursor: pointer;\n" +
                "        text-decoration: none;\n" +
                "      }\n"
                + "\n"
                + " a {\n" +
                "        color: #fff;\n" +
                "      }\n"
                + "    .footer {\n"
                + "      background-color: #34db74;\n"
                + "      color: #fff;\n"
                + "      padding: 10px;\n"
                + "      text-align: center;\n"
                + "    }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div class=\"email-container\">\n"
                + "    <div class=\"header\">\n"
                + "     <h1>BudgetMate System</h1>\n "
                + "      <h2>Welcome " + fullName + "</h2>\n"
                + "    </div>\n"
                + "    <div class=\"content\">\n"
                + "      <h3>Information:</h3>\n"
                + "      <p>Date : " + LocalDate.now() + "</p>\n"
                + "      <p>Address : " + address + "</p>\n"
                + "      <p>Phone number : " + phone + "</p>\n"
                + "      <p>This email valid in 24h. Please verify your email as soon as possible</p>\n"
                + "      <a class=\"button\" href=\""+ clientUrl + "verify?token=" + jwtUtils.generateVerificationToken(email)+"\">VERIFY</a>\n"
                + "      <p>We are ready support</p>\n"
                + "    </div>\n"
                + "    <div class=\"footer\">\n"
                + "      <p>Thanks for Subscribe</p>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>";
    }

    public String subjectResetPassword(){
        return "Reset Your Password";
    }

    public String bodyResetPassword(String email){
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\">\n"
                + "<head>\n"
                + "  <meta charset=\"utf-8\">\n"
                + "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>\n"
                + "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "  <style>\n"
                + "    body {\n"
                + "      font-family: Arial, sans-serif;\n"
                + "      background-color: #f4f4f4;\n"
                + "      margin: 0;\n"
                + "      padding: 0;\n"
                + "    }\n"
                + "\n"
                + "    .email-container {\n"
                + "      max-width: 600px;\n"
                + "      margin: 20px auto;\n"
                + "      background-color: #fff;\n"
                + "      border-radius: 8px;\n"
                + "      overflow: hidden;\n"
                + "      box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);\n"
                + "    }\n"
                + "\n"
                + "    .header {\n"
                + "      background-color:#ff4545;\n"
                + "      color: #fff;\n"
                + "      padding: 20px;\n"
                + "      text-align: center;\n"
                + "    }\n"
                + "\n"
                + "    .content {\n"
                + "      padding: 20px;\n"
                + "    }\n"
                + "\n"
                + "    .button{\n" +
                "        background-color: #ff4545;\n" +
                "        color: #fff;\n" +
                "        margin: 24px 24px 24px 24px;\n" +
                "        padding: 10px 20px;\n" +
                "        border: none;\n" +
                "        cursor: pointer;\n" +
                "        text-decoration: none;\n" +
                "      }\n"
                + "\n"
                + " a {\n" +
                "        color: #fff;\n" +
                "      }\n"
                + "    .footer {\n"
                + "      background-color: #ff4545;\n"
                + "      color: #fff;\n"
                + "      padding: 10px;\n"
                + "      text-align: center;\n"
                + "    }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div class=\"email-container\">\n"
                + "    <div class=\"header\">\n"
                + "     <h1>BudgetMate System</h1>\n "
                + "    </div>\n"
                + "    <div class=\"content\">\n"
                + "      <h3>Reset Password</h3>\n"
                + "      <p>Date : " + LocalDate.now() + "</p>\n"
                + "      <p>This email valid in 24h</p>\n"
                + "      <a class=\"button\" href=\""+ clientUrl + "reset/password?token=" + jwtUtils.generateResetPasswordToken(email)+"\">RESET PASSWORD</a>\n"
                + "      <p>We are ready support</p>\n"
                + "    </div>\n"
                + "    <div class=\"footer\">\n"
                + "      <p>Thanks for Subscribe</p>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "</body>\n"
                + "</html>";
    }
}
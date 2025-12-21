package com.foodapp.foodhub.service;

import com.foodapp.foodhub.entity.EmailVerificationCode;
import com.foodapp.foodhub.repository.EmailVerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService
{
    private final EmailVerificationCodeRepository otpRepository;
    private final JavaMailSender mailSender;

    public void saveOtp(String email, String code) {

        EmailVerificationCode otpEntity = EmailVerificationCode.builder()
                .email(email)
                .code(code)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepository.save(otpEntity);
    }

    public void sendOtpEmail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("yyynny4@gmail.com");
        message.setTo(email);
        message.setSubject("Verify your email");
        message.setText("Your verification code is: " + code);
        mailSender.send(message);
    }
}

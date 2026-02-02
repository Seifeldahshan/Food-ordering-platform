package com.foodapp.foodhub.service;

import com.foodapp.foodhub.dto.auth.*;
import com.foodapp.foodhub.entity.EmailVerificationCode;
import com.foodapp.foodhub.entity.OtpToken;
import com.foodapp.foodhub.entity.Token;
import com.foodapp.foodhub.entity.User;
import com.foodapp.foodhub.enums.Role;
import com.foodapp.foodhub.enums.TokenType;
import com.foodapp.foodhub.enums.UserStatus;
import com.foodapp.foodhub.repository.EmailVerificationCodeRepository;
import com.foodapp.foodhub.repository.OtpTokenRepository;
import com.foodapp.foodhub.repository.TokenRepository;
import com.foodapp.foodhub.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Random;
@Service
@RequiredArgsConstructor
public class AuthenticationService
{
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final OtpTokenRepository otpTokenRepository;
    private final JwtAuthService jwtService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private  final PasswordEncoder passwordEncoder;
    private final EmailVerificationCodeRepository emailVerificationCodeRepository;
    private final int OTP_EXPIRY_MINUTES = 5;
    private final int OTP_WAIT_SECONDS = 60;
    private final RestClient.Builder builder;
    private final JavaMailSender mailSender;
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );
            User user = userService.findByUsername(authenticationRequest.getUsername());
            String token = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            revokeAllUserTokens(user, TokenType.ACCESS);
            revokeAllUserTokens(user, TokenType.REFRESH);

            saveToken(token, TokenType.ACCESS, user);
            saveToken(refreshToken, TokenType.REFRESH, user);

            return AuthenticationResponse.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .status("Success")
                    .message("you have been authenticated")
                    .build();
        }
        catch (AuthenticationException e) {
             return AuthenticationResponse.builder()
                     .status("Failed")
                     .message(e.getMessage())
                     .build();
        }
    }

    public PasswordResponse sendOtp(ForgetPasswordRequest request) throws MessagingException {
        User user = userRepository.findByEmail(request.getEmail()).get();
        if(user == null) {
            return PasswordResponse.builder()
                    .success(false)
                    .message("user not found")
                    .build();
        }
        OtpToken lastOtp = otpTokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (lastOtp != null && lastOtp.getCreatedAt().plusSeconds(OTP_WAIT_SECONDS).isAfter(LocalDateTime.now())) {
            return PasswordResponse.builder().message( "Please wait 1 minute before requesting a new OTP")
                    .success(false)
                    .build();
        }
        String otp = String.format("%06d", new Random().nextInt(999999));
        OtpToken otpToken =
                OtpToken.builder()
                .user(user)
                .otp(otp)
                .createdAt(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .build();
        otpTokenRepository.save(otpToken);
        emailService.sendOtpEmail(user.getEmail(), otp, OTP_EXPIRY_MINUTES);

        return PasswordResponse.builder().success(true)
                .message("OTP send Successfully to " + request.getEmail())
                .build();


    }


    public PasswordResponse resetPassword(ResetPasswordRequest request)  {
        User user = userRepository.findByEmail(request.getEmail()).get();
        if(user == null) {
            return PasswordResponse.builder()
                    .message("user not found")
                    .success(false)
                    .build();
        }
     OtpToken otpOpt = otpTokenRepository.findByUserAndOtpAndExpiryTimeAfter(user, request.getOtp(), LocalDateTime.now());
        if (otpOpt == null){
            return PasswordResponse.builder()
                    .message("invalid or expired OTP")
                    .success(false)
                    .build();
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        otpTokenRepository.delete(otpOpt);
        userRepository.save(user);

        return PasswordResponse.builder()
                .message("Password reset successfully")
                .success(true)
                .build();

    }
    void saveToken( String token, TokenType tokenType, User user) {
        var savedToken = Token.builder()
                .user(user)
                .token(token)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(savedToken);

    }

    void revokeAllUserTokens(User user, TokenType tokenType) {
        tokenRepository.revokeAllByType(user.getId(), tokenType);
    }

    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response)
    {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return buildFailedResponse("No Refresh Token Found", null);


        final String refreshToken = authHeader.substring(7);
        final String username = jwtService.extractUsername(refreshToken);

        if (username == null)
            return buildFailedResponse("The token doesn't contain username", refreshToken);


        var user = userRepository.findByUsername(username);
        if (user == null)
            return buildFailedResponse("There is no such user", refreshToken);


        if (!jwtService.isTokenValid(refreshToken, user, TokenType.REFRESH))
            return buildFailedResponse("Invalid or expired refresh token", refreshToken);


        var accessToken = jwtService.generateToken(user);

        revokeAllUserTokens(user, TokenType.ACCESS);
        saveToken(accessToken, TokenType.ACCESS, user);

        return AuthenticationResponse.builder()
                .status("success")
                .message("Refresh Token Success")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private AuthenticationResponse buildFailedResponse(String message, String refreshToken) {
        return AuthenticationResponse.builder()
                .status("Failed")
                .message(message)
                .refreshToken(refreshToken)
                .build();
    }

    public PasswordResponse changePassword(ChangePasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).get();
        if (user == null) {
            return PasswordResponse.builder()
                    .message("user not found")
                    .success(false)
                    .build();

        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return PasswordResponse.builder()
                    .message("old password is incorrect")
                    .success(false)
                    .build();
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return PasswordResponse.builder()
                .message("password changed successfully")
                .success(true)
                .build();

    }

    private void sendVerificationEmail(String email, String code)
    {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("yyynny4@gmail.com");
        message.setTo(email);
        message.setSubject("Email Verification Code");
        message.setText("Your verification code is: " + code + "\nThis code will expire in 5 minutes.");
        mailSender.send(message);
    }

    public RegisterResponse sendVerificationCode(RegisterRequest request)
    {
        if (userRepository.findByEmail(request.getEmail()).isPresent())
        {
            return RegisterResponse.builder()
                    .status("Failed")
                    .message("Email already registered")
                    .build();
        }
        String code = String.format("%04d", new Random().nextInt(10000));
        EmailVerificationCode verification = EmailVerificationCode.builder()
                .email(request.getEmail())
                .code(code)
                .createdAt(LocalDateTime.now())
                .build();

        emailVerificationCodeRepository.save(verification);
        sendVerificationEmail(request.getEmail(), code);
        return RegisterResponse.builder()
                .status("Success")
                .message("Verification code sent to your email")
                .build();
    }
    public AuthenticationResponse verifyAndRegister(VerifyRequest verifyRequest, RegisterRequest registerRequest) {

        Optional<EmailVerificationCode> verificationOpt =
                emailVerificationCodeRepository.findTopByEmailOrderByCreatedAtDesc(verifyRequest.getEmail());

        if (verificationOpt.isEmpty()) {
            return AuthenticationResponse.builder()
                    .status("Failed")
                    .message("No verification code found")
                    .build();
        }

        EmailVerificationCode verification = verificationOpt.get();

        if (!verification.getCode().equals(verifyRequest.getCode()))
        {
            return AuthenticationResponse.builder()
                    .status("Failed")
                    .message("Invalid verification code")
                    .build();
        }
        LocalDateTime now = LocalDateTime.now();
        long minutesPassed = ChronoUnit.MINUTES.between(verification.getCreatedAt(), now);


        // TODO :: change to 5 or 10 , 30 just for testing and the message also
        if (minutesPassed > 30) {
            return AuthenticationResponse.builder()
                    .status("Failed")
                    .message("Verification code expired")
                    .build();
        }

        User user = User.builder()
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .fullName(registerRequest.getFullName())
                .phone(registerRequest.getPhone())
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
        verification.setVerified(true);
        emailVerificationCodeRepository.save(verification);
        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        saveToken(token, TokenType.ACCESS, user);
        saveToken(refreshToken, TokenType.REFRESH, user);

        return AuthenticationResponse.builder()
                .status("Success")
                .message("Registration successful")
                .accessToken(token)
                .refreshToken(refreshToken)
                .build();
    }
}
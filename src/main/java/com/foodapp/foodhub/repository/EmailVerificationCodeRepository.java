package com.foodapp.foodhub.repository;

import com.foodapp.foodhub.entity.EmailVerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationCodeRepository extends JpaRepository<EmailVerificationCode, Long>
{
    Optional<EmailVerificationCode>findTopByEmailOrderByCreatedAtDesc(String email);

}
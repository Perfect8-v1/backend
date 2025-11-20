package com.perfect8.admin.repository;

import com.perfect8.admin.model.User;
import com.perfect8.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByResetPasswordToken(String token);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.isActive = false")
    List<User> findAllInactive();

    @Query("SELECT u FROM User u WHERE u.createdDate >= :since")
    List<User> findUsersCreatedSince(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.accountLockedUntil IS NOT NULL AND u.accountLockedUntil > :now")
    List<User> findLockedAccounts(@Param("now") LocalDateTime now);
}
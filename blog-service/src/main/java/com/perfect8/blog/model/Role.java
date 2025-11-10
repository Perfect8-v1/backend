package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role entity for blog user roles
 * 
 * FIXED: id → roleId (Magnum Opus: [entityName]Id)
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;  // FIXED: id → roleId (Magnum Opus compliance)

    @Column(nullable = false, unique = true, length = 100)
    private String name;  // ROLE_USER, ROLE_ADMIN
}

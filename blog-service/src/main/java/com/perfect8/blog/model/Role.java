package com.perfect8.blog.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Entity - Version 1.0
 * FIXED: id → roleId (Magnum Opus)
 * FIXED: Added Lombok annotations
 */
@Entity
@Table(name = "roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;  // → DB: role_id (Magnum Opus)

    @Column(unique = true, nullable = false)
    private String name;  // → DB: name
}
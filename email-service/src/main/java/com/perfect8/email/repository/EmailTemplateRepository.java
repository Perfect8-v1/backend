package com.perfect8.email.repository;

import com.perfect8.email.entity.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EmailTemplate entity
 * Version 1.0 - Core template management
 */
@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {

    Optional<EmailTemplate> findByNameAndActiveTrue(String name);

    List<EmailTemplate> findByActiveTrue();

    // FIXED: Changed from findByTemplateName to findByName
    List<EmailTemplate> findByName(String name);

    List<EmailTemplate> findByCategory(String category);

    @Query("SELECT e FROM EmailTemplate e WHERE e.active = true AND e.templateType = :type")
    List<EmailTemplate> findActiveTemplatesByType(@Param("type") String templateType);

    @Query("SELECT DISTINCT e.category FROM EmailTemplate e WHERE e.active = true")
    List<String> findDistinctCategories();

    @Query("SELECT e.name FROM EmailTemplate e WHERE e.active = true ORDER BY e.usageCount DESC")
    List<String> findMostUsedTemplateNames();

    boolean existsByName(String name);
}
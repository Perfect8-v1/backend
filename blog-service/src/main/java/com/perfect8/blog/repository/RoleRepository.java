// blog-service/src/main/java/com/perfect8/blog/repository/RoleRepository.java

        package com.perfect8.blog.repository;

import com.perfect8.blog.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    boolean existsByName(String name);
}
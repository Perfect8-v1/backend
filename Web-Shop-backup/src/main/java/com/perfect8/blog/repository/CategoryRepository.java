package com.perfect8.blog.repository;

//package com.perfect8.blog.repository;

import com.perfect8.blog.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // All basic database methods are inherited for free.
}

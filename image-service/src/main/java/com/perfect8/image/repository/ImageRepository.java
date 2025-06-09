// image-service/src/main/java/com/perfect8/image/repository/ImageRepository.java
//

        package com.perfect8.image.repository;

import com.perfect8.image.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {
}
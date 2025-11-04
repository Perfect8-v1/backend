package com.perfect8.admin.service;

import org.springframework.stereotype.Service;

@Service
public class ImageService {

    public String uploadImage(String filename) {
        return "Image uploaded: " + filename;
    }

    public String getImageUrl(String filename) {
        return "http://localhost:8081/images/" + filename;
    }
}

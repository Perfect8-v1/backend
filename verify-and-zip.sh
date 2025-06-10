#!/bin/bash

# Blog Microservice Project File Verification Script
# This script checks all required files and creates a zip archive

echo "Blog Microservice Project File Verification"
echo "=========================================="

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Counter for missing files
MISSING_COUNT=0

# Function to check if file exists
check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
        return 0
    else
        echo -e "${RED}✗${NC} $1 - MISSING"
        ((MISSING_COUNT++))
        return 1
    fi
}

# Function to check if directory exists
check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1/"
        return 0
    else
        echo -e "${RED}✗${NC} $1/ - MISSING"
        ((MISSING_COUNT++))
        return 1
    fi
}

echo -e "\n${YELLOW}Checking root files...${NC}"
check_file "docker-compose.yml"
check_file ".env"
check_file ".env.example"
check_file ".gitlab-ci.yml"
check_file "README.md"
check_file "API_DOCUMENTATION.md"

echo -e "\n${YELLOW}Checking blog-service structure...${NC}"
check_dir "blog-service"
check_file "blog-service/Dockerfile"
check_file "blog-service/pom.xml"

echo -e "\n${YELLOW}Checking blog-service source files...${NC}"
# Main application
check_file "blog-service/src/main/java/com/perfect8/blog/BlogServiceApplication.java"

# Config files
check_file "blog-service/src/main/java/com/perfect8/blog/config/SecurityConfig.java"
check_file "blog-service/src/main/java/com/perfect8/blog/config/JwtConfig.java"
check_file "blog-service/src/main/java/com/perfect8/blog/config/DataInitializer.java"
check_file "blog-service/src/main/java/com/perfect8/blog/config/FeignConfig.java"

# Controllers
check_file "blog-service/src/main/java/com/perfect8/blog/controller/AuthController.java"
check_file "blog-service/src/main/java/com/perfect8/blog/controller/PostController.java"
check_file "blog-service/src/main/java/com/perfect8/blog/controller/AdminController.java"

# DTOs
check_file "blog-service/src/main/java/com/perfect8/blog/dto/PostDto.java"
check_file "blog-service/src/main/java/com/perfect8/blog/dto/UserDto.java"
check_file "blog-service/src/main/java/com/perfect8/blog/dto/AuthDto.java"

# Exceptions
check_file "blog-service/src/main/java/com/perfect8/blog/exception/GlobalExceptionHandler.java"
check_file "blog-service/src/main/java/com/perfect8/blog/exception/ResourceNotFoundException.java"

# Models
check_file "blog-service/src/main/java/com/perfect8/blog/model/Post.java"
check_file "blog-service/src/main/java/com/perfect8/blog/model/User.java"
check_file "blog-service/src/main/java/com/perfect8/blog/model/Role.java"
check_file "blog-service/src/main/java/com/perfect8/blog/model/ImageReference.java"

# Repositories
check_file "blog-service/src/main/java/com/perfect8/blog/repository/PostRepository.java"
check_file "blog-service/src/main/java/com/perfect8/blog/repository/UserRepository.java"
check_file "blog-service/src/main/java/com/perfect8/blog/repository/RoleRepository.java"
check_file "blog-service/src/main/java/com/perfect8/blog/repository/ImageReferenceRepository.java"

# Security
check_file "blog-service/src/main/java/com/perfect8/blog/security/JwtTokenProvider.java"
check_file "blog-service/src/main/java/com/perfect8/blog/security/JwtAuthenticationFilter.java"
check_file "blog-service/src/main/java/com/perfect8/blog/security/UserDetailsServiceImpl.java"

# Services
check_file "blog-service/src/main/java/com/perfect8/blog/service/PostService.java"
check_file "blog-service/src/main/java/com/perfect8/blog/service/UserService.java"
check_file "blog-service/src/main/java/com/perfect8/blog/service/AuthService.java"
check_file "blog-service/src/main/java/com/perfect8/blog/service/AdminService.java"

# Client
check_file "blog-service/src/main/java/com/perfect8/blog/client/ImageServiceClient.java"

# Resources
check_file "blog-service/src/main/resources/application.properties"
check_file "blog-service/src/main/resources/application-dev.properties"
check_file "blog-service/src/main/resources/application-prod.properties"

# Tests
check_file "blog-service/src/test/java/com/perfect8/blog/service/PostServiceTest.java"

echo -e "\n${YELLOW}Checking image-service structure...${NC}"
check_dir "image-service"
check_file "image-service/Dockerfile"
check_file "image-service/pom.xml"

echo -e "\n${YELLOW}Checking image-service source files...${NC}"
# Main application
check_file "image-service/src/main/java/com/perfect8/image/ImageServiceApplication.java"

# Config files
check_file "image-service/src/main/java/com/perfect8/image/config/SecurityConfig.java"
check_file "image-service/src/main/java/com/perfect8/image/config/StorageConfig.java"

# Controller
check_file "image-service/src/main/java/com/perfect8/image/controller/ImageController.java"

# DTO
check_file "image-service/src/main/java/com/perfect8/image/dto/ImageDto.java"

# Exceptions
check_file "image-service/src/main/java/com/perfect8/image/exception/GlobalExceptionHandler.java"
check_file "image-service/src/main/java/com/perfect8/image/exception/StorageException.java"

# Model
check_file "image-service/src/main/java/com/perfect8/image/model/Image.java"

# Repository
check_file "image-service/src/main/java/com/perfect8/image/repository/ImageRepository.java"

# Service
check_file "image-service/src/main/java/com/perfect8/image/service/ImageService.java"

# Resources
check_file "image-service/src/main/resources/application.properties"
check_file "image-service/src/main/resources/application-dev.properties"

# Tests
check_file "image-service/src/test/java/com/perfect8/image/service/ImageServiceTest.java"

echo -e "\n${YELLOW}=========================================${NC}"
if [ $MISSING_COUNT -eq 0 ]; then
    echo -e "${GREEN}All files present! Total: 54 files${NC}"

    echo -e "\n${YELLOW}Creating zip archive...${NC}"

    # Create zip file
    ZIP_NAME="perfect8-blog-microservice-$(date +%Y%m%d-%H%M%S).zip"

    zip -r "$ZIP_NAME" \
        docker-compose.yml \
        .env \
        .env.example \
        .gitlab-ci.yml \
        README.md \
        API_DOCUMENTATION.md \
        blog-service/ \
        image-service/ \
        -x "*/target/*" \
        -x "*/.idea/*" \
        -x "*/.mvn/*" \
        -x "*/mvnw*" \
        -x "*/.git/*" \
        -x "*.iml" \
        -x ".DS_Store" \
        -x "*/.DS_Store"

    echo -e "${GREEN}✓ Archive created: $ZIP_NAME${NC}"
    echo -e "Size: $(du -h "$ZIP_NAME" | cut -f1)"
else
    echo -e "${RED}Missing $MISSING_COUNT files!${NC}"
    echo -e "${YELLOW}Please create the missing files before creating the archive.${NC}"
fi

echo -e "\n${YELLOW}Optional files that may be present:${NC}"
check_file "blog-service/src/main/resources/data.sql" || echo "  (This file is optional)"

# File count summary
echo -e "\n${YELLOW}File Summary:${NC}"
echo "- Root files: 6"
echo "- Blog service files: 35"
echo "- Image service files: 13"
echo "- Total required files: 54"
echo "- Optional files: 1"
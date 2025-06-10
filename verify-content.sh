#!/bin/bash

# Blog Microservice Project Content Verification Script
# This script checks file contents to ensure they are complete

echo "Blog Microservice Project Content Verification"
echo "============================================="

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counter for issues
ISSUES_COUNT=0

# Function to check if file contains required content
check_content() {
    local file=$1
    shift
    local patterns=("$@")

    if [ ! -f "$file" ]; then
        echo -e "${RED}✗${NC} $file - FILE MISSING"
        ((ISSUES_COUNT++))
        return 1
    fi

    local all_found=true
    for pattern in "${patterns[@]}"; do
        if ! grep -q "$pattern" "$file" 2>/dev/null; then
            echo -e "${RED}✗${NC} $file - Missing: '$pattern'"
            ((ISSUES_COUNT++))
            all_found=false
        fi
    done

    if [ "$all_found" = true ]; then
        echo -e "${GREEN}✓${NC} $file"
    fi
}

# Function to check file size (to detect truncated files)
check_min_size() {
    local file=$1
    local min_size=$2

    if [ ! -f "$file" ]; then
        return 1
    fi

    local size=$(wc -c < "$file")
    if [ $size -lt $min_size ]; then
        echo -e "${YELLOW}⚠${NC}  $file - Suspiciously small (${size} bytes)"
        ((ISSUES_COUNT++))
        return 1
    fi
}

echo -e "\n${BLUE}=== Checking Root Configuration Files ===${NC}"

# Docker Compose
check_content "docker-compose.yml" \
    "version: '3.8'" \
    "mysql-main:" \
    "mysql-images:" \
    "blog-service:" \
    "image-service:" \
    "blog-network:"

# Environment files
check_content ".env" \
    "MYSQL_ROOT_PASSWORD=" \
    "JWT_SECRET=" \
    "MYSQL_IMAGES_DATABASE="

check_content ".env.example" \
    "MYSQL_ROOT_PASSWORD=changeme" \
    "JWT_SECRET=changeThisToAVeryLongSecretKey"

# GitLab CI
check_content ".gitlab-ci.yml" \
    "stages:" \
    "- build" \
    "- test" \
    "- package" \
    "- deploy"

echo -e "\n${BLUE}=== Checking Blog Service Files ===${NC}"

# Blog Service Main Files
check_content "blog-service/pom.xml" \
    "<artifactId>blog-service</artifactId>" \
    "spring-boot-starter-web" \
    "spring-boot-starter-security" \
    "spring-cloud-starter-openfeign" \
    "jjwt-api"

check_content "blog-service/Dockerfile" \
    "FROM openjdk:17-jdk-slim" \
    "ENTRYPOINT"

# Main Application
check_content "blog-service/src/main/java/com/perfect8/blog/BlogServiceApplication.java" \
    "@SpringBootApplication" \
    "@EnableFeignClients" \
    "public class BlogServiceApplication"

# Configuration Classes
check_content "blog-service/src/main/java/com/perfect8/blog/config/SecurityConfig.java" \
    "@EnableWebSecurity" \
    "@EnableMethodSecurity" \
    "SecurityFilterChain" \
    "JwtAuthenticationFilter"

check_content "blog-service/src/main/java/com/perfect8/blog/config/DataInitializer.java" \
    "CommandLineRunner" \
    "ADMIN" \
    "WRITER" \
    "READER" \
    "admin@perfect8.com"

# Models - Check for complete class definitions
check_content "blog-service/src/main/java/com/perfect8/blog/model/User.java" \
    "@Entity" \
    "@Table(name = \"users\")" \
    "private String username;" \
    "private String password;" \
    "@ManyToMany"

check_content "blog-service/src/main/java/com/perfect8/blog/model/Post.java" \
    "@Entity" \
    "private String title;" \
    "private String content;" \
    "@ManyToOne" \
    "@OneToMany.*ImageReference"

check_content "blog-service/src/main/java/com/perfect8/blog/model/Role.java" \
    "@Entity" \
    "@Table(name = \"roles\")" \
    "private String name;"

# DTOs - Check for complete structure
check_content "blog-service/src/main/java/com/perfect8/blog/dto/PostDto.java" \
    "@NotBlank.*Title is required" \
    "private String content;" \
    "public static class ImageReferenceDto"

check_content "blog-service/src/main/java/com/perfect8/blog/dto/UserDto.java" \
    "private String username;" \
    "private String email;" \
    "public void setUpdatedAt.*LocalDateTime updatedAt.*}"

check_content "blog-service/src/main/java/com/perfect8/blog/dto/AuthDto.java" \
    "public static class LoginRequest" \
    "public static class RegisterRequest" \
    "public static class JwtResponse"

# Services - Check for key methods
check_content "blog-service/src/main/java/com/perfect8/blog/service/PostService.java" \
    "public PostDto createPost" \
    "public PostDto updatePost" \
    "private String generateSlug" \
    "@Transactional"

check_content "blog-service/src/main/java/com/perfect8/blog/service/AuthService.java" \
    "public.*JwtResponse login" \
    "public.*JwtResponse register" \
    "passwordEncoder.encode"

# Security Components
check_content "blog-service/src/main/java/com/perfect8/blog/security/JwtTokenProvider.java" \
    "public String generateToken" \
    "public String getUsernameFromToken" \
    "public boolean validateToken" \
    "Jwts.builder()"

check_content "blog-service/src/main/java/com/perfect8/blog/security/JwtAuthenticationFilter.java" \
    "extends OncePerRequestFilter" \
    "doFilterInternal" \
    "Bearer "

# Controllers
check_content "blog-service/src/main/java/com/perfect8/blog/controller/PostController.java" \
    "@RestController" \
    "@GetMapping.*public" \
    "@PostMapping.*create" \
    "@AuthenticationPrincipal"

check_content "blog-service/src/main/java/com/perfect8/blog/controller/AuthController.java" \
    "@PostMapping.*login" \
    "@PostMapping.*register" \
    "ResponseEntity<AuthDto.JwtResponse>"

# Repositories
check_content "blog-service/src/main/java/com/perfect8/blog/repository/UserRepository.java" \
    "extends JpaRepository<User, Long>" \
    "Optional<User> findByUsername" \
    "boolean existsByEmail"

check_content "blog-service/src/main/java/com/perfect8/blog/repository/RoleRepository.java" \
    "extends JpaRepository<Role, Long>" \
    "Optional<Role> findByName"

# Application Properties
check_content "blog-service/src/main/resources/application.properties" \
    "spring.datasource.url=" \
    "jwt.secret=" \
    "spring.jpa.hibernate.ddl-auto=update"

echo -e "\n${BLUE}=== Checking Image Service Files ===${NC}"

# Image Service Main Files
check_content "image-service/pom.xml" \
    "<artifactId>image-service</artifactId>" \
    "spring-boot-starter-web" \
    "spring-boot-starter-data-jpa"

# Main Application
check_content "image-service/src/main/java/com/perfect8/image/ImageServiceApplication.java" \
    "@SpringBootApplication" \
    "public class ImageServiceApplication"

# Model
check_content "image-service/src/main/java/com/perfect8/image/model/Image.java" \
    "@Entity" \
    "@GeneratedValue.*UUID" \
    "@Lob" \
    "private byte\[\] data;"

# Service
check_content "image-service/src/main/java/com/perfect8/image/service/ImageService.java" \
    "public ImageDto uploadImage" \
    "validateFile" \
    "file.getBytes()" \
    "Arrays.asList.*getAllowedExtensions"

# Controller
check_content "image-service/src/main/java/com/perfect8/image/controller/ImageController.java" \
    "@PostMapping.*upload" \
    "@GetMapping.*view" \
    "ResponseEntity<byte\[\]>" \
    "MediaType.parseMediaType"

# Configuration
check_content "image-service/src/main/resources/application.properties" \
    "server.port=8081" \
    "storage.allowed-extensions=jpg,jpeg,png,gif,webp"

echo -e "\n${BLUE}=== Checking Test Files ===${NC}"

check_content "blog-service/src/test/java/com/perfect8/blog/service/PostServiceTest.java" \
    "@ExtendWith.*MockitoExtension" \
    "@Mock" \
    "@Test" \
    "void createPost_Success"

check_content "image-service/src/test/java/com/perfect8/image/service/ImageServiceTest.java" \
    "MockMultipartFile" \
    "@Test" \
    "uploadImage_Success"

echo -e "\n${BLUE}=== Checking File Sizes ===${NC}"

# Check for suspiciously small files (might indicate truncation)
check_min_size "blog-service/src/main/java/com/perfect8/blog/service/PostService.java" 3000
check_min_size "blog-service/src/main/java/com/perfect8/blog/model/Post.java" 2000
check_min_size "docker-compose.yml" 1000

echo -e "\n${BLUE}=== Additional Validation ===${NC}"

# Check for TODO or FIXME comments that might indicate incomplete code
echo -e "\n${YELLOW}Checking for TODO/FIXME comments...${NC}"
if grep -r "TODO\|FIXME" blog-service/src image-service/src 2>/dev/null; then
    echo -e "${YELLOW}Found TODO/FIXME comments - review these areas${NC}"
fi

# Check for proper package declarations
echo -e "\n${YELLOW}Verifying package declarations...${NC}"
find blog-service/src/main/java -name "*.java" | while read file; do
    expected_package=$(echo $file | sed 's|blog-service/src/main/java/||' | sed 's|/[^/]*\.java$||' | tr '/' '.')
    if ! grep -q "package $expected_package;" "$file" 2>/dev/null; then
        echo -e "${RED}✗${NC} Wrong package in: $file"
        ((ISSUES_COUNT++))
    fi
done

echo -e "\n${BLUE}==========================================${NC}"
if [ $ISSUES_COUNT -eq 0 ]; then
    echo -e "${GREEN}All content checks passed!${NC}"
    echo -e "${GREEN}Your project appears to be complete.${NC}"
else
    echo -e "${RED}Found $ISSUES_COUNT potential issues!${NC}"
    echo -e "${YELLOW}Please review the files marked with ✗ or ⚠${NC}"
fi

# Final integrity check
echo -e "\n${BLUE}=== Quick Integrity Summary ===${NC}"
echo -n "Total Java files in blog-service: "
find blog-service/src -name "*.java" | wc -l
echo -n "Total Java files in image-service: "
find image-service/src -name "*.java" | wc -l
echo -n "Total properties files: "
find . -name "*.properties" | wc -l
// blog-service/src/main/java/com/perfect8/blog/dto/UserDto.java

package com.perfect8.blog.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class UserDto {
    private Long userDtoId;
    private String username;
    private String email;
    private Set<String> roles;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    // Getters and setters
    public Long getUserDtoId() { return userDtoId; }
    public void setUserDtoId(Long userDtoId) { this.userDtoId = userDtoId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
}
// blog-service/src/main/java/com/perfect8/blog/service/UserService.java
//

        package com.perfect8.blog.service;

import com.perfect8.blog.dto.UserDto;
import com.perfect8.blog.exception.ResourceNotFoundException;
import com.perfect8.blog.model.User;
import com.perfect8.blog.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return convertToDto(user);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRoles(user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet()));
        dto.setCreatedAt(user.getCreatedDate());
        dto.setUpdatedAt(user.getUpdatedDate());
        return dto;
    }
}

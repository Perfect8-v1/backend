// blog-service/src/main/java/com/perfect8/blog/service/AdminService.java
//

        package com.perfect8.blog.service;

import com.perfect8.blog.dto.UserDto;
import com.perfect8.blog.model.User;
import com.perfect8.blog.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDto);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
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

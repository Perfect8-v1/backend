package com.perfect8.blog.controller;


import main.java.com.perfect8.blog.dto.JwtAuthResponseDto;
import main.java.com.perfect8.blog.dto.LoginDto;
import main.java.com.perfect8.blog.dto.RegisterDto;
import com.perfect8.blog.model.Role;
import com.perfect8.blog.model.User;
import com.perfect8.blog.repository.RoleRepository;
import com.perfect8.blog.repository.UserRepository;
import main.java.com.perfect8.blog.security.JwtTokenProvider_2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider_2 jwtTokenProvider;

    // Spring will automatically inject all these dependencies for us
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider_2 jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Handles POST requests to /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDto> authenticateUser(@RequestBody LoginDto loginDto){
        // Try to authenticate the user with the provided email and password
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(), loginDto.getPassword()));

        // If successful, set the authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate a JWT for the authenticated user
        String token = jwtTokenProvider.generateToken(authentication);

        // Return the token in the response body with a 200 OK status
        return ResponseEntity.ok(new JwtAuthResponseDto(token));
    }

    /**
     * Handles POST requests to /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterDto registerDto){
        // Check if a user with that email already exists
        if(userRepository.existsByEmail(registerDto.getEmail())){
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // Create a new User entity
        User user = new User();
        user.setName(registerDto.getName());
        user.setEmail(registerDto.getEmail());
        // IMPORTANT: Hash the password before saving it!
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        // Assign the default "USER" role to the new user
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Error: Default role 'ROLE_USER' not found in database."));
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        // Save the new user to the database
        userRepository.save(user);

        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }
}

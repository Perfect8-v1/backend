// blog-service/src/main/java/com/perfect8/blog/controller/AuthController.java

package com.perfect8.blog.controller;

/* VERSION 2.0 - COMMENTED OUT FOR CLEAN V1.0 RELEASE
 * 
 * This authentication controller will be uncommented and enhanced in version 2.0
 * 
 * Version 1.0 Strategy:
 * - All user authentication handled by admin-service
 * - blog-service focuses on content (posts, categories)
 * - No separate login/register endpoints needed
 * 
 * Version 2.0 Will Add:
 * - Blog-specific user authentication
 * - Comment system authentication
 * - Author profile management
 * 
 * Reason for commenting out:
 * - Eliminates dependency on AuthService.login() and register()
 * - Simplifies v1.0 deployment
 * - Prevents AuthenticationManager crash
 */

import com.perfect8.blog.dto.AuthDto;
import com.perfect8.blog.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * VERSION 1.0 - Health check endpoint
     * Confirms auth service is loaded and ready for v2.0
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        boolean healthy = authService.isServiceHealthy();
        return ResponseEntity.ok(healthy ? "Auth service ready for v2.0" : "Auth service unavailable");
    }

    /* VERSION 2.0 - User login endpoint
    @PostMapping("/login")
    public ResponseEntity<AuthDto.JwtResponse> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    */

    /* VERSION 2.0 - User registration endpoint
    @PostMapping("/register")
    public ResponseEntity<AuthDto.JwtResponse> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    */

    /**
     * VERSION 1.0 - Check username availability
     * Useful for future registration form validation
     */
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsernameAvailable(@PathVariable String username) {
        return ResponseEntity.ok(authService.isUsernameAvailable(username));
    }

    /**
     * VERSION 1.0 - Check email availability
     * Useful for future registration form validation
     */
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailAvailable(@PathVariable String email) {
        return ResponseEntity.ok(authService.isEmailAvailable(email));
    }
}

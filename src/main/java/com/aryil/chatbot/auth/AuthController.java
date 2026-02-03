package com.aryil.chatbot.auth;

import com.aryil.chatbot.auth.dto.AuthResponse;
import com.aryil.chatbot.auth.dto.LoginRequest;
import com.aryil.chatbot.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        String token = authService.register(req);
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req);
        return new AuthResponse(token);
    }
}

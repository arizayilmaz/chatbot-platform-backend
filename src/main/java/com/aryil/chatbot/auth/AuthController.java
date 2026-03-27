package com.aryil.chatbot.auth;

import com.aryil.chatbot.auth.dto.AuthResponse;
import com.aryil.chatbot.auth.dto.LoginRequest;
import com.aryil.chatbot.auth.dto.RegisterRequest;
import com.aryil.chatbot.common.dto.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "User authentication and registration")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with email and password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        String token = authService.register(req);
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and receive JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful", content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req);
        return new AuthResponse(token);
    }
}

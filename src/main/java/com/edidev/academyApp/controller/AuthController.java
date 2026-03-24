package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.JwtResponseDTO;
import com.edidev.academyApp.dto.LoginRequestDTO;
import com.edidev.academyApp.dto.RegisterRequestDTO;
import com.edidev.academyApp.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        JwtResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        JwtResponseDTO response = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // En JWT stateless, el logout se maneja en el frontend eliminando el token
        return ResponseEntity.ok().body("{\"message\": \"Logout exitoso. Elimina el token del cliente.\"}");
    }
}

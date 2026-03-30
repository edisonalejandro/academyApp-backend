package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.JwtResponseDTO;
import com.edidev.academyApp.dto.LoginRequestDTO;
import com.edidev.academyApp.dto.RegisterRequestDTO;
import com.edidev.academyApp.security.JwtUtils;
import com.edidev.academyApp.security.SecurityConfig;
import com.edidev.academyApp.security.UserDetailsServiceImpl;
import com.edidev.academyApp.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void login_withValidCredentials_shouldReturn200() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("admin@test.com");
        request.setPassword("admin123");

        JwtResponseDTO response = new JwtResponseDTO(
                "token.jwt.aqui", 1L, "admin@test.com", "Admin", "Test", List.of("ADMIN"));
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token.jwt.aqui"))
                .andExpect(jsonPath("$.email").value("admin@test.com"));

        verify(authService).login(any(LoginRequestDTO.class));
    }

    @Test
    void register_withValidData_shouldReturn201() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("nuevo@test.com");
        request.setPassword("password123");
        request.setFirstName("Juan");
        request.setLastName("Pérez");

        JwtResponseDTO response = new JwtResponseDTO(
                "token.jwt.registro", 2L, "nuevo@test.com", "Juan", "Pérez", List.of("STUDENT"));
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("nuevo@test.com"))
                .andExpect(jsonPath("$.roles[0]").value("STUDENT"));

        verify(authService).register(any(RegisterRequestDTO.class));
    }

    @Test
    void login_withMissingFields_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void register_withMissingFields_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());

        verify(authService, never()).register(any());
    }
}

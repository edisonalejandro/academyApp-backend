package com.edidev.academyApp.controller;

import com.edidev.academyApp.security.JwtUtils;
import com.edidev.academyApp.security.SecurityConfig;
import com.edidev.academyApp.security.UserDetailsServiceImpl;
import com.edidev.academyApp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    // ===================== Tests sin autenticación =====================

    @Test
    void getAllUsers_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isUnauthorized());
    }

    // ===================== Tests con rol incorrecto =====================

    @Test
    @WithMockUser(roles = "STUDENT")
    void getAllUsers_withStudentRole_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    // ===================== Tests con rol correcto =====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_withAdminRole_shouldReturn200() throws Exception {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void getAllUsers_withTeacherRole_shouldReturn200() throws Exception {
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(Page.empty());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }
}

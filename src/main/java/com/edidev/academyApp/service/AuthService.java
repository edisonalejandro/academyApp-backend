package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.JwtResponseDTO;
import com.edidev.academyApp.dto.LoginRequestDTO;
import com.edidev.academyApp.dto.RegisterRequestDTO;
import com.edidev.academyApp.exception.DuplicateResourceException;
import com.edidev.academyApp.model.Role;
import com.edidev.academyApp.model.RoleName;
import com.edidev.academyApp.model.User;
import com.edidev.academyApp.repository.UserRepository;
import com.edidev.academyApp.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleService roleService;

    public JwtResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado."));

        // Obtener nombres de roles
        java.util.List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new JwtResponseDTO(jwt, user.getId(), user.getEmail(), 
                                 user.getFirstName(), user.getLastName(), roles);
    }

    @Transactional
    public JwtResponseDTO register(RegisterRequestDTO registerRequest) {
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new DuplicateResourceException("Error: El email ya está en uso!");
        }

        // Crear nuevo usuario
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setPhone(registerRequest.getPhone());
        user.setIsActive(true);

        // Asignar rol de estudiante por defecto
        Role studentRole = roleService.findByName(RoleName.STUDENT)
                .orElseThrow(() -> new RuntimeException("Error: Rol STUDENT no encontrado."));
        user.getRoles().add(studentRole);

        User savedUser = userRepository.save(user);

        // Generar token JWT
        String jwt = jwtUtils.generateTokenFromUsername(savedUser.getEmail());

        // Obtener nombres de roles
        java.util.List<String> roles = savedUser.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        return new JwtResponseDTO(jwt, savedUser.getId(), savedUser.getEmail(),
                                 savedUser.getFirstName(), savedUser.getLastName(), roles);
    }
}

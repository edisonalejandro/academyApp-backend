package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.UserDTO;
import com.edidev.academyApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs para gestión de usuarios")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @PageableDefault(size = 50, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or @userService.getUserById(#id).email == authentication.name")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener usuario por email")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or #email == authentication.name")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#id).email == authentication.name")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @Operation(summary = "Activar/Desactivar usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> toggleUserStatus(@PathVariable Long id) {
        UserDTO user = userService.toggleUserStatus(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar usuarios por nombre")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<UserDTO>> searchUsersByName(@RequestParam String name) {
        List<UserDTO> users = userService.searchUsersByName(name);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/active")
    @Operation(summary = "Obtener usuarios activos")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<UserDTO>> getActiveUsers() {
        List<UserDTO> users = userService.getActiveUsers();
        return ResponseEntity.ok(users);
    }
}

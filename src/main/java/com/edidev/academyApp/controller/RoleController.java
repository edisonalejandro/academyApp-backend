package com.edidev.academyApp.controller;

import com.edidev.academyApp.dto.UserDTO;
import com.edidev.academyApp.model.Role;
import com.edidev.academyApp.model.RoleName;
import com.edidev.academyApp.service.RoleService;
import com.edidev.academyApp.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs para gestión de roles")
public class RoleController {

    private final RoleService roleService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Obtener todos los roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = roleService.findAll();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener rol por ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return roleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/assign/{userId}")
    @Operation(summary = "Asignar rol a usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> assignRoleToUser(
            @PathVariable Long userId,
            @RequestParam RoleName roleName) {
        UserDTO updatedUser = userService.assignRole(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/remove/{userId}")
    @Operation(summary = "Remover rol de usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> removeRoleFromUser(
            @PathVariable Long userId,
            @RequestParam RoleName roleName) {
        UserDTO updatedUser = userService.removeRole(userId, roleName);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtener roles de un usuario")
    @PreAuthorize("hasRole('ADMIN') or @userService.getUserById(#userId).email == authentication.name")
    public ResponseEntity<Set<Role>> getUserRoles(@PathVariable Long userId) {
        Set<Role> roles = userService.getUserRoles(userId);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/users/{roleName}")
    @Operation(summary = "Obtener usuarios por rol")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable RoleName roleName) {
        List<UserDTO> users = userService.getUsersByRole(roleName);
        return ResponseEntity.ok(users);
    }
}

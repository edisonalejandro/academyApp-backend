package com.edidev.academyApp.service;

import com.edidev.academyApp.dto.UserDTO;
import com.edidev.academyApp.exception.ResourceNotFoundException;
import com.edidev.academyApp.exception.DuplicateResourceException;
import com.edidev.academyApp.model.Role;
import com.edidev.academyApp.model.RoleName;
import com.edidev.academyApp.model.User;
import com.edidev.academyApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    // Obtener todos los usuarios
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Obtener todos los usuarios con paginación
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDTO);
    }

    // Obtener usuario por ID
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return convertToDTO(user);
    }

    // Obtener usuario por email
    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
        return convertToDTO(user);
    }

    // Crear nuevo usuario
    public UserDTO createUser(UserDTO userDTO) {
        // Verificar si el email ya existe
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email: " + userDTO.getEmail());
        }

        User user = convertToEntity(userDTO);
        // Encriptar la contraseña antes de guardar
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Usar los roles del DTO si el admin los especificó; si no, asignar STUDENT por defecto
        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            for (RoleName roleName : userDTO.getRoles()) {
                Role role = roleService.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));
                user.getRoles().add(role);
            }
        } else {
            Role studentRole = roleService.findByName(RoleName.STUDENT)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol STUDENT no encontrado"));
            user.getRoles().add(studentRole);
        }
        
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    // Actualizar usuario
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        // Verificar si el email ya existe en otro usuario
        if (!existingUser.getEmail().equals(userDTO.getEmail()) 
            && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new DuplicateResourceException("Ya existe un usuario con el email: " + userDTO.getEmail());
        }

        existingUser.setFirstName(userDTO.getFirstName());
        existingUser.setLastName(userDTO.getLastName());
        existingUser.setEmail(userDTO.getEmail());
        existingUser.setPhone(userDTO.getPhone());
        
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        if (userDTO.getIsActive() != null) {
            existingUser.setIsActive(userDTO.getIsActive());
        }

        User updatedUser = userRepository.save(existingUser);
        return convertToDTO(updatedUser);
    }

    // Eliminar usuario
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado con id: " + id);
        }
        userRepository.deleteById(id);
    }

    // Activar/Desactivar usuario
    public UserDTO toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        
        user.setIsActive(!user.getIsActive());
        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    // Buscar usuarios por nombre
    public List<UserDTO> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Obtener usuarios activos
    public List<UserDTO> getActiveUsers() {
        return userRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Incluir roles
        Set<RoleName> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        dto.setRoles(roleNames);
        
        // NO incluir password en la respuesta por seguridad
        return dto;
    }

    private User convertToEntity(UserDTO dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // Se encriptará en el método create/update
        user.setPhone(dto.getPhone());
        user.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return user;
    }

    // Métodos para gestión de roles
    public UserDTO assignRole(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        
        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));
        
        user.getRoles().add(role);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO removeRole(Long userId, RoleName roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        
        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));
        
        user.getRoles().remove(role);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public Set<Role> getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));
        return user.getRoles();
    }

    public List<UserDTO> getUsersByRole(RoleName roleName) {
        Role role = roleService.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleName));
        
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getRoles().contains(role))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}

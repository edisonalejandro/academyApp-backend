package com.edidev.academyApp.service;

import com.edidev.academyApp.model.Role;
import com.edidev.academyApp.model.RoleName;
import com.edidev.academyApp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    public Optional<Role> findByName(RoleName name) {
        return roleRepository.findByName(name);
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    public boolean existsByName(RoleName name) {
        return roleRepository.existsByName(name);
    }

    // Método para inicializar roles por defecto
    public void initializeDefaultRoles() {
        if (!existsByName(RoleName.STUDENT)) {
            save(new Role(RoleName.STUDENT, "Estudiante con acceso a cursos"));
        }
        if (!existsByName(RoleName.TEACHER)) {
            save(new Role(RoleName.TEACHER, "Profesor con capacidad de gestionar cursos"));
        }
        if (!existsByName(RoleName.ADMIN)) {
            save(new Role(RoleName.ADMIN, "Administrador con acceso completo"));
        }
    }
}

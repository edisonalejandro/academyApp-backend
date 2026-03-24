package com.edidev.academyApp.repository;

import com.edidev.academyApp.model.Role;
import com.edidev.academyApp.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    Optional<Role> findByName(RoleName name);
    
    boolean existsByName(RoleName name);
}

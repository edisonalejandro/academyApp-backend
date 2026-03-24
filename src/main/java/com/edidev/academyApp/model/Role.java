package com.edidev.academyApp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private RoleName name;

    @Column(name = "description")
    private String description;

    public Role(RoleName name) {
        this.name = name;
    }

    public Role(RoleName name, String description) {
        this.name = name;
        this.description = description;
    }
}

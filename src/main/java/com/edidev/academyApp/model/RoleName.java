package com.edidev.academyApp.model;

public enum RoleName {
    STUDENT("Estudiante", "Estudiante con acceso a cursos y materiales"),
    TEACHER("Profesor", "Profesor con capacidad de gestionar cursos y estudiantes"),
    ADMIN("Administrador", "Administrador con acceso completo al sistema");

    private final String displayName;
    private final String description;

    RoleName(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}

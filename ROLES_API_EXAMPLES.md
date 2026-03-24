# Gestión de Roles - API Examples

## 🔐 Sistema de Roles Implementado

El sistema ahora incluye tres roles principales:
- **STUDENT**: Estudiante con acceso a cursos y materiales
- **TEACHER**: Profesor con capacidad de gestionar cursos y estudiantes  
- **ADMIN**: Administrador con acceso completo al sistema

## 👨‍💼 Usuario Administrador por Defecto

```
Email: admin@academy.com
Password: admin123
Roles: [ADMIN]
```

---

## 📋 APIs Disponibles

### 1. **Autenticación con Roles**

#### Login del Administrador
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
    "email": "admin@academy.com",
    "password": "admin123"
}
```

**Respuesta:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "email": "admin@academy.com",
    "roles": ["ADMIN"]
}
```

#### Registro de Nuevo Estudiante
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@email.com",
    "password": "password123"
}
```

**Respuesta:** El usuario se crea automáticamente con rol `STUDENT`

---

### 2. **Gestión de Roles** (Requiere autenticación)

#### Obtener Todos los Roles
```bash
GET http://localhost:8080/api/roles
Authorization: Bearer {token_admin}
```

#### Asignar Rol a Estudiante
```bash
POST http://localhost:8080/api/roles/assign/{studentId}?roleName=TEACHER
Authorization: Bearer {token_admin}
```

#### Remover Rol de Estudiante
```bash
POST http://localhost:8080/api/roles/remove/{studentId}?roleName=TEACHER
Authorization: Bearer {token_admin}
```

#### Obtener Roles de un Estudiante
```bash
GET http://localhost:8080/api/roles/student/{studentId}
Authorization: Bearer {token_admin}
```

#### Obtener Estudiantes por Rol
```bash
GET http://localhost:8080/api/roles/students/STUDENT
Authorization: Bearer {token_admin}
```

---

### 3. **Endpoints Protegidos por Roles**

#### Ver Todos los Estudiantes (Solo ADMIN y TEACHER)
```bash
GET http://localhost:8080/api/students
Authorization: Bearer {token_admin_o_teacher}
```

#### Crear Estudiante (Solo ADMIN)
```bash
POST http://localhost:8080/api/students
Authorization: Bearer {token_admin}
Content-Type: application/json

{
    "firstName": "María",
    "lastName": "González",
    "email": "maria.gonzalez@email.com",
    "password": "password123",
    "phone": "555-1234"
}
```

---

## 🔧 Permisos por Rol

### STUDENT
- ✅ Acceso a sus propios datos
- ✅ Ver cursos disponibles
- ❌ No puede gestionar otros usuarios

### TEACHER  
- ✅ Ver lista de estudiantes
- ✅ Gestionar cursos
- ✅ Ver estadísticas de estudiantes
- ❌ No puede gestionar roles

### ADMIN
- ✅ Acceso completo al sistema
- ✅ Gestionar usuarios y roles
- ✅ Ver todas las estadísticas
- ✅ Configurar el sistema

---

## 🧪 Ejemplos de Prueba

### Paso 1: Login como Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@academy.com",
    "password": "admin123"
  }'
```

### Paso 2: Registrar un Estudiante
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Ana",
    "lastName": "Torres",
    "email": "ana.torres@email.com",
    "password": "password123"
  }'
```

### Paso 3: Asignar Rol de TEACHER al Estudiante
```bash
curl -X POST "http://localhost:8080/api/roles/assign/2?roleName=TEACHER" \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

### Paso 4: Verificar Roles del Usuario
```bash
curl -X GET http://localhost:8080/api/roles/student/2 \
  -H "Authorization: Bearer {ADMIN_TOKEN}"
```

---

## 📊 Respuesta de Estudiante con Roles

Cuando consultas un estudiante, ahora la respuesta incluye sus roles:

```json
{
    "id": 1,
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@email.com",
    "phone": "555-1234",
    "isActive": true,
    "roles": ["STUDENT", "TEACHER"],
    "createdAt": "2025-01-24T10:30:00",
    "updatedAt": "2025-01-24T10:30:00"
}
```

---

## 🛡️ Seguridad Implementada

1. **JWT con Roles**: Los tokens JWT ahora incluyen los roles del usuario
2. **Method Security**: Endpoints protegidos con `@PreAuthorize`
3. **Role-Based Access**: Diferentes niveles de acceso según el rol
4. **Inicialización Automática**: Roles y admin creados al iniciar la aplicación

---

## 🚀 Próximos Pasos

Con este sistema de roles implementado, puedes ahora:

1. **Crear diferentes tipos de usuarios**
2. **Implementar Course y Teacher entities**
3. **Agregar dashboard específico por rol**
4. **Implementar sistema de inscripciones**
5. **Agregar reportes y estadísticas por rol**

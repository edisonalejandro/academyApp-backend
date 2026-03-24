# 📊 Análisis del Proyecto - Academia de Baile Backend

**Fecha de análisis:** 23 de marzo de 2026  
**Versión:** 0.0.1-SNAPSHOT  
**Framework:** Spring Boot 3.5.3  
**Java:** 17

---

## 📋 Resumen Ejecutivo

Este proyecto es el backend de una aplicación web para gestión de una academia de baile. Permite administrar usuarios (estudiantes, profesores, administradores), cursos, inscripciones, pagos, asistencias y sesiones de clase.

### Estado Actual
✅ **Operativo** - Aplicación corriendo exitosamente en puerto 8080  
✅ Base de datos MySQL configurada y funcionando  
✅ JWT Authentication implementado  
✅ Swagger UI disponible en `/swagger-ui.html`

---

## 🏗️ Arquitectura Técnica

### Stack Tecnológico

#### Backend Framework
- **Spring Boot**: 3.5.3
- **Java**: 17
- **Maven**: Gestión de dependencias

#### Base de Datos
- **MySQL**: 8.0.33
- **JPA/Hibernate**: 6.6.18
- **Flyway**: Migraciones de base de datos
- **Database**: `academia_baile`
- **Puerto**: 3306

#### Seguridad
- **Spring Security**: Autenticación y autorización
- **JWT**: Tokens stateless
- **BCrypt**: Encriptación de contraseñas
- **Expiración Token**: 24 horas (86400000 ms)

#### Documentación
- **Swagger/OpenAPI**: `/api-docs`
- **Swagger UI**: `/swagger-ui.html`

#### Utilidades
- **Lombok**: Reducción de código boilerplate
- **Validation**: Jakarta Validation
- **H2 Console**: `/h2-console` (disponible)

---

## 📦 Estructura del Proyecto

```
src/main/java/com/edidev/academyApp/
├── AcademyAppApplication.java        # Clase principal
├── config/                            # Configuraciones (Security, CORS, etc.)
├── controller/                        # Controladores REST
│   ├── AuthController.java           ✅ Autenticación
│   ├── UserController.java           ✅ Gestión de usuarios
│   ├── RoleController.java           ✅ Gestión de roles
│   ├── PaymentController.java        ✅ Procesamiento de pagos
│   ├── PricingController.java        ✅ Gestión de precios
│   └── TestController.java           ✅ Testing/Health checks
├── dto/                               # Data Transfer Objects (16 DTOs)
├── enums/                             # Enumeraciones (10 enums)
├── exception/                         # Manejo de excepciones
├── model/                             # Entidades JPA
├── repository/                        # Repositorios JPA
├── security/                          # Configuración de seguridad
└── service/                           # Lógica de negocio

resources/
├── application.properties             # Configuración principal
├── db/migration/                      # Scripts Flyway
└── static/ & templates/               # Recursos estáticos
```

---

## 🗄️ Modelo de Datos

### Entidades Principales (10 entidades)

#### 1. **User** - Usuarios del sistema
- **Campos**: id, firstName, lastName, email, password, phone, isActive
- **Relaciones**: 
  - ManyToMany con `Role`
  - OneToOne con `Student`
- **Timestamps**: createdAt, updatedAt

#### 2. **Role** - Roles del sistema
- **Valores**: ADMIN, TEACHER, STUDENT
- **Tabla**: `roles`
- **Join Table**: `user_roles`

#### 3. **Student** - Información detallada de estudiantes
- **Campos personales**: firstName, lastName, email, phone, dateOfBirth, address
- **Campos académicos**: category, status, universityName, studentId, career, semester
- **Campos médicos**: medicalConditions, allergies, medications, danceExperience, fitnessLevel
- **Relaciones**:
  - OneToOne con `User`
  - OneToMany con `Enrollment`
  - OneToMany con `Attendance`

#### 4. **Course** - Cursos de baile
- **Campos**: title, code, description, danceType, level, pricePerHour, durationHours, maxCapacity
- **Relaciones**:
  - ManyToOne con `User` (teacher)
  - OneToMany con `Enrollment`, `ClassSession`, `Payment`

#### 5. **Enrollment** - Inscripciones de estudiantes a cursos
- **Campos**: purchasedHours, usedHours, totalPaid, status
- **Relaciones**:
  - ManyToOne con `Student`
  - ManyToOne con `Course`
  - ManyToOne con `Payment`
- **Métodos calculados**: getRemainingHours()

#### 6. **ClassSession** - Sesiones de clase
- **Campos**: sessionDate, startTime, endTime, status, topic, notes
- **Relaciones**:
  - ManyToOne con `Course`
  - OneToMany con `Attendance`

#### 7. **Attendance** - Asistencia a clases
- **Campos**: attended, excused, notes
- **Relaciones**:
  - ManyToOne con `Student`
  - ManyToOne con `ClassSession`

#### 8. **Payment** - Pagos realizados
- **Campos**: amount, finalPrice, paymentCode, paymentMethod, status, notes
- **Relaciones**:
  - ManyToOne con `User`
  - ManyToOne con `Course`
  - ManyToOne con `PricingRule`

#### 9. **PricingRule** - Reglas de precios
- **Campos**: name, description, pricingType, studentCategory, numberOfClasses, basePrice, discountPercentage, finalPrice
- **Campos de validez**: isActive, validFrom, validUntil, minPersons, maxPersons

#### 10. **StudentDiscount** - Descuentos especiales para estudiantes
- **Campos**: discount, validFrom, validUntil, criteria

---

## 🔐 Sistema de Autenticación y Autorización

### Roles del Sistema
1. **ADMIN** - Administrador del sistema
2. **TEACHER** - Profesor
3. **STUDENT** - Estudiante

### Endpoints de Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/logout` - Cerrar sesión (frontend elimina token)

### Flujo de Autenticación
1. Usuario envía credenciales a `/api/auth/login`
2. Sistema valida y genera JWT token
3. Token se incluye en header: `Authorization: Bearer {token}`
4. Token expira en 24 horas

---

## 📡 Controladores Implementados

### 1. **AuthController** (`/api/auth`)
- ✅ Login de usuarios
- ✅ Registro de nuevos usuarios
- ✅ Logout (stateless)

### 2. **UserController** (`/api/users`)
- ✅ CRUD completo de usuarios
- ✅ Búsqueda por nombre y email
- ✅ Activar/Desactivar usuarios
- ✅ Listar usuarios activos
- **Permisos**: ADMIN, TEACHER (lectura), propio usuario

### 3. **RoleController** (`/api/roles`)
- ✅ Listar todos los roles
- ✅ Asignar/Remover roles a usuarios
- ✅ Obtener roles de un usuario
- ✅ Listar usuarios por rol
- **Permisos**: ADMIN (gestión), usuarios consultan sus propios roles

### 4. **PaymentController** (`/api/payments`)
Funcionalidades:
- ✅ Cálculo de precios (GET/POST `/pricing/calculate`)
- ✅ Cálculo flexible (`/pricing/flexible`)
- ✅ Cálculo por regla específica (`/pricing/rule/{id}`)
- ✅ Procesamiento de pagos (`POST /process`)
- ✅ Validación de pagos (`POST /validate`)
- ✅ Consulta por código (`/status/{code}`)
- ✅ Mis pagos (`/my-payments`)
- ✅ Pagos por curso (`/course/{id}`)
- ✅ Actualizar estado (`PATCH /{id}/status`)
- ✅ Procesar reembolso (`POST /{id}/refund`)
- ✅ Reportes de ingresos (`/reports/revenue`)

### 5. **PricingController** (`/api/pricing`)
Funcionalidades:
- ✅ Cálculo de opciones de precios (`GET/POST /calculate`)
- ✅ Cotización rápida (`/quick-quote`)
- ✅ CRUD de reglas de precios
- ✅ Activar/Desactivar reglas
- ✅ Validación de reglas
- ✅ Validación en lote
- ✅ Reportes y estadísticas

### 6. **TestController** (`/api/test`)
- ✅ Health check endpoints
- ✅ Testing de conectividad

---

## 🔢 Enumeraciones del Sistema

### 1. **StudentCategory**
- REGULAR - Cliente regular
- UNIVERSITY - Estudiante universitario (descuento)
- COUPLE - Pareja
- SENIOR - Adulto mayor
- CHILD - Niño

### 2. **EnrollmentStatus**
- PENDING - Pendiente
- ACTIVE - Activa
- COMPLETED - Completada
- CANCELLED - Cancelada
- SUSPENDED - Suspendida
- TRANSFERRED - Transferida
- HOURS_EXHAUSTED - Horas agotadas

### 3. **PaymentStatus**
- PENDING - Pendiente
- COMPLETED - Completado
- FAILED - Fallido
- REFUNDED - Reembolsado
- CANCELLED - Cancelado

### 4. **PaymentMethod**
- CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, etc.

### 5. **DanceType**
- SALSA, BACHATA, MERENGUE, TANGO, etc.

### 6. **DanceLevel**
- BEGINNER, INTERMEDIATE, ADVANCED, PROFESSIONAL

### 7. **StudentStatus**
- ACTIVE, INACTIVE, SUSPENDED, GRADUATED

### 8. **PricingType**
- PER_CLASS, MONTHLY, PACKAGE, UNLIMITED, etc.

### 9. **ClassStatus**
- SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED

### 10. **CourseStatus**
- ACTIVE, INACTIVE, FULL, CANCELLED

---

## 🚀 Funcionalidades Implementadas

### ✅ Completadas

#### Autenticación y Usuarios
- [x] Sistema de registro y login con JWT
- [x] Gestión completa de usuarios (CRUD)
- [x] Sistema de roles y permisos
- [x] Activar/Desactivar usuarios
- [x] Búsqueda y filtrado de usuarios

#### Sistema de Pagos
- [x] Cálculo dinámico de precios
- [x] Múltiples opciones de pricing
- [x] Procesamiento de pagos
- [x] Gestión de estados de pago
- [x] Sistema de reembolsos
- [x] Reportes de ingresos
- [x] Validación de pagos

#### Sistema de Precios
- [x] Reglas de precios flexibles
- [x] Descuentos por categoría de estudiante
- [x] Precios por número de clases
- [x] Activación/Desactivación de reglas
- [x] Validación de disponibilidad
- [x] Cotizaciones rápidas

#### Base de Datos
- [x] Migraciones Flyway configuradas
- [x] Relaciones entre entidades
- [x] Índices optimizados
- [x] Timestamps automáticos

#### Seguridad
- [x] Protección de endpoints por rol
- [x] Encriptación de contraseñas
- [x] Tokens JWT con expiración
- [x] CORS configurado

---

## 🔨 Pendientes de Implementación

### 📌 Controladores Faltantes

#### 1. **CourseController** (`/api/courses`) - PENDIENTE
Funcionalidades necesarias:
- [ ] CRUD de cursos
- [ ] Listar cursos por tipo de baile
- [ ] Listar cursos por nivel
- [ ] Listar cursos por profesor
- [ ] Verificar cupos disponibles
- [ ] Activar/Desactivar cursos
- [ ] Búsqueda y filtros

#### 2. **StudentController** (`/api/students`) - PENDIENTE
Funcionalidades necesarias:
- [ ] CRUD de estudiantes
- [ ] Perfil del estudiante
- [ ] Historial académico
- [ ] Información médica
- [ ] Estadísticas del estudiante
- [ ] Búsqueda y filtros

#### 3. **EnrollmentController** (`/api/enrollments`) - PENDIENTE
Funcionalidades necesarias:
- [ ] Inscribir estudiante a curso
- [ ] Cancelar inscripción
- [ ] Transferir inscripción
- [ ] Ver inscripciones activas
- [ ] Historial de inscripciones
- [ ] Gestión de horas (compradas/usadas)
- [ ] Actualizar estado

#### 4. **ClassSessionController** (`/api/class-sessions`) - PENDIENTE
Funcionalidades necesarias:
- [ ] Programar sesiones
- [ ] Cancelar/Reprogramar sesiones
- [ ] Listar sesiones por curso
- [ ] Calendario de sesiones
- [ ] Actualizar tema/notas de sesión

#### 5. **AttendanceController** (`/api/attendances`) - PENDIENTE
Funcionalidades necesarias:
- [ ] Registrar asistencia
- [ ] Modificar asistencia
- [ ] Reportes de asistencia por estudiante
- [ ] Reportes de asistencia por curso
- [ ] Estadísticas de asistencia
- [ ] Justificaciones de ausencias

---

## 📊 Repositorios Implementados

### Repositorios Existentes
- `UserRepository` - Consultas de usuarios
- `RoleRepository` - Gestión de roles
- `StudentRepository` - **findByUserId()**, **findByEmail()**
- `PaymentRepository` - Consultas de pagos
- `PricingRuleRepository` - Reglas de precios
- `EnrollmentRepository` - Consultas de inscripciones con navegación Student → User
- `CourseRepository` - Consultas de cursos
- `ClassSessionRepository` - Sesiones de clase
- `AttendanceRepository` - **Queries de estadísticas de asistencia**

### Queries Personalizadas Implementadas

#### EnrollmentRepository
```java
- findByUserIdOrderByCreatedAtDesc() - Usa e.student.user.id
- findByUserIdAndStatus() - Usa e.student.user.id
- findByUserIdAndCourseIdAndStatus() - Usa e.student.user.id
- findActiveEnrollmentsWithHours() - Calcula (purchasedHours - usedHours)
```

#### AttendanceRepository
```java
- getTopAttendanceStudents() - Top estudiantes por asistencia
- getStudentsWithLowAttendance() - Estudiantes con baja asistencia
```

---

## 🔧 Configuración del Proyecto

### application.properties
```properties
# Base de Datos
spring.datasource.url=jdbc:mysql://localhost:3306/academia_baile
spring.datasource.username=root

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Server
server.port=8080

# JWT
app.jwt.secret=mySecretKeyForJWTTokenGenerationAndValidationThatIsSecureEnough256Bits
app.jwt.expiration-ms=86400000

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### Perfiles de Configuración
- `application.properties` - Producción
- `application-test.properties` - Testing

---

## 🏗️ Migraciones de Base de Datos (Flyway)

### Scripts Ejecutados
- `V6__create_courses_table.sql`
- `V7__create_enrollments_table.sql`
- `V8__create_class_sessions_and_attendances_tables.sql`
- `V9__add_final_price_to_pricing_rules.sql`
- `V10__update_pricing_rules_with_final_prices.sql`
- `V11__create_students_table.sql`
- `V12__create_enrollments_table.sql`

---

## 🐛 Problemas Resueltos Recientemente

### 1. **Refactorización Student-User-Enrollment**
- **Problema**: Enrollment tenía relación directa con User
- **Solución**: Ahora Enrollment → Student → User
- **Impacto**: Todos los repositorios actualizados con navegación correcta

### 2. **JPA Query Aliases**
- **Problema**: Hibernate no permite aliases en HAVING/ORDER BY
- **Solución**: Expandir expresiones completas en queries

### 3. **Campos Calculados en Queries**
- **Problema**: getRemainingHours() no es campo de BD
- **Solución**: Usar (purchasedHours - usedHours) en JPQL

### 4. **Encoding Issues**
- **Problema**: BOM en StudentRepository.java
- **Solución**: Recrear archivo con encoding correcto

---

## 📈 Próximos Pasos Recomendados

### Prioridad Alta
1. **Implementar CourseController**
   - Vital para gestión de cursos
   - Permitir a profesores crear/gestionar cursos

2. **Implementar EnrollmentController**
   - Proceso de inscripción de estudiantes
   - Gestión de horas compradas/usadas

3. **Implementar AttendanceController**
   - Registro de asistencia por sesión
   - Reportes de asistencia

### Prioridad Media
4. **StudentController**
   - Gestión de perfil de estudiante
   - Información académica/médica

5. **ClassSessionController**
   - Programación de sesiones
   - Calendario de clases

### Prioridad Baja
6. **Mejoras en Sistema de Reportes**
   - Dashboard de administración
   - Exportación de reportes (PDF/Excel)

7. **Notificaciones**
   - Email notifications
   - Recordatorios de clases

8. **Sistema de Archivos**
   - Upload de documentos
   - Fotos de perfil

---

## 🧪 Testing

### Estado Actual
- ✅ Tests básicos incluidos
- ⚠️ Cobertura de tests limitada
- 📝 Necesita expansión

### Recomendaciones
- Implementar tests unitarios para servicios
- Tests de integración para endpoints
- Tests de seguridad
- Tests de rendimiento

---

## 📚 Documentación Adicional

### Archivos de Documentación Existentes
- `HELP.md` - Ayuda general
- `ROLES_API_EXAMPLES.md` - Ejemplos de API de roles
- `USER_REFACTORING_COMPLETE.md` - Refactorización de usuarios
- `Backend.txt` (en Documentacion/) - Notas de backend
- `schema_academyApp.json` (en Documentacion/) - Schema de BD

---

## ✅ Checklist de Completitud

### Infraestructura
- [x] Spring Boot configurado
- [x] Base de datos MySQL
- [x] JPA/Hibernate
- [x] Flyway migrations
- [x] Security con JWT
- [x] Swagger/OpenAPI
- [x] Logging configurado

### Módulos
- [x] Autenticación (100%)
- [x] Usuarios (100%)
- [x] Roles (100%)
- [x] Pagos (90%)
- [x] Pricing (90%)
- [ ] Cursos (0%)
- [ ] Estudiantes (20% - solo modelo)
- [ ] Inscripciones (20% - solo modelo)
- [ ] Sesiones de Clase (10% - solo modelo)
- [ ] Asistencias (10% - solo modelo)

### Progreso General: **~50% completado**

---

## 🎯 Conclusiones

El proyecto tiene una **base sólida** con:
- ✅ Arquitectura bien estructurada
- ✅ Sistema de autenticación robusto
- ✅ Modelo de datos completo y normalizado
- ✅ Sistema de pagos y precios funcional

**Necesita**:
- 🔨 Implementación de controladores faltantes
- 📝 Mayor cobertura de tests
- 📊 Sistema de reportes más completo
- 📧 Sistema de notificaciones

El proyecto está **listo para continuar desarrollo** de los módulos pendientes, siguiendo la misma arquitectura y patrones ya establecidos.

---

**Generado el:** 23 de marzo de 2026  
**Por:** GitHub Copilot AI Assistant

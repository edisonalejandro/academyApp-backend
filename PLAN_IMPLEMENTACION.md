# 📋 Plan de Implementación — Academia de Baile Backend

**Fecha**: 23 de marzo de 2026  
**Estado del proyecto**: Backend parcialmente implementado  
**Stack**: Spring Boot 3.5.11 | Java 21 | MySQL

---

## ✅ Estado Actual (qué ya tenemos)

| Módulo               | Servicio       | Controller     | Estado        |
| -------------------- | -------------- | -------------- | ------------- |
| Autenticación (JWT)  | ✅ Completo    | ✅ Completo    | ✅ Listo      |
| Usuarios             | ✅ Completo    | ✅ Completo    | ✅ Listo      |
| Roles                | ✅ Completo    | ✅ Completo    | ✅ Listo      |
| Pagos                | ✅ Completo    | ✅ Completo    | ✅ Listo      |
| Precios (Pricing)    | ✅ Completo    | ✅ Completo    | ✅ Listo      |
| Cursos               | ⚠️ Parcial     | ❌ Falta       | 🔧 En curso   |
| Estudiantes          | ❌ Vacío       | ❌ Falta       | ❌ Pendiente  |
| Inscripciones        | ⚠️ Parcial     | ❌ Falta       | 🔧 En curso   |
| Sesiones de clase    | ❌ No impl.    | ❌ Falta       | ❌ Pendiente  |
| Asistencias          | ⚠️ Interfaz    | ❌ Falta       | ❌ Pendiente  |

---

## 🗺️ Roadmap de Implementación

### 🔴 Fase 1 — Núcleo Operativo (Alta prioridad)

> Sin esto, el frontend no puede mostrar ni gestionar cursos ni estudiantes.

---

#### Tarea 1.1 — `CourseController` + completar `CourseService`

**Por qué primero**: Los cursos son el eje central. Los pagos ya funcionan pero referencian `courseId` sin que el frontend pueda listar/crear cursos.

**`CourseService`** — métodos que faltan:
- [ ] `createCourse(CourseDTO dto)` — validar código único, asignar `teacher`
- [ ] `updateCourse(Long id, CourseDTO dto)` — actualizar campos
- [ ] `deleteCourse(Long id)` — eliminación lógica (`isActive = false`)
- [ ] `toggleCourseStatus(Long id)` — activar / desactivar
- [ ] `getCoursesByTeacher(Long teacherId)` — cursos por profesor
- [ ] `getCoursesByDanceType(DanceType type)` — filtro por tipo de baile
- [ ] `getCoursesByLevel(DanceLevel level)` — filtro por nivel
- [ ] `getAvailableCapacity(Long courseId)` — cupos libres
- [ ] `toDTO(Course course)` / `toEntity(CourseDTO dto)` — mapeo

**`CourseController`** — endpoints a crear (`/api/courses`):

| Método   | Endpoint                          | Roles            | Descripción |
|----------|-----------------------------------|------------------|-------------|
| `GET`    | `/api/courses`                    | Todos            | Listar cursos activos |
| `GET`    | `/api/courses/{id}`               | Todos            | Curso por ID |
| `GET`    | `/api/courses/search?q=...`       | Todos            | Buscar por texto |
| `GET`    | `/api/courses/dance-type/{type}`  | Todos            | Por tipo de baile |
| `GET`    | `/api/courses/level/{level}`      | Todos            | Por nivel |
| `GET`    | `/api/courses/teacher/{id}`       | ADMIN, TEACHER   | Por profesor |
| `GET`    | `/api/courses/{id}/capacity`      | Todos            | Cupos disponibles |
| `POST`   | `/api/courses`                    | ADMIN            | Crear curso |
| `PUT`    | `/api/courses/{id}`               | ADMIN            | Actualizar curso |
| `DELETE` | `/api/courses/{id}`               | ADMIN            | Eliminar (lógico) |
| `PATCH`  | `/api/courses/{id}/toggle-status` | ADMIN            | Activar/Desactivar |

**DTO necesario** — `CourseDTO`:
```java
// Campos: id, title, code, description, danceType, level,
//         pricePerHour, durationHours, maxCapacity,
//         teacherId, teacherName, isActive,
//         imageUrl, prerequisites, objectives,
//         createdAt, updatedAt
```

---

#### Tarea 1.2 — `StudentService` + `StudentController`

**Por qué**: Sin el perfil de estudiante no hay inscripciones ni asistencias. `StudentService` está completamente vacío.

**`StudentService`** — todo por implementar:
- [ ] `createStudent(StudentDTO dto, Long userId)` — crea y vincula al `User`
- [ ] `getStudentByUserId(Long userId)` — perfil propio
- [ ] `getStudentById(Long id)` — por ID
- [ ] `updateStudent(Long id, StudentDTO dto)` — actualizar perfil
- [ ] `getAllStudents()` — listado ADMIN
- [ ] `getStudentsByStatus(StudentStatus)` — filtro por estado
- [ ] `getStudentsByCategory(StudentCategory)` — filtro por categoría
- [ ] `searchStudents(String term)` — búsqueda por nombre/email
- [ ] `getStudentEnrollmentSummary(Long studentId)` — resumen académico
- [ ] `toDTO(Student)` / `toEntity(StudentDTO)` — mapeo

**`StudentController`** — endpoints a crear (`/api/students`):

| Método  | Endpoint                           | Roles                     | Descripción |
|---------|------------------------------------|---------------------------|-------------|
| `GET`   | `/api/students`                    | ADMIN, TEACHER            | Todos los estudiantes |
| `GET`   | `/api/students/{id}`              | ADMIN, TEACHER, propio    | Por ID |
| `GET`   | `/api/students/me`                | STUDENT                   | Perfil propio |
| `GET`   | `/api/students/search?q=...`      | ADMIN, TEACHER            | Buscar |
| `GET`   | `/api/students/category/{cat}`    | ADMIN, TEACHER            | Por categoría |
| `GET`   | `/api/students/{id}/summary`      | ADMIN, TEACHER, propio    | Resumen académico |
| `POST`  | `/api/students`                   | ADMIN                     | Crear estudiante |
| `PUT`   | `/api/students/{id}`              | ADMIN, propio             | Actualizar perfil |
| `PATCH` | `/api/students/{id}/category`     | ADMIN                     | Cambiar categoría |

**DTO ya existe**: `StudentDTO` (archivo vacío — necesita llenarse con los campos del modelo `Student`).

---

#### Tarea 1.3 — `EnrollmentController` + completar `EnrollmentService`

**Por qué**: `EnrollmentService` tiene la lógica interna de creación (llamada desde pagos), pero falta la API REST para que el frontend consulte inscripciones.

**`EnrollmentService`** — métodos que faltan:
- [ ] `getEnrollmentById(Long id)` → `EnrollmentDTO`
- [ ] `getMyEnrollments(String userEmail)` — inscripciones del usuario autenticado
- [ ] `getEnrollmentsByCourse(Long courseId)` — para vista del profesor
- [ ] `getActiveEnrollments()` — todas las activas (ADMIN)
- [ ] `cancelEnrollment(Long id, String reason)` — cancelar inscripción
- [ ] `getEnrollmentSummary(Long enrollmentId)` → `EnrollmentSummaryDTO`

**`EnrollmentController`** — endpoints a crear (`/api/enrollments`):

| Método  | Endpoint                            | Roles                  | Descripción |
|---------|-------------------------------------|------------------------|-------------|
| `GET`   | `/api/enrollments/my`               | STUDENT, TEACHER, ADMIN | Mis inscripciones |
| `GET`   | `/api/enrollments/{id}`             | ADMIN, TEACHER, propio | Por ID |
| `GET`   | `/api/enrollments/course/{id}`      | ADMIN, TEACHER         | Por curso |
| `GET`   | `/api/enrollments/active`           | ADMIN                  | Todas las activas |
| `GET`   | `/api/enrollments/{id}/summary`     | ADMIN, TEACHER, propio | Resumen de horas |
| `PATCH` | `/api/enrollments/{id}/cancel`      | ADMIN, propio          | Cancelar |

---

### 🟡 Fase 2 — Operación Académica (Prioridad media)

> Necesario para la gestión diaria de clases.

---

#### Tarea 2.1 — `ClassSessionController` + `ClassSessionService`

`ClassSessionService` **no existe** — crear desde cero.

**`ClassSessionService`** — métodos:
- [ ] `createSession(ClassSessionDTO dto)` — programar sesión
- [ ] `getSessionById(Long id)`
- [ ] `getSessionsByCourse(Long courseId)` — calendario
- [ ] `getSessionsByDateRange(LocalDateTime from, LocalDateTime to)` — por fechas
- [ ] `getUpcomingSessions(Long courseId)` — próximas sesiones
- [ ] `updateSession(Long id, ClassSessionDTO dto)` — editar
- [ ] `cancelSession(Long id, String reason)` — cancelar
- [ ] `startSession(Long id)` — marcar como en curso
- [ ] `completeSession(Long id)` — marcar como completada
- [ ] `getSessionAttendanceSummary(Long sessionId)` — cuántos asistieron

**`ClassSessionController`** — endpoints (`/api/class-sessions`):

| Método  | Endpoint                                   | Roles          | Descripción |
|---------|--------------------------------------------|----------------|-------------|
| `GET`   | `/api/class-sessions/course/{courseId}`    | Todos autent.  | Por curso |
| `GET`   | `/api/class-sessions/{id}`                | Todos autent.  | Por ID |
| `GET`   | `/api/class-sessions/upcoming/{courseId}` | Todos autent.  | Próximas sesiones |
| `GET`   | `/api/class-sessions/calendar`            | Todos autent.  | Por rango de fechas |
| `POST`  | `/api/class-sessions`                     | ADMIN, TEACHER | Crear sesión |
| `PUT`   | `/api/class-sessions/{id}`                | ADMIN, TEACHER | Editar sesión |
| `PATCH` | `/api/class-sessions/{id}/cancel`         | ADMIN, TEACHER | Cancelar |
| `PATCH` | `/api/class-sessions/{id}/start`          | ADMIN, TEACHER | Iniciar sesión |
| `PATCH` | `/api/class-sessions/{id}/complete`       | ADMIN, TEACHER | Completar sesión |
| `GET`   | `/api/class-sessions/{id}/summary`        | ADMIN, TEACHER | Resumen asistencia |

**DTO necesario** — `ClassSessionDTO`:
```java
// Campos: id, courseId, courseName, teacherId, teacherName,
//         sessionName, description, scheduledDate,
//         actualStartTime, actualEndTime, plannedDuration,
//         actualDuration, status, notes, location,
//         createdAt, updatedAt
```

---

#### Tarea 2.2 — `AttendanceController` + implementar `AttendanceServiceImpl`

`AttendanceService` tiene la **interfaz definida** pero no tiene implementación (`AttendanceServiceImpl` no existe).

**`AttendanceServiceImpl`** — implementar todos los métodos de la interfaz:
- [ ] `recordAttendance(studentId, classSessionId, attended, notes)`
- [ ] `updateAttendance(attendanceId, attended, notes)`
- [ ] `getStudentAttendances(studentId)` — historial del estudiante
- [ ] `getClassAttendances(classSessionId)` — lista de una sesión
- [ ] `calculateStudentAttendanceRate(studentId)` — porcentaje general
- [ ] `generateAttendanceReport(startDate, endDate)` → `AttendanceReportDTO`
- [ ] `getStudentsWithLowAttendance(minPercentage)`
- [ ] `hasAttendanceRecord(studentId, classSessionId)`

**`AttendanceController`** — endpoints (`/api/attendances`):

| Método  | Endpoint                                         | Roles          | Descripción |
|---------|--------------------------------------------------|----------------|-------------|
| `POST`  | `/api/attendances`                               | ADMIN, TEACHER | Registrar asistencia |
| `PUT`   | `/api/attendances/{id}`                          | ADMIN, TEACHER | Actualizar asistencia |
| `GET`   | `/api/attendances/session/{sessionId}`           | ADMIN, TEACHER | Por sesión (lista) |
| `GET`   | `/api/attendances/student/{studentId}`           | ADMIN, TEACHER, propio | Por estudiante |
| `GET`   | `/api/attendances/student/{studentId}/rate`      | ADMIN, TEACHER, propio | % asistencia |
| `GET`   | `/api/attendances/report`                        | ADMIN, TEACHER | Reporte por fechas |
| `GET`   | `/api/attendances/low-attendance`                | ADMIN, TEACHER | Estudiantes bajo umbral |

**DTO necesario** — `AttendanceDTO`:
```java
// Campos: id, studentId, studentName, classSessionId,
//         sessionName, scheduledDate, attended, excused,
//         notes, createdAt, updatedAt
```

---

### 🟢 Fase 3 — Mejoras y Extras (Prioridad baja)

> Mejoran la experiencia pero no bloquean la operación básica.

---

#### Tarea 3.1 — Dashboard / Reportes para Admin

- [ ] `GET /api/admin/dashboard` — resumen: alumnos activos, ingresos del mes, cursos, sesiones próximas
- [ ] `GET /api/payments/reports/revenue` — ingresos por período (endpoint ya citado en análisis pero no implementado)
- [ ] `GET /api/payments/course/{courseId}` — pagos de un curso (mencionado, verificar si existe)

---

#### Tarea 3.2 — Tests unitarios e integración

Estado actual: **1 test** (`contextLoads`). Objetivo mínimo: **cobertura >70%** en servicios.

- [ ] Tests para `AuthService` (login, registro, token inválido)
- [ ] Tests para `CourseService` (CRUD, validaciones)
- [ ] Tests para `EnrollmentService` (crear, cancelar, horas)
- [ ] Tests para `PaymentService` (calcular precio, procesar pago)
- [ ] Tests para `AttendanceService` (registrar, calcular %)
- [ ] Tests de integración por controller (mockMvc)

---

#### Tarea 3.3 — Seguridad y Configuración

- [ ] Cambiar JWT secret de `application.properties` a variable de entorno (`${JWT_SECRET}`)
- [ ] Configurar CORS para orígenes específicos (reemplazar `"*"` por URL del frontend en producción)
- [ ] Añadir `spring.jpa.show-sql=false` para perfil de producción
- [ ] Agregar perfil `prod` en `application-prod.properties`
- [ ] Monitorear CVE-2026-22733 y actualizar Spring Boot a 3.5.12+ cuando esté disponible

---

#### Tarea 3.4 — Funcionalidades Futuras

- [ ] **Notificaciones por email** — recordatorios de clase, confirmación de inscripción (JavaMailSender)
- [ ] **Export PDF/Excel** — reportes de asistencia y pagos
- [ ] **Upload de imágenes** — fotos de perfil y cursos (multipart)
- [ ] **Paginación** — añadir `Pageable` a los endpoints de listado con muchos registros

---

## 📅 Orden de ejecución sugerido

```
Semana 1 ─── Tarea 1.1: CourseController + CourseService completo
             Tarea 1.2: StudentDTO + StudentService + StudentController

Semana 2 ─── Tarea 1.3: EnrollmentController + EnrollmentService (endpoints REST)
             Tarea 2.1: ClassSessionService + ClassSessionController

Semana 3 ─── Tarea 2.2: AttendanceServiceImpl + AttendanceController
             Tarea 3.1: Dashboard / Reportes básicos

Semana 4 ─── Tarea 3.2: Testing (cobertura >70%)
             Tarea 3.3: Seguridad y configuración de producción
```

---

## 🧩 Dependencias entre tareas

```
CourseController
    └── requiere: CourseService completo, CourseDTO

StudentController
    └── requiere: StudentService, StudentDTO (rellenar)

EnrollmentController
    └── requiere: EnrollmentService (métodos REST), Student y Course ya operativos

ClassSessionController
    └── requiere: ClassSessionService (nuevo), Course ya operativo

AttendanceController
    └── requiere: AttendanceServiceImpl, ClassSession y Student ya operativos

Dashboard/Reportes
    └── requiere: Todos los módulos anteriores operativos
```

---

## 📁 Archivos a crear / modificar

### Crear desde cero
- `src/.../controller/CourseController.java`
- `src/.../controller/StudentController.java`
- `src/.../controller/EnrollmentController.java`
- `src/.../controller/ClassSessionController.java`
- `src/.../controller/AttendanceController.java`
- `src/.../service/impl/AttendanceServiceImpl.java`
- `src/.../service/impl/EnrollmentServiceImpl.java` (mover lógica existente)
- `src/.../service/ClassSessionService.java` (interfaz)
- `src/.../service/impl/ClassSessionServiceImpl.java`
- `src/.../dto/CourseDTO.java`
- `src/.../dto/ClassSessionDTO.java`
- `src/.../dto/AttendanceDTO.java`

### Modificar / completar
- `src/.../service/CourseService.java` — añadir métodos faltantes
- `src/.../service/StudentService.java` — está vacío, implementar completo
- `src/.../service/EnrollmentService.java` — añadir métodos REST faltantes
- `src/.../dto/StudentDTO.java` — está vacío, rellenar campos del modelo

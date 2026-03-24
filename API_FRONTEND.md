# 🎓 Academia de Baile — Documentación de API para Frontend

**Versión backend**: Spring Boot 3.5.11 | Java 21  
**Base de datos**: PostgreSQL  
**Fecha**: 23 de marzo de 2026  
**Fase 2 añadida**: Sesiones de clase y Asistencias

---

## 📌 Configuración Base

| Parámetro       | Valor                          |
| --------------- | ------------------------------ |
| Base URL        | `http://localhost:8080`        |
| Prefijo API     | `/api`                         |
| Formato         | JSON (`application/json`)      |
| CORS            | Todos los orígenes permitidos  |
| Swagger UI      | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON    | `http://localhost:8080/api-docs` |

---

## 🔐 Autenticación

### Tipo de autenticación
JWT (JSON Web Token) — stateless.

### Duración del token
**24 horas** (86 400 000 ms). Tras expirar, el usuario debe volver a hacer login.

### Cómo incluir el token

En todas las peticiones que requieran autenticación, añadir el header:

```
Authorization: Bearer <token>
```

### Flujo completo

```
1. POST /api/auth/login  →  recibe { token, id, email, firstName, lastName, roles }
2. Guardar token en localStorage / sessionStorage / cookie segura
3. Adjuntar header Authorization en cada request protegido
4. Al expirar (401), redirigir al login y limpiar el token almacenado
```

---

## 📡 Endpoints por módulo

### 1. Autenticación — `/api/auth`

> Sin autenticación requerida

#### `POST /api/auth/login`

**Request**
```json
{
  "email": "usuario@ejemplo.com",
  "password": "contraseña123"
}
```

**Response 200**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "usuario@ejemplo.com",
  "firstName": "Juan",
  "lastName": "Pérez",
  "roles": ["ROLE_STUDENT"]
}
```

**Errores comunes**
| Código | Causa |
|--------|-------|
| 400    | Email inválido o contraseña menor a 6 caracteres |
| 401    | Credenciales incorrectas |

---

#### `POST /api/auth/register`

**Request**
```json
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "usuario@ejemplo.com",
  "password": "contraseña123",
  "phone": "+56912345678"
}
```

> `phone` es opcional.

**Validaciones**
- `firstName` / `lastName`: entre 2 y 50 caracteres
- `email`: formato válido, único en el sistema
- `password`: entre 6 y 100 caracteres

**Response 201** — mismo formato que login (`JwtResponseDTO`)

---

#### `POST /api/auth/logout`

Stateless. Sólo eliminar el token del lado cliente.

**Response 200**
```json
{ "message": "Logout exitoso. Elimina el token del cliente." }
```

---

### 2. Usuarios — `/api/users`

> Requiere `Authorization: Bearer <token>`

| Método   | Endpoint                       | Roles permitidos                        | Descripción |
| -------- | ------------------------------ | --------------------------------------- | ----------- |
| `GET`    | `/api/users`                   | ADMIN, TEACHER                          | Todos los usuarios |
| `GET`    | `/api/users/{id}`              | ADMIN, TEACHER, propio usuario          | Usuario por ID |
| `GET`    | `/api/users/email/{email}`     | ADMIN, TEACHER, propio usuario          | Usuario por email |
| `GET`    | `/api/users/active`            | ADMIN, TEACHER                          | Sólo usuarios activos |
| `GET`    | `/api/users/search?name=...`   | ADMIN, TEACHER                          | Buscar por nombre |
| `POST`   | `/api/users`                   | ADMIN                                   | Crear usuario |
| `PUT`    | `/api/users/{id}`              | ADMIN, propio usuario                   | Actualizar usuario |
| `DELETE` | `/api/users/{id}`              | ADMIN                                   | Eliminar usuario |
| `PATCH`  | `/api/users/{id}/toggle-status`| ADMIN                                   | Activar / Desactivar |

**Objeto `UserDTO` (request / response)**
```json
{
  "id": 1,
  "firstName": "Juan",
  "lastName": "Pérez",
  "email": "juan@ejemplo.com",
  "password": "contraseña123",
  "phone": "+56912345678",
  "isActive": true,
  "createdAt": "2026-03-23T20:00:00",
  "updatedAt": "2026-03-23T20:00:00",
  "roles": ["ADMIN", "STUDENT"]
}
```

> **Nota**: `password` es **write-only** — nunca se devuelve en las respuestas.  
> `roles` es **read-only** — no se envía al crear/actualizar (usar `/api/roles`).

---

### 3. Roles — `/api/roles`

> Requiere `Authorization: Bearer <token>`

| Método | Endpoint                          | Roles permitidos      | Descripción |
| ------ | --------------------------------- | --------------------- | ----------- |
| `GET`  | `/api/roles`                      | ADMIN                 | Todos los roles |
| `GET`  | `/api/roles/{id}`                 | ADMIN                 | Rol por ID |
| `GET`  | `/api/roles/user/{userId}`        | ADMIN, propio usuario | Roles de un usuario |
| `GET`  | `/api/roles/users/{roleName}`     | ADMIN, TEACHER        | Usuarios por rol |
| `POST` | `/api/roles/assign/{userId}?roleName=ADMIN` | ADMIN    | Asignar rol |
| `POST` | `/api/roles/remove/{userId}?roleName=ADMIN` | ADMIN    | Remover rol |

**Valores de `roleName`** (query param)
```
ADMIN | TEACHER | STUDENT
```

---

### 4. Precios — `/api/pricing`

> Sin autenticación requerida (consultas públicas)

#### `GET /api/pricing/calculate`

Calcula las opciones de precio disponibles para una combinación de curso + categoría.

**Query params**
| Param           | Tipo            | Requerido | Descripción |
|-----------------|-----------------|-----------|-------------|
| `courseId`      | `Long`          | ✅        | ID del curso |
| `studentCategory` | `StudentCategory` | ✅   | Categoría del estudiante |
| `personCount`   | `Integer`       | No (def. 1) | 1 o 2 personas |

**Ejemplo**
```
GET /api/pricing/calculate?courseId=1&studentCategory=REGULAR&personCount=1
```

**Response 200 — `PricingCalculationDTO`**
```json
{
  "courseId": 1,
  "courseName": "Salsa Principiantes",
  "studentCategory": "REGULAR",
  "personCount": 1,
  "isCouple": false,
  "options": [
    {
      "pricingRuleId": 3,
      "name": "Paquete 4 Clases",
      "description": "4 clases de salsa",
      "pricingType": "PACKAGE_4",
      "classQuantity": 4,
      "originalPrice": 60000,
      "finalPrice": 54000,
      "discountPercentage": 10.0,
      "savings": 6000,
      "pricePerClass": 13500,
      "isRecommended": true,
      "validFrom": "2026-01-01T00:00:00",
      "validUntil": "2026-12-31T23:59:59"
    }
  ],
  "recommendedOptionId": 3,
  "calculatedAt": "2026-03-23T20:00:00",
  "hasDefaultPricing": false
}
```

---

#### `POST /api/pricing/calculate`

Misma lógica que GET, pero enviando los datos en el body.

**Request**
```json
{
  "courseId": 1,
  "studentCategory": "REGULAR",
  "personCount": 1
}
```

---

#### `GET /api/pricing/quick-quote`

Cotización rápida sin especificar curso.

**Query params**: `studentCategory`, `personCount` (opcional, def. 1)

**Ejemplo**
```
GET /api/pricing/quick-quote?studentCategory=UNIVERSITY&personCount=1
```

---

#### `GET /api/pricing/rules`

Lista todas las reglas de precios activas.

**Query params** (todos opcionales)
| Param           | Tipo            | Descripción |
|-----------------|-----------------|-------------|
| `studentCategory` | `StudentCategory` | Filtrar por categoría |
| `pricingType`   | `PricingType`   | Filtrar por tipo |
| `activeOnly`    | `Boolean`       | Default `true` |

**Ejemplo**
```
GET /api/pricing/rules?studentCategory=REGULAR&activeOnly=true
```

**Response 200** — lista de `PricingRuleDTO`
```json
[
  {
    "id": 3,
    "name": "Paquete 4 Clases Regular",
    "description": "Paquete básico para estudiantes regulares",
    "pricingType": "PACKAGE_4",
    "studentCategory": "REGULAR",
    "personCount": 1,
    "classQuantity": 4,
    "price": 60000,
    "discountPercentage": 10.0,
    "finalPrice": 54000,
    "validFrom": "2026-01-01T00:00:00",
    "validUntil": "2026-12-31T23:59:59",
    "isActive": true,
    "createdAt": "2026-01-01T00:00:00",
    "updatedAt": "2026-01-01T00:00:00"
  }
]
```

---

#### `GET /api/pricing/rules/{ruleId}`

Regla de precios específica por ID.

---

#### `GET /api/pricing/rules/available`

Lista de reglas disponibles para un curso, categoría y número de personas.

**Query params**: `courseId`, `studentCategory`, `personCount` (def. 1)

---

#### `GET /api/pricing/rules/category/{category}`

Reglas por categoría de estudiante. Query param opcional: `personCount`.

---

### 5. Pagos — `/api/payments`

> La mayoría de endpoints requiere `Authorization: Bearer <token>`

#### `GET /api/payments/pricing/calculate`
Alias del endpoint de precios. Mismos parámetros que `GET /api/pricing/calculate`.

---

#### `GET /api/payments/pricing/flexible`

Cálculo rápido sin especificar curso por número de clases.

**Query params**
| Param           | Tipo            | Default | Descripción |
|-----------------|-----------------|---------|-------------|
| `numberOfClasses` | `Integer`     | —       | Nro. de clases |
| `studentCategory` | `StudentCategory` | —   | Categoría |
| `isCouple`      | `Boolean`       | `false` | Si es pareja |

**Response 200**
```json
{
  "price": 54000,
  "currency": "CLP",
  "numberOfClasses": 4,
  "studentCategory": "REGULAR",
  "isCouple": false,
  "pricePerClass": 13500.00
}
```

---

#### `GET /api/payments/pricing/rule/{pricingRuleId}`

Calcula el precio final de una regla específica.

**Response 200**
```json
{
  "price": 54000,
  "currency": "CLP",
  "pricingRuleId": 3
}
```

---

#### `POST /api/payments/validate`

Valida los datos de un pago antes de procesarlo.

**Roles**: cualquier usuario autenticado (o anónimo)

**Request — `PaymentRequestDTO`**
```json
{
  "courseId": 1,
  "pricingRuleId": 3,
  "studentCategory": "REGULAR",
  "paymentMethod": "CASH",
  "personCount": 1,
  "notes": "Pago en recepción",
  "transactionId": null
}
```

**Response 200 (válido)**
```json
{
  "valid": true,
  "price": 54000,
  "currency": "CLP",
  "message": "Datos de pago válidos"
}
```

**Response 400 (inválido)**
```json
{
  "valid": false,
  "message": "La regla de precios no está disponible"
}
```

---

#### `POST /api/payments/process` 🔒

Procesa el pago y genera la inscripción.

**Roles requeridos**: `STUDENT` o `ADMIN`

**Request** — mismo formato que `/validate`

**Response 200 — `PaymentResponseDTO`**
```json
{
  "id": 10,
  "paymentCode": "PAY-20260323-ABC123",
  "studentName": "Juan Pérez",
  "courseName": "Salsa Principiantes",
  "pricingType": "PACKAGE_4",
  "studentCategory": "REGULAR",
  "quantityClasses": 4,
  "personCount": 1,
  "originalPrice": 60000,
  "discountAmount": 6000,
  "finalPrice": 54000,
  "pricePerClass": 13500,
  "paymentMethod": "CASH",
  "status": "COMPLETED",
  "transactionId": null,
  "paymentDate": "2026-03-23T20:00:00",
  "createdAt": "2026-03-23T20:00:00",
  "notes": "Pago en recepción"
}
```

---

#### `GET /api/payments/status/{paymentCode}` 🔒

Consulta un pago por su código único.

**Ejemplo**: `GET /api/payments/status/PAY-20260323-ABC123`

---

#### `GET /api/payments/my-payments` 🔒

Historial de pagos del usuario autenticado.

**Roles**: STUDENT, TEACHER, ADMIN

**Response 200** — lista de `PaymentResponseDTO`

---

### 6. Health check — `/api/test`

> Sin autenticación

| Endpoint          | Response      |
| ----------------- | ------------- |
| `GET /api/test/hello` | `"Hello from Academy App!"` |
| `GET /api/test/ping`  | `"pong"` |

---

### 7. Cursos — `/api/courses`

> Los endpoints de consulta (`GET`) son **públicos** (sin token).  
> Las operaciones de escritura requieren rol `ADMIN`.

| Método   | Endpoint                              | Roles            | Descripción |
| -------- | ------------------------------------- | ---------------- | ----------- |
| `GET`    | `/api/courses`                        | Público          | Cursos activos |
| `GET`    | `/api/courses/all`                    | ADMIN            | Todos los cursos (incluye inactivos) |
| `GET`    | `/api/courses/{id}`                   | Público          | Curso por ID |
| `GET`    | `/api/courses/search?q=...`           | Público          | Buscar por texto (título, código, descripción) |
| `GET`    | `/api/courses/dance-type/{danceType}` | Público          | Por tipo de baile |
| `GET`    | `/api/courses/level/{level}`          | Público          | Por nivel |
| `GET`    | `/api/courses/teacher/{teacherId}`    | ADMIN, TEACHER   | Cursos de un profesor |
| `GET`    | `/api/courses/{id}/capacity`          | Público          | Cupos disponibles |
| `POST`   | `/api/courses`                        | ADMIN            | Crear curso |
| `PUT`    | `/api/courses/{id}`                   | ADMIN            | Actualizar curso completo |
| `DELETE` | `/api/courses/{id}`                   | ADMIN            | Eliminar (lógico) |
| `PATCH`  | `/api/courses/{id}/toggle-status`     | ADMIN            | Activar / Desactivar |

**Objeto `CourseDTO`**
```json
{
  "id": 1,
  "title": "Salsa Básica para Principiantes",
  "code": "SAL-B-001",
  "description": "Aprende los fundamentos de la salsa desde cero",
  "danceType": "SALSA",
  "level": "BEGINNER",
  "pricePerHour": 15000.00,
  "durationHours": 1.5,
  "maxCapacity": 20,
  "teacherId": 2,
  "teacherName": "Pedro Gómez",
  "teacherEmail": "pedro@academia.com",
  "isActive": true,
  "imageUrl": null,
  "prerequisites": null,
  "objectives": "Aprender pasos básicos y postura",
  "activeEnrollments": 8,
  "availableSlots": 12,
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-03-23T20:00:00"
}
```

**Response `GET /api/courses/{id}/capacity`**
```json
{
  "courseId": 1,
  "courseTitle": "Salsa Básica para Principiantes",
  "maxCapacity": 20,
  "activeEnrollments": 8,
  "availableSlots": 12
}
```

> Para crear/actualizar: enviar `teacherId`, `danceType` y `level` con los valores exactos de las enumeraciones.

---

### 8. Estudiantes — `/api/students`

> Requiere `Authorization: Bearer <token>`

| Método  | Endpoint                           | Roles                     | Descripción |
| ------- | ---------------------------------- | ------------------------- | ----------- |
| `GET`   | `/api/students`                    | ADMIN, TEACHER            | Todos los estudiantes |
| `GET`   | `/api/students/{id}`               | ADMIN, TEACHER            | Estudiante por ID |
| `GET`   | `/api/students/me`                 | Cualquier autenticado     | Perfil propio |
| `GET`   | `/api/students/search?q=...`       | ADMIN, TEACHER            | Buscar por nombre, email o teléfono |
| `GET`   | `/api/students/status/{status}`    | ADMIN, TEACHER            | Por estado |
| `GET`   | `/api/students/category/{category}`| ADMIN, TEACHER            | Por categoría |
| `POST`  | `/api/students`                    | ADMIN                     | Crear estudiante |
| `PUT`   | `/api/students/{id}`               | ADMIN                     | Actualizar datos completos |
| `PATCH` | `/api/students/{id}/category`      | ADMIN                     | Cambiar categoría |

**Objeto `StudentDTO`**
```json
{
  "id": 1,
  "firstName": "Ana",
  "lastName": "García",
  "email": "ana.garcia@email.com",
  "phone": "+56912345678",
  "emergencyContact": "Luis García",
  "emergencyPhone": "+56987654321",
  "dateOfBirth": "2000-05-15",
  "address": "Calle Falsa 123",
  "category": "UNIVERSITY",
  "status": "ACTIVE",
  "universityName": "Universidad de Chile",
  "studentId": "20231234",
  "career": "Ingeniería",
  "semester": 4,
  "medicalConditions": null,
  "allergies": null,
  "medications": null,
  "danceExperience": "Principiante en salsa",
  "fitnessLevel": "Intermedio",
  "physicalLimitations": null,
  "preferredContactMethod": "EMAIL",
  "newsletterSubscription": true,
  "promotionalEmails": true,
  "notes": null,
  "userId": 5,
  "userEmail": "ana.garcia@email.com",
  "fullName": "Ana García",
  "createdAt": "2026-01-15T10:00:00",
  "updatedAt": "2026-03-23T20:00:00"
}
```

**Request `PATCH /api/students/{id}/category`**
```json
{ "category": "UNIVERSITY" }
```

---

### 9. Inscripciones — `/api/enrollments`

> Requiere `Authorization: Bearer <token>`  
> Los nuevos pagos procesados por `/api/payments/process` crean inscripciones automáticamente.

| Método  | Endpoint                           | Roles          | Descripción |
| ------- | ---------------------------------- | -------------- | ----------- |
| `GET`   | `/api/enrollments/my`              | Todos autent.  | Todas mis inscripciones |
| `GET`   | `/api/enrollments/my/active`       | Todos autent.  | Solo mis inscripciones activas |
| `GET`   | `/api/enrollments/my/summary`      | Todos autent.  | Resumen de horas compradas/usadas |
| `GET`   | `/api/enrollments/{id}`            | ADMIN, TEACHER | Inscripción por ID |
| `GET`   | `/api/enrollments/course/{courseId}` | ADMIN, TEACHER | Inscripciones de un curso |
| `PATCH` | `/api/enrollments/{id}/cancel`     | ADMIN          | Cancelar inscripción |
| `PATCH` | `/api/enrollments/{id}/suspend`    | ADMIN          | Suspender inscripción |
| `PATCH` | `/api/enrollments/{id}/reactivate` | ADMIN          | Reactivar inscripción suspendida |

**Objeto `EnrollmentDTO`**
```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "Ana García",
  "studentEmail": "ana.garcia@email.com",
  "courseId": 1,
  "courseName": "Salsa Básica para Principiantes",
  "courseCode": "SAL-B-001",
  "paymentId": 10,
  "paymentCode": "PAY-20260323-ABC123",
  "status": "ACTIVE",
  "enrollmentDate": "2026-03-23T20:00:00",
  "startDate": null,
  "endDate": null,
  "purchasedHours": 6.0,
  "usedHours": 1.5,
  "remainingHours": 4.5,
  "totalPaid": 54000.00,
  "paidAmount": 54000.00,
  "discountPercentage": 10.0,
  "finalPrice": 54000.00,
  "notes": null,
  "createdAt": "2026-03-23T20:00:00",
  "updatedAt": "2026-03-23T20:00:00"
}
```

**Response `GET /api/enrollments/my/summary`**
```json
{
  "totalEnrollments": 3,
  "activeEnrollments": 2,
  "totalHoursPurchased": 18.0,
  "totalHoursUsed": 4.5,
  "totalHoursRemaining": 13.5,
  "totalAmountPaid": 120000.00
}
```

**Request `PATCH /api/enrollments/{id}/cancel`** (body opcional)
```json
{ "reason": "El estudiante no puede continuar por motivos personales" }
```

---

### 10. Sesiones de Clase — `/api/class-sessions`

> Los endpoints de consulta (`GET`) son accesibles a **cualquier usuario autenticado**.  
> Las operaciones de escritura y cambio de estado requieren rol `ADMIN` o `TEACHER`.

| Método    | Endpoint                                   | Roles               | Descripción |
| --------- | ------------------------------------------ | ------------------- | ----------- |
| `GET`     | `/api/class-sessions/course/{courseId}`    | Autenticado         | Todas las sesiones de un curso |
| `GET`     | `/api/class-sessions/{id}`                 | Autenticado         | Sesión por ID |
| `GET`     | `/api/class-sessions/upcoming/{courseId}`  | Autenticado         | Próximas sesiones programadas de un curso |
| `GET`     | `/api/class-sessions/calendar?from=&to=`   | Autenticado         | Sesiones en rango de fechas (calendario) |
| `GET`     | `/api/class-sessions/teacher/{teacherId}`  | ADMIN, TEACHER      | Sesiones de un profesor |
| `GET`     | `/api/class-sessions`                      | ADMIN, TEACHER      | Todas las sesiones |
| `POST`    | `/api/class-sessions`                      | ADMIN, TEACHER      | Crear sesión |
| `PUT`     | `/api/class-sessions/{id}`                 | ADMIN, TEACHER      | Actualizar sesión |
| `PATCH`   | `/api/class-sessions/{id}/cancel`          | ADMIN, TEACHER      | Cancelar sesión |
| `PATCH`   | `/api/class-sessions/{id}/start`           | ADMIN, TEACHER      | Marcar sesión como iniciada |
| `PATCH`   | `/api/class-sessions/{id}/complete`        | ADMIN, TEACHER      | Marcar sesión como completada |

**Objeto `ClassSessionDTO`**
```json
{
  "id": 1,
  "courseId": 1,
  "courseName": "Salsa Básica para Principiantes",
  "courseCode": "SAL-B-001",
  "teacherId": 2,
  "teacherName": "Pedro Gómez",
  "teacherEmail": "pedro@academia.com",
  "sessionName": "Clase 1 — Pasos Básicos",
  "description": "Introducción a los pasos fundamentales de la salsa",
  "scheduledDate": "2026-04-01T18:00:00",
  "actualStartTime": null,
  "actualEndTime": null,
  "plannedDuration": 1.5,
  "actualDuration": null,
  "status": "SCHEDULED",
  "maxCapacity": 20,
  "location": "Sala A",
  "topic": "Pasos básicos y postura",
  "requiredMaterials": "Zapatos de baile",
  "teacherNotes": null,
  "virtualMeetingUrl": null,
  "isVirtual": false,
  "isRecurring": false,
  "parentClassId": null,
  "difficultyLevel": null,
  "specialRequirements": null,
  "cancellationReason": null,
  "createdAt": "2026-03-23T20:00:00",
  "updatedAt": "2026-03-23T20:00:00",
  "attendanceCount": 15,
  "availableSpots": 5
}
```

> `attendanceCount` y `availableSpots` son campos **calculados** (sólo lectura).

**Request `POST /api/class-sessions`** (campos mínimos requeridos)
```json
{
  "courseId": 1,
  "teacherId": 2,
  "sessionName": "Clase 1 — Pasos Básicos",
  "scheduledDate": "2026-04-01T18:00:00",
  "plannedDuration": 1.5,
  "maxCapacity": 20,
  "location": "Sala A",
  "isVirtual": false
}
```

**Request `PATCH /api/class-sessions/{id}/cancel`** (body opcional)
```json
{ "reason": "El profesor no puede asistir" }
```

> `PATCH /start` pone la sesión en `IN_PROGRESS` y registra `actualStartTime = ahora`.  
> `PATCH /complete` pone la sesión en `COMPLETED`, registra `actualEndTime` y calcula `actualDuration` automáticamente.

**Parámetros para el calendario**
```
GET /api/class-sessions/calendar?from=2026-04-01T00:00:00&to=2026-04-30T23:59:59
```
> `from` y `to` deben tener formato ISO 8601 (`yyyy-MM-ddTHH:mm:ss`).

---

### 11. Asistencias — `/api/attendances`

> Todos los endpoints requieren `Authorization: Bearer <token>` con rol `ADMIN` o `TEACHER`,  
> excepto las consultas de tasa de asistencia propia en contextos autorizados.

| Método  | Endpoint                                          | Roles          | Descripción |
| ------- | ------------------------------------------------- | -------------- | ----------- |
| `POST`  | `/api/attendances`                                | ADMIN, TEACHER | Registrar asistencia |
| `PUT`   | `/api/attendances/{id}`                           | ADMIN, TEACHER | Actualizar estado de asistencia |
| `GET`   | `/api/attendances/session/{sessionId}`            | ADMIN, TEACHER | Asistencias de una sesión |
| `GET`   | `/api/attendances/student/{studentId}`            | ADMIN, TEACHER | Historial de asistencias de un estudiante |
| `GET`   | `/api/attendances/student/{studentId}/rate`       | ADMIN, TEACHER | Porcentaje de asistencia de un estudiante |
| `GET`   | `/api/attendances/student/{studentId}/check/{classSessionId}` | ADMIN, TEACHER | Verificar si el estudiante tiene registro en una sesión |
| `GET`   | `/api/attendances/report?from=&to=`               | ADMIN          | Reporte completo de asistencias |
| `GET`   | `/api/attendances/low-attendance?minPercentage=`  | ADMIN, TEACHER | Estudiantes con baja asistencia |

**Request `POST /api/attendances`**
```json
{
  "studentId": 1,
  "classSessionId": 1,
  "attended": true,
  "notes": "Llegó con 5 minutos de retraso"
}
```

> Si ya existe un registro para esa combinación `studentId + classSessionId`, se **actualiza** en lugar de crear uno nuevo (idempotente).

**Request `PUT /api/attendances/{id}`**
```json
{
  "attended": false,
  "notes": "Falta justificada por enfermedad"
}
```

**Objeto `AttendanceDTO`**
```json
{
  "id": 1,
  "studentId": 1,
  "studentName": "Ana García",
  "studentEmail": "ana.garcia@email.com",
  "classSessionId": 1,
  "sessionName": "Clase 1 — Pasos Básicos",
  "scheduledDate": "2026-04-01T18:00:00",
  "courseId": 1,
  "courseName": "Salsa Básica para Principiantes",
  "attended": true,
  "isLate": false,
  "isExcused": false,
  "attendanceDate": "2026-04-01T18:03:00",
  "arrivalTime": null,
  "departureTime": null,
  "notes": null,
  "recordedBy": "pedro@academia.com",
  "createdAt": "2026-04-01T18:03:00",
  "updatedAt": "2026-04-01T18:03:00"
}
```

**Response `GET /api/attendances/student/{studentId}/rate`**
```json
{
  "studentId": 1,
  "attendanceRate": 85.7
}
```

**Response `GET /api/attendances/student/{studentId}/check/{classSessionId}`**
```json
{ "hasRecord": true }
```

**Response `GET /api/attendances/report?from=&to=`**
```json
{
  "reportDate": "2026-04-30T23:59:59",
  "periodStart": "2026-04-01T00:00:00",
  "periodEnd": "2026-04-30T23:59:59",
  "totalClasses": 12,
  "totalAttendances": 180,
  "totalAbsences": 20,
  "overallAttendanceRate": 90.0,
  "courseStats": [
    {
      "courseId": 1,
      "courseName": "Salsa Básica para Principiantes",
      "totalSessions": 8,
      "totalAttendances": 120,
      "totalAbsences": 10,
      "attendanceRate": 92.3
    }
  ],
  "studentStats": [
    {
      "studentId": 1,
      "firstName": "Ana",
      "lastName": "García",
      "email": "ana.garcia@email.com",
      "totalClasses": 8,
      "classesAttended": 7,
      "classesMissed": 1,
      "attendanceRate": 87.5,
      "riskLevel": "LOW"
    }
  ],
  "lowAttendanceStudents": [],
  "topAttendanceStudents": []
}
```

> `riskLevel` puede ser `LOW` (≥ 80%), `MEDIUM` (≥ 60%) o `HIGH` (< 60%).

**Response `GET /api/attendances/low-attendance?minPercentage=75`**

Devuelve la lista de estudiantes cuya tasa de asistencia (en los últimos 3 meses) está por debajo de `minPercentage`. Default: `75.0`.

```json
[
  {
    "studentId": 5,
    "firstName": "Carlos",
    "lastName": "López",
    "email": "carlos@email.com",
    "attendanceRate": 62.5
  }
]
```

---

## 📦 Enumeraciones (valores aceptados por la API)

### `StudentCategory`
| Valor       | Etiqueta       |
|-------------|----------------|
| `REGULAR`   | Regular        |
| `UNIVERSITY`| Universitario  |
| `COUPLE`    | Pareja         |
| `SENIOR`    | Adulto Mayor   |
| `CHILD`     | Niño           |

### `PricingType`
| Valor               | Etiqueta                |
|---------------------|-------------------------|
| `SINGLE_CLASS`      | Clase Individual        |
| `PACKAGE_4`         | Paquete 4 Clases        |
| `PACKAGE_8`         | Paquete 8 Clases        |
| `PACKAGE_12`        | Paquete 12 Clases       |
| `COUPLE_PACKAGE_8`  | Paquete Pareja 8 Clases |
| `UNLIMITED_MONTHLY` | Mensualidad Ilimitada   |

### `PaymentMethod`
| Valor             | Etiqueta              |
|-------------------|-----------------------|
| `CASH`            | Efectivo              |
| `CREDIT_CARD`     | Tarjeta de Crédito    |
| `DEBIT_CARD`      | Tarjeta de Débito     |
| `BANK_TRANSFER`   | Transferencia Bancaria|
| `MOBILE_PAYMENT`  | Pago Móvil            |

### `PaymentStatus`
| Valor       | Descripción          |
|-------------|----------------------|
| `PENDING`   | Pendiente            |
| `COMPLETED` | Completado           |
| `FAILED`    | Fallido              |
| `REFUNDED`  | Reembolsado          |
| `CANCELLED` | Cancelado            |

### `DanceType`
`SALSA` | `BACHATA` | `MERENGUE` | `REGGAETON` | `CUMBIA` | `TANGO` | `KIZOMBA` | `ZOUK` | `MAMBO` | `CHA_CHA_CHA`

### `DanceLevel`
`BEGINNER` | `INTERMEDIATE` | `ADVANCED` | `MASTER` | `OPEN`

### `EnrollmentStatus`
| Valor             | Descripción |
|-------------------|-------------|
| `PENDING`         | Pendiente de confirmación |
| `ACTIVE`          | Inscripción activa |
| `COMPLETED`       | Completada |
| `CANCELLED`       | Cancelada |
| `SUSPENDED`       | Suspendida temporalmente |
| `TRANSFERRED`     | Transferida a otro curso |
| `HOURS_EXHAUSTED` | Se agotaron las horas compradas |

### `StudentStatus`
| Valor         | Descripción |
|---------------|-------------|
| `ACTIVE`      | Activo |
| `INACTIVE`    | Inactivo |
| `SUSPENDED`   | Suspendido |
| `GRADUATED`   | Graduado |
| `DROPPED_OUT` | Abandonó |
| `ON_HOLD`     | En espera |

### `ClassStatus`
| Valor          | Descripción |
|----------------|-------------|
| `SCHEDULED`    | Programada — aún no iniciada |
| `IN_PROGRESS`  | En curso — iniciada con `PATCH /start` |
| `COMPLETED`    | Completada — finalizada con `PATCH /complete` |
| `CANCELLED`    | Cancelada — finalizada con `PATCH /cancel` |
| `POSTPONED`    | Pospuesta |
| `NO_SHOW`      | Sin asistencia del profesor |

---

## ⚠️ Manejo de errores

El backend responde con estos códigos HTTP estándar:

| Código | Situación |
|--------|-----------|
| `200`  | OK — solicitud exitosa |
| `201`  | Created — recurso creado |
| `204`  | No Content — eliminación exitosa |
| `400`  | Bad Request — datos inválidos o faltantes |
| `401`  | Unauthorized — token ausente, inválido o expirado |
| `403`  | Forbidden — rol insuficiente para el recurso |
| `404`  | Not Found — recurso no encontrado |
| `500`  | Internal Server Error |

### Estructura de error típica (validación)
```json
{
  "timestamp": "2026-03-23T20:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "El email debe tener un formato válido",
  "path": "/api/auth/login"
}
```

---

## 🚀 Flujos de uso recomendados

### Flujo 1 — Explorar cursos (sin login)

```
1. GET /api/courses                              → catálogo de cursos activos
2. GET /api/courses/dance-type/SALSA             → filtrar por tipo de baile
3. GET /api/courses/{id}                         → detalle de un curso
4. GET /api/courses/{id}/capacity                → verificar cupos disponibles
5. GET /api/pricing/calculate?courseId=X         → ver precios disponibles
           &studentCategory=REGULAR
```

### Flujo 2 — Inscripción de un estudiante

```
1. POST /api/auth/login                          → obtener token
2. GET  /api/courses                             → elegir curso
3. GET  /api/pricing/calculate                   → mostrar opciones de precio
           ?courseId=X&studentCategory=REGULAR
4. POST /api/payments/validate          🔒       → confirmar antes de cobrar
5. POST /api/payments/process           🔒       → procesar pago → crea inscripción automáticamente
6. GET  /api/payments/status/{code}     🔒       → mostrar comprobante
7. GET  /api/enrollments/my             🔒       → ver inscripción generada
```

### Flujo 3 — Perfil del estudiante

```
1. POST /api/auth/login                          → obtener token
2. GET  /api/students/me                🔒       → ver perfil propio
3. GET  /api/enrollments/my             🔒       → ver inscripciones activas
4. GET  /api/enrollments/my/summary     🔒       → ver resumen de horas
```

### Flujo 4 — Panel de administración

```
1. POST /api/auth/login (ADMIN)                  → obtener token
2. GET  /api/students                   🔒       → lista de todos los estudiantes
3. GET  /api/students/search?q=...      🔒       → buscar estudiante
4. GET  /api/enrollments/course/{id}    🔒       → ver inscritos de un curso
5. GET  /api/courses/all                🔒       → ver todos los cursos (activos + inactivos)
6. PATCH /api/courses/{id}/toggle-status 🔒      → activar/desactivar curso
```

### Flujo 5 — Gestión de sesiones de clase (TEACHER / ADMIN)

```
1. POST /api/auth/login (TEACHER)                        → obtener token
2. GET  /api/class-sessions/course/{courseId}   🔒       → ver todas las sesiones del curso
3. POST /api/class-sessions                     🔒       → crear nueva sesión
4. GET  /api/class-sessions/upcoming/{courseId} 🔒       → ver próximas sesiones
5. PATCH /api/class-sessions/{id}/start         🔒       → marcar sesión iniciada (en vivo)
6. POST /api/attendances                        🔒       → registrar asistencia por estudiante
7. PATCH /api/class-sessions/{id}/complete      🔒       → cerrar sesión (calcula duración)
```

### Flujo 6 — Seguimiento de asistencias (ADMIN)

```
1. POST /api/auth/login (ADMIN)                            → obtener token
2. GET  /api/attendances/session/{sessionId}      🔒       → asistentes de una sesión
3. PUT  /api/attendances/{id}                     🔒       → corregir registro de asistencia
4. GET  /api/attendances/student/{studentId}/rate 🔒       → ver % de asistencia del estudiante
5. GET  /api/attendances/low-attendance?minPercentage=75 🔒 → detectar estudiantes en riesgo
6. GET  /api/attendances/report?from=&to=         🔒       → reporte completo del período
```

### Flujo 7 — Calendario de clases (vista pública autenticada)

```
1. POST /api/auth/login                                     → obtener token
2. GET  /api/class-sessions/calendar               🔒
           ?from=2026-04-01T00:00:00
           &to=2026-04-30T23:59:59                          → sesiones del mes
3. GET  /api/class-sessions/{id}                   🔒       → detalle de una sesión
4. GET  /api/class-sessions/upcoming/{courseId}    🔒       → próximas clases de mi curso
```

---

## 🔧 Configuración CORS

El backend acepta peticiones de **cualquier origen** con los métodos `GET`, `POST`, `PUT`, `DELETE`, `OPTIONS` y todos los headers. No se requiere configuración adicional en el frontend durante desarrollo.

```javascript
// Ejemplo con fetch
const response = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email: 'user@mail.com', password: '123456' })
});
const data = await response.json();
const token = data.token;

// Petición autenticada
const users = await fetch('http://localhost:8080/api/users', {
  headers: { 'Authorization': `Bearer ${token}` }
});
```

---

## 📚 Recursos adicionales

- **Swagger UI interactivo**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) — permite probar todos los endpoints directamente desde el navegador.
- **OpenAPI spec**: [http://localhost:8080/api-docs](http://localhost:8080/api-docs) — JSON importable en Postman o Insomnia.

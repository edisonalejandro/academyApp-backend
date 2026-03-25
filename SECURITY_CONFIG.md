# 🔒 Guía de Configuración de Seguridad - AcademyApp

## Variables de Entorno Requeridas

### Desarrollo Local
Crear archivo `src/main/resources/application-local.properties` con:

```properties
# Base de datos local
DB_USERNAME=tu_usuario_postgres
DB_PASSWORD=tu_contraseña_postgres

# JWT Secret (generar uno seguro)
JWT_SECRET=tu-secreto-jwt-de-al-menos-256-bits-muy-seguro-y-aleatorio
```

### Producción
Configurar las siguientes variables de entorno:

```bash
# Base de datos
export DB_URL=jdbc:postgresql://tu-servidor:5432/academia_baile
export DB_USERNAME=usuario_produccion
export DB_PASSWORD=contraseña_segura_produccion

# JWT
export JWT_SECRET=secreto-jwt-produccion-256-bits-minimo
export JWT_EXPIRATION_MS=86400000  # 24 horas (opcional, default: 24h)

# CORS - Dominios permitidos separados por coma
export CORS_ALLOWED_ORIGINS=https://tudominio.com,https://www.tudominio.com

# Puerto (opcional)
export PORT=8080

# Perfil de Spring Boot
export SPRING_PROFILES_ACTIVE=prod
```

## 🔑 Generación de JWT Secret Seguro

### Opción 1: OpenSSL
```bash
openssl rand -base64 64
```

### Opción 2: Node.js
```bash
node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"
```

### Opción 3: Python
```bash
python -c "import secrets; print(secrets.token_urlsafe(64))"
```

## 🚀 Ejecución según Entorno

### Desarrollo Local
```bash
# Usar perfil local (lee application-local.properties)
mvn spring-boot:run -Dspring-boot.run.profiles=local

# O configurar en IDE:
# VM Options: -Dspring.profiles.active=local
```

### Producción
```bash
# Asegurarse de tener las variables de entorno configuradas
java -jar academyApp-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

# O con Maven:
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

## 🔐 Configuración de CORS

### Desarrollo
Por defecto permite: `http://localhost:3000` y `http://localhost:4200`

### Producción
Especificar dominios exactos en variable de entorno:
```bash
export CORS_ALLOWED_ORIGINS=https://tuapp.com,https://www.tuapp.com,https://admin.tuapp.com
```

## 📋 Checklist de Seguridad para Producción

- [ ] JWT_SECRET configurado (mínimo 256 bits)
- [ ] DB_PASSWORD es una contraseña fuerte
- [ ] CORS_ALLOWED_ORIGINS tiene solo los dominios necesarios
- [ ] Perfil `prod` activado
- [ ] `spring.jpa.hibernate.ddl-auto=validate` (no `update`)
- [ ] `spring.jpa.show-sql=false`
- [ ] Logs configurados en nivel WARN/INFO
- [ ] Conexión a base de datos con SSL habilitado
- [ ] Certificado HTTPS configurado en el servidor
- [ ] Firewall configurado para permitir solo tráfico necesario

## 🔍 Verificación de Configuración

### Verificar que las variables están cargadas:
```bash
# En Linux/Mac
echo $JWT_SECRET
echo $DB_USERNAME

# En Windows PowerShell
$env:JWT_SECRET
$env:DB_USERNAME
```

### Verificar el perfil activo al iniciar:
Al arrancar la aplicación, verificar en los logs:
```
The following profiles are active: prod
```

## ⚠️ Advertencias Importantes

1. **NUNCA** commitear credenciales en el repositorio
2. **SIEMPRE** usar diferentes secretos para desarrollo y producción
3. **ROTAR** el JWT_SECRET periódicamente en producción
4. **MONITOREAR** intentos de acceso no autorizados
5. **MANTENER** Spring Boot actualizado para parches de seguridad

## 📊 Endpoints de Admin Dashboard

El nuevo endpoint de dashboard está protegido:

### `GET /api/admin/dashboard`
- **Requiere**: Rol `ADMIN`
- **JWT**: Obligatorio en header `Authorization: Bearer {token}`
- **Respuesta**: Resumen completo del sistema

### `GET /api/admin/dashboard/quick-stats`
- **Requiere**: Rol `ADMIN` o `TEACHER`
- **JWT**: Obligatorio en header `Authorization: Bearer {token}`
- **Respuesta**: Estadísticas rápidas

## 🐳 Docker (Opcional)

Variables de entorno en `docker-compose.yml`:
```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
  - DB_URL=jdbc:postgresql://db:5432/academia_baile
  - DB_USERNAME=postgres
  - DB_PASSWORD=${DB_PASSWORD}
  - JWT_SECRET=${JWT_SECRET}
  - CORS_ALLOWED_ORIGINS=https://tudominio.com
```

## 📞 Soporte

Para dudas sobre configuración de seguridad, consultar al equipo de DevOps.

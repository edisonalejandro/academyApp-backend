#!/bin/bash
# =================================
# run-dev.sh — Inicia el backend en modo desarrollo
# =================================
# Usa el perfil 'dev' que lee application-dev.properties:
#   - DB local en localhost:5432
#   - show-sql=true
#   - logging DEBUG
#
# Requisito: PostgreSQL corriendo localmente.
# O bien, levantar solo la DB con Docker:
#   cd ../infra && docker-compose up -d postgres
# =================================

./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

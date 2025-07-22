# 📦 josue_tipan_shipflow - Sistema de Gestión de Envíos

Sistema REST para registrar, consultar y gestionar el estado de envíos de paquetes.

---

## 🚀 Descripción General

Permite:
- Registrar paquetes con tipo, peso, descripción, ciudad origen/destino
- Generar tracking ID único
- Consultar envíos y su historial
- Actualizar estado del envío con registro histórico

---

## 🧩 Funcionalidades Principales:

**Registro de envíos:**
- Permite registrar paquetes de tipo DOCUMENT, SMALL_BOX o FRAGILE.
- Valida el peso y la longitud de la descripción.
- Solicita ciudad de origen y destino (no pueden ser iguales).
- Asigna automáticamente un identificador de seguimiento único.
- El estado inicial del envío es PENDING y se calcula una fecha estimada de entrega.

**Consulta de envíos:**
- Puedes ver todos los envíos registrados en el sistema.
- Es posible buscar un envío específico usando su tracking ID.
- Se puede consultar el historial de cambios de estado de cada envío.

**Gestión y control de estados:**
- Los envíos pueden cambiar entre los estados: PENDING, IN_TRANSIT, DELIVERED, ON_HOLD y CANCELLED.
- El sistema valida que las transiciones de estado sean correctas según las reglas definidas.
- Cada cambio de estado queda registrado con fecha, estado y comentario opcional para trazabilidad.

---

## ⚙️ Requisitos Previos
- Java 21
- Docker y Docker Compose
- Git

---

## 🛠️ Instalación y Ejecución

1. **Clona el repositorio**
   ```bash
   git clone https://github.com/josuetb/josue_tipan_shipflow.git
   cd josue_tipan_shipflow
   ```

2. **Levanta la base de datos**
   ```bash
   docker-compose up -d
   ```
   Esto crea un contenedor Postgres y PgAdmin listos para usar.

3. **Configura la conexión en `src/main/resources/application.properties` si es necesario:**
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/josue_db
   spring.datasource.username=admin
   spring.datasource.password=admin
   ```

4. **Ejecuta la aplicación**
   ```bash
   ./gradlew bootRun
   ```

---

## 🌐 Endpoints Principales

**Base URL:** `http://localhost:8080/shipflow/api`

### 1. Registrar un envío
```http
POST /shipflow/api/packages
Content-Type: application/json

{
  "type": "DOCUMENT",
  "weight": 1.2,
  "description": "Papeles importantes",
  "cityFrom": "Quito",
  "cityTo": "Guayaquil"
}
```

### 2. Listar todos los envíos
```http
GET /shipflow/api/packages
```

### 3. Consultar envío por tracking ID
```http
GET /shipflow/api/packages/{trackingId}
```

### 4. Consultar historial de un envío
```http
GET /shipflow/api/packages/{trackingId}/history
```

### 5. Actualizar estado de un envío
```http
PUT /shipflow/api/packages/{trackingId}/status
Content-Type: application/json

{
  "status": "IN_TRANSIT",
  "comment": "Paquete recogido en sucursal"
}
```

---

## 📝 Notas
- Usa PgAdmin en `http://localhost:8081` (usuario: admin@shipflow.com, clave: admin) para ver la base de datos.
- Los estados válidos son: PENDING, IN_TRANSIT, DELIVERED, ON_HOLD, CANCELLED.
- La descripción no debe superar 50 caracteres.
- Las ciudades de origen y destino no pueden ser iguales.

---

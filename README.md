# Usuarios Service - Pedidos360

Microservicio de usuarios para la plataforma Pedidos360. Está construido con Spring Boot y expone una API REST protegida mediante tokens JWT emitidos por Microsoft Entra ID.

## Arquitectura

Pedidos360 está compuesto por un frontend Angular y dos microservicios independientes de Spring Boot. En el entorno local, el frontend consume las APIs protegidas mediante tokens Bearer sobre HTTP, con el frontend y ambos servicios ejecutándose en puertos separados.


### Flujo de una petición protegida

1. El usuario inicia sesión en Microsoft Entra ID desde el frontend Angular mediante MSAL.
2. MSAL obtiene un access token para el scope `access_as_user`.
3. `MsalInterceptor` agrega el token como `Authorization: Bearer <token>` en las llamadas protegidas.
4. Usuarios Service valida el JWT antes de permitir el acceso a `/api/**`.
5. El controlador delega la operación a `UsuarioService` y devuelve la respuesta REST.


## Tecnologías

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Security
- OAuth2 Resource Server con JWT
- Spring Boot Actuator
- Maven Wrapper

## Responsabilidad

El servicio administra usuarios y entrega la información del usuario autenticado a partir de los claims incluidos en su token JWT.


## Requisitos

- JDK 21
- Windows: `mvnw.cmd` incluido en el repositorio
- Linux o macOS: `./mvnw`
- Acceso de red al issuer de Microsoft Entra ID para validar los tokens


## Ejecución local

Desde la carpeta `Usuarios-Service-Pedidos360`:

### Windows PowerShell

```powershell
.\\mvnw.cmd spring-boot:run
```

### Linux o macOS

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en:

```text
http://localhost:8081
```

## Compilación y pruebas

Compilar el proyecto:

```powershell
.\\mvnw.cmd clean package
```

Ejecutar las pruebas:

```powershell
.\\mvnw.cmd test
```

## API REST

Base URL:

```text
http://localhost:8081/api/usuarios
```

Todas las rutas de esta sección requieren un token Bearer válido con el scope `access_as_user`.

### Listar usuarios

```http
GET /api/usuarios
Authorization: Bearer <access-token>
```

Respuesta `200 OK`:

```json
[
  {
    "id": 1,
    "entraObjectId": "11111111-1111-1111-1111-111111111111",
    "nombre": "Laura Méndez",
    "email": "laura.mendez@example.com",
    "telefono": "+56 9 1111 1111",
    "direccion": "Av. Providencia 100",
    "activo": true
  }
]
```

### Obtener el perfil autenticado

```http
GET /api/usuarios/me
Authorization: Bearer <access-token>
```

La respuesta se construye a partir de los claims del JWT:

```json
{
  "objectId": "<oid>",
  "nombre": "Nombre del usuario",
  "username": "usuario@example.com",
  "email": "usuario@example.com",
  "tenantId": "<tid>",
  "scope": "access_as_user"
}
```

### Obtener un usuario

```http
GET /api/usuarios/{id}
Authorization: Bearer <access-token>
```

Respuestas principales:

- `200 OK`: usuario encontrado.
- `404 Not Found`: el ID no existe.

### Crear un usuario

```http
POST /api/usuarios
Authorization: Bearer <access-token>
Content-Type: application/json
```

Ejemplo de cuerpo:

```json
{
  "entraObjectId": "44444444-4444-4444-4444-444444444444",
  "nombre": "Nuevo Usuario",
  "email": "nuevo.usuario@example.com",
  "telefono": "+56 9 4444 4444",
  "direccion": "Av. Central 400",
  "activo": true
}
```

Respuesta: `201 Created`.

### Actualizar un usuario

```http
PUT /api/usuarios/{id}
Authorization: Bearer <access-token>
Content-Type: application/json
```

Respuesta: `200 OK` si existe o `404 Not Found` si no existe.

### Eliminar un usuario

```http
DELETE /api/usuarios/{id}
Authorization: Bearer <access-token>
```

Respuestas principales:

- `204 No Content`: usuario eliminado.
- `404 Not Found`: el ID no existe.

### Health check

```http
GET /actuator/health
```

Ejemplo de respuesta:

```json
{
  "groups": ["liveness", "readiness"],
  "status": "UP"
}
```

## Seguridad

La configuración de seguridad se encuentra en `src/main/java/cl/duoc/pedidos360/usuarios/config/SecurityConfig.java`.

No se implementan roles diferenciados actualmente: todas las operaciones `/api/**` requieren el mismo scope `access_as_user`.

## Estructura principal

```text
src/
├── main/
│   ├── java/cl/duoc/pedidos360/usuarios/
│   │   ├── UsuariosServicePedidos360Application.java
│   │   ├── config/SecurityConfig.java
│   │   ├── controller/UsuarioController.java
│   │   ├── model/Usuario.java
│   │   ├── model/UsuarioAutenticadoResponse.java
│   │   └── service/UsuarioService.java
│   └── resources/application.properties
└── test/
```




# Usuarios Service - Pedidos360

Microservicio encargado de la gestión de usuarios de la plataforma Pedidos360. Está desarrollado con Spring Boot y expone una API REST protegida mediante tokens JWT emitidos por Microsoft Entra ID.

## Descripción general

Usuarios Service forma parte de la arquitectura de Pedidos360 junto con el frontend Angular y el microservicio de pedidos.

Sus principales responsabilidades son:

- obtener información del usuario autenticado a partir de los claims contenidos en su JWT;
- proteger los endpoints mediante Spring Security y OAuth2 Resource Server;
- validar el issuer y audience de los tokens emitidos por Microsoft Entra ID;
- requerir el scope `access_as_user` para acceder a las rutas protegidas;
- persistir la información mediante Spring Data JPA y MySQL;
- exponer un endpoint de health check mediante Spring Boot Actuator.

## Arquitectura

Pedidos360 está compuesto principalmente por:

- **Frontend Angular:** interfaz de usuario y autenticación mediante MSAL.
- **Pedidos Service:** gestión de pedidos.
- **Usuarios Service:** gestión de usuarios e información del usuario autenticado.
- **Microsoft Entra ID:** autenticación y emisión de tokens JWT.
- **AWS API Gateway:** punto de entrada utilizado por el frontend para acceder a los microservicios en el entorno desplegado.
- **AWS EC2:** entorno donde se ejecutan los microservicios según la arquitectura de despliegue del proyecto.

En desarrollo local, Usuarios Service se ejecuta en el puerto `8081`.

## Flujo de una petición protegida

1. El usuario inicia sesión desde el frontend Angular mediante Microsoft Entra ID y MSAL.
2. El frontend solicita un access token con el scope `access_as_user`.
3. `MsalInterceptor` incorpora el token en las solicitudes protegidas utilizando el encabezado:

```http
Authorization: Bearer <access-token>
```

4. La solicitud es enviada hacia la API correspondiente.
5. Usuarios Service recibe el JWT y Spring Security valida su autenticidad.
6. Se comprueba el issuer, audience y el scope requerido.
7. Si el token es válido, el controlador procesa la solicitud.
8. `UsuarioController` delega las operaciones de negocio en `UsuarioService`.
9. `UsuarioService` utiliza `UsuarioRepository` para acceder a la persistencia.
10. El resultado se devuelve como respuesta REST en formato JSON.

## Stack tecnológico

- Java 21
- Spring Boot 4.1.1
- Maven
- Maven Wrapper
- Spring Web MVC
- Spring Security
- OAuth2 Resource Server
- JWT
- Spring Data JPA
- MySQL
- Spring Boot Actuator

## Estructura del proyecto

```text
Usuarios-Service-Pedidos360/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── cl/
│   │   │       └── duoc/
│   │   │           └── pedidos360/
│   │   │               └── usuarios/
│   │   │                   ├── config/
│   │   │                   │   └── SecurityConfig.java
│   │   │                   ├── controller/
│   │   │                   │   └── UsuarioController.java
│   │   │                   ├── model/
│   │   │                   │   ├── Usuario.java
│   │   │                   │   └── UsuarioAutenticadoResponse.java
│   │   │                   ├── repository/
│   │   │                   │   └── UsuarioRepository.java
│   │   │                   ├── service/
│   │   │                   │   └── UsuarioService.java
│   │   │                   └── UsuariosServicePedidos360Application.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Modelo de usuario

La entidad `Usuario` representa a los usuarios almacenados por el servicio.

Sus principales atributos son:

```json
{
  "id": 1,
  "entraObjectId": "11111111-1111-1111-1111-111111111111",
  "nombre": "Laura Méndez",
  "email": "laura.mendez@example.com",
  "telefono": "+56 9 1111 1111",
  "direccion": "Av. Providencia 100",
  "activo": true
}
```

El campo `entraObjectId` permite almacenar el identificador asociado al usuario de Microsoft Entra ID.

## Persistencia

Usuarios Service utiliza Spring Data JPA y MySQL para la persistencia de usuarios.

La entidad `Usuario` está asociada a la tabla:

```text
usuarios
```

El acceso a los datos se realiza mediante `UsuarioRepository`.

La configuración de conexión se encuentra en:

```text
src/main/resources/application.properties
```

Las credenciales y parámetros de conexión se reciben mediante variables de entorno:

```properties
spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/${DB_NAME}?useSSL=true&serverTimezone=UTC
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

Las variables necesarias son:

- `DB_HOST`: host del servidor MySQL.
- `DB_PORT`: puerto de MySQL. 
- `DB_NAME`: nombre de la base de datos.
- `DB_USER`: usuario de conexión.
- `DB_PASSWORD`: contraseña de conexión.

Además, JPA está configurado mediante:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Funcionalidades principales

La clase `UsuarioService` implementa las operaciones principales sobre los usuarios:

- `listarTodos()`
- `buscarPorId(Long id)`
- `buscarPorEntraObjectId(String entraObjectId)`
- `crear(Usuario usuario)`
- `actualizar(Long id, Usuario usuario)`
- `eliminar(Long id)`

El servicio utiliza `UsuarioRepository` para almacenar y recuperar información desde la base de datos.

## API REST

La API está montada bajo el prefijo:

```text
/api/usuarios
```

Todas las rutas bajo `/api/**` requieren un JWT válido con el scope `access_as_user`.

### 1. Listar usuarios

```http
GET /api/usuarios
Authorization: Bearer <access-token>
```

Respuesta esperada:

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

### 2. Obtener perfil del usuario autenticado

```http
GET /api/usuarios/me
Authorization: Bearer <access-token>
```

Este endpoint obtiene la información directamente desde los claims contenidos en el JWT autenticado.

Ejemplo de respuesta:

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

### 3. Obtener usuario por ID

```http
GET /api/usuarios/{id}
Authorization: Bearer <access-token>
```

Respuestas principales:

- `200 OK`: usuario encontrado.
- `404 Not Found`: usuario no encontrado.

### 4. Crear usuario

```http
POST /api/usuarios
Authorization: Bearer <access-token>
Content-Type: application/json
```

Ejemplo de body:

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

Respuesta esperada:

```text
201 Created
```

> Este endpoint crea un usuario dentro de la base de datos de Pedidos360. No corresponde a la creación de una cuenta de autenticación en Microsoft Entra ID.

### 5. Actualizar usuario

```http
PUT /api/usuarios/{id}
Authorization: Bearer <access-token>
Content-Type: application/json
```

Respuestas principales:

- `200 OK`: usuario actualizado.
- `404 Not Found`: usuario no encontrado.

### 6. Eliminar usuario

```http
DELETE /api/usuarios/{id}
Authorization: Bearer <access-token>
```

Respuestas principales:

- `204 No Content`: usuario eliminado.
- `404 Not Found`: usuario no encontrado.

### 7. Health check

```http
GET /actuator/health
```

El endpoint de health check se encuentra habilitado mediante Spring Boot Actuator y no requiere autenticación.

Una respuesta normal cuando el servicio se encuentra disponible es:

```json
{
  "status": "UP"
}
```

## Configuración de seguridad

La configuración se encuentra en:

```text
src/main/java/cl/duoc/pedidos360/usuarios/config/SecurityConfig.java
```

Usuarios Service funciona como un OAuth2 Resource Server y utiliza JWT para proteger la API.

### Propiedades principales

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://login.microsoftonline.com/3441157d-ea5c-483f-a66d-e45c3ed7f9da/v2.0
pedidos360.security.jwt.audience=5582b6c4-7ecd-4bed-9337-ba3f1f8e58e5
```

### Validación JWT

El servicio valida:

- la firma y validez del JWT;
- el issuer del token;
- la vigencia del token;
- el audience esperado;
- el scope requerido para acceder a la API.

El audience esperado corresponde a la API registrada para Pedidos360 en Microsoft Entra ID.

### Reglas de acceso

La configuración de Spring Security establece las siguientes reglas:

- las solicitudes `OPTIONS` están permitidas;
- `/actuator/health` es público;
- las rutas bajo `/api/**` requieren `SCOPE_access_as_user`;
- el resto de rutas requiere autenticación.

Actualmente no existen roles diferenciados por operación. Todas las operaciones de la API utilizan el mismo scope `access_as_user`.

## CORS

Usuarios Service incorpora configuración CORS para permitir la comunicación desde los orígenes autorizados del frontend.

Los métodos HTTP permitidos incluyen:

```text
GET
POST
PUT
DELETE
OPTIONS
```

Los encabezados permitidos incluyen:

```text
Authorization
Content-Type
Accept
```

## Microsoft Entra ID

Usuarios Service no administra las credenciales de autenticación de los usuarios.

La autenticación se delega a Microsoft Entra ID.

El frontend Angular inicia sesión mediante MSAL y solicita el scope:

```text
api://5582b6c4-7ecd-4bed-9337-ba3f1f8e58e5/access_as_user
```

Microsoft Entra ID emite el access token y Usuarios Service valida posteriormente el JWT antes de permitir el acceso a los endpoints protegidos.

La creación de registros mediante:

```http
POST /api/usuarios
```

corresponde exclusivamente a usuarios almacenados en la base de datos de Pedidos360 y no crea nuevas cuentas dentro de Microsoft Entra ID.

## Requisitos previos

Antes de ejecutar el proyecto se necesita:

- JDK 21
- acceso a una instancia MySQL
- variables de entorno de conexión a la base de datos
- acceso de red a Microsoft Entra ID para la validación del JWT
- Maven o Maven Wrapper incluido en el proyecto

## Ejecución local

Desde la carpeta:

```text
Usuarios-Service-Pedidos360
```

### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux o macOS

```bash
./mvnw spring-boot:run
```

La aplicación queda disponible en:

```text
http://localhost:8081
```

## Compilación

### Windows

```powershell
.\mvnw.cmd clean package
```

### Linux o macOS

```bash
./mvnw clean package
```

## Integración con el frontend

En el entorno desplegado, el frontend de Pedidos360 consume Usuarios Service a través de AWS API Gateway.

La URL configurada actualmente en el frontend utiliza:

```text
/api/usuarios
```

También consume:

```text
/api/usuarios/me
```

para obtener la información del usuario autenticado.

MSAL incorpora automáticamente el access token a las solicitudes protegidas mediante `MsalInterceptor`.

## Resumen del flujo

El flujo principal de Usuarios Service es:

1. el usuario inicia sesión mediante Microsoft Entra ID;
2. el frontend obtiene un access token con el scope `access_as_user`;
3. el frontend envía el token como Bearer Token;
4. la solicitud llega a la API de Usuarios;
5. Spring Security valida JWT, issuer, audience y scope;
6. `UsuarioController` recibe la solicitud;
7. `UsuarioService` procesa la operación;
8. `UsuarioRepository` accede a MySQL cuando corresponde;
9. el servicio devuelve la respuesta REST en formato JSON.

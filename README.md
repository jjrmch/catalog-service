# Catalog Service

![CI](https://github.com/jjrmch/catalog-service/actions/workflows/ci.yml/badge.svg)

Microservicio de catálogo de la plataforma de gestión de biblioteca. Se encarga del CRUD de libros y del control de stock. Es una de las piezas de un ecosistema de microservicios con Spring Cloud: se registra en Eureka y lo consumen otros servicios (por ejemplo, transactions-service lo llama para descontar stock al registrar una venta o un alquiler).

Valida el JWT por su cuenta: la consulta del catálogo es pública, pero crear, editar o borrar libros exige un token con rol `ADMIN` o `BIBLIOTECARIO`.

## Qué hace

- CRUD de libros (título, autor, ISBN, precio, stock)
- Ajuste de stock: sumar o restar ejemplares con una sola petición
- Validación de stock: rechaza operaciones que dejen el stock en negativo (HTTP 409)
- Seguridad con JWT (HS256): lectura pública y escritura restringida por rol
- Swagger UI en `/swagger-ui.html` con botón Authorize (la doc de OpenAPI se expone también a través del gateway)

## Stack

- Java 17
- Spring Boot 4.1
- Spring Security (OAuth2 Resource Server) + Nimbus JWT
- Spring Cloud 2025.1.2 (Eureka client)
- Spring Data JPA
- PostgreSQL
- springdoc-openapi

## Cómo ejecutarlo

Necesitas PostgreSQL y el discovery-service (Eureka) levantados. Puedes levantar todo el stack con docker-compose desde `biblioteca-deploy`, o ejecutar este servicio solo:

```bash
./mvnw spring-boot:run
```

La configuración se hace por variables de entorno:

| Variable | Descripción |
|---|---|
| `DB_URL` | JDBC URL de PostgreSQL (default `jdbc:postgresql://localhost:5432/biblioteca`) |
| `DB_USER` | Usuario de PostgreSQL |
| `DB_PASSWORD` | Contraseña de PostgreSQL |
| `EUREKA_URL` | URL del servidor Eureka (default `http://localhost:8761/eureka/`) |
| `JWT_SECRET` | Secreto compartido para validar los JWT (mínimo 32 caracteres). **Debe ser el mismo que usa auth-service** |

## Endpoints

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| GET | `/libros` | Lista todos los libros | Público |
| GET | `/libros/buscar?q=` | Busca por título, autor o ISBN | Público |
| GET | `/libros/isbn/{isbn}` | Obtiene un libro por ISBN | Público |
| GET | `/libros/estadisticas` | Estadísticas del catálogo | Público |
| GET | `/libros/{id}` | Obtiene un libro por id | Público |
| POST | `/libros` | Crea un libro | ADMIN, BIBLIOTECARIO |
| PUT | `/libros/{id}` | Actualiza un libro | ADMIN, BIBLIOTECARIO |
| DELETE | `/libros/{id}` | Elimina un libro (404 si no existe) | ADMIN, BIBLIOTECARIO |
| PATCH | `/libros/{id}/stock` | Ajusta el stock (positivo suma, negativo resta) | ADMIN, BIBLIOTECARIO |

Las escrituras exigen `Authorization: Bearer <token>`; sin token responden `401` y con un rol insuficiente `403`.

## Parte de un sistema más grande

La plataforma completa se compone de:

- [discovery-service](https://github.com/jjrmch/discovery-service) — servidor Eureka
- [gateway-service](https://github.com/jjrmch/gateway-service) — API Gateway (punto de entrada, `localhost:8080`)
- [transactions-service](https://github.com/jjrmch/transactions-service) — ventas, alquileres, reservas y multas
- [customer-service](https://github.com/jjrmch/customer-service) — clientes
- [auth-service](https://github.com/jjrmch/auth-service) — registro, login y emisión de JWT
- [biblioteca-frontend](https://github.com/jjrmch/biblioteca-frontend) — panel web en React
- [biblioteca-deploy](https://github.com/jjrmch/biblioteca-deploy) — docker-compose con el stack completo

## Por mejorar

- No hay tests de negocio todavía, solo el test de contexto de Spring.
- El listado de libros no tiene paginación.
- Las llamadas entre microservicios (transactions-service → catalog-service) todavía no propagan el token; cuando transactions valide JWT habrá que añadir un interceptor de Feign.

## Licencia

MIT

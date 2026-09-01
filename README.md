# Catalog Service

Microservicio de catálogo de la plataforma de gestión de biblioteca. Se encarga del CRUD de libros y del control de stock. Es una de las piezas de un ecosistema de microservicios con Spring Cloud: se registra en Eureka y lo consumen otros servicios (por ejemplo, transactions-service lo llama para descontar stock al registrar una venta o un alquiler).

## Qué hace

- CRUD de libros (título, autor, ISBN, precio, stock)
- Ajuste de stock: sumar o restar ejemplares con una sola petición
- Validación de stock: rechaza operaciones que dejen el stock en negativo (HTTP 409)
- Swagger UI en `/swagger-ui.html` (la doc de OpenAPI se expone también a través del gateway)

## Stack

- Java 17
- Spring Boot 4.1
- Spring Cloud 2025.1.2 (Eureka client)
- Spring Data JPA
- PostgreSQL
- springdoc-openapi

## Cómo ejecutarlo

Necesitas PostgreSQL y el discovery-service (Eureka) levantados. Puedes levantar todo el stack con docker-compose desde `biblioteca-deploy`, o ejecutar este servicio solo:

```bash
./mvnw spring-boot:run
```

La configuración de la base de datos se hace por variables de entorno:

| Variable | Descripción |
|---|---|
| `DB_URL` | JDBC URL de PostgreSQL (default `jdbc:postgresql://localhost:5432/biblioteca`) |
| `DB_USER` | Usuario de PostgreSQL |
| `DB_PASSWORD` | Contraseña de PostgreSQL |
| `EUREKA_URL` | URL del servidor Eureka (default `http://localhost:8761/eureka/`) |

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/libros` | Lista todos los libros |
| GET | `/libros/{id}` | Obtiene un libro por id |
| POST | `/libros` | Crea un libro |
| PUT | `/libros/{id}` | Actualiza un libro |
| DELETE | `/libros/{id}` | Elimina un libro (404 si no existe) |
| PATCH | `/libros/{id}/stock` | Ajusta el stock (cantidad positiva suma, negativa resta) |

## Parte de un sistema más grande

La plataforma completa se compone de:

- [discovery-service](https://github.com/jjrmch/discovery-service) — servidor Eureka
- [gateway-service](https://github.com/jjrmch/gateway-service) — API Gateway (punto de entrada, `localhost:8080`)
- [transactions-service](https://github.com/jjrmch/transactions-service) — ventas, alquileres, reservas y multas
- [customer-service](https://github.com/jjrmch/customer-service) — clientes
- [biblioteca-frontend](https://github.com/jjrmch/biblioteca-frontend) — panel web en React
- [biblioteca-deploy](https://github.com/jjrmch/biblioteca-deploy) — docker-compose con el stack completo

## Por mejorar

- No hay tests de negocio todavía, solo el test de contexto de Spring.
- El listado de libros no tiene paginación.
- El CORS está abierto y las rutas no tienen autenticación; es algo pendiente para producción.

## Licencia

MIT

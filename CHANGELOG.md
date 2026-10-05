# Changelog

Todos los cambios relevantes de este proyecto se documentan en este archivo.

El formato está basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/)
y este proyecto sigue [Semantic Versioning](https://semver.org/lang/es/).

## [1.0.0] - 2026-10-05

### Añadido

- CRUD de libros (título, autor, ISBN, precio, stock)
- Búsqueda por título, autor o ISBN, consulta por ISBN y estadísticas del catálogo
- Ajuste de stock atómico: suma o resta ejemplares y rechaza dejar el stock en negativo (HTTP 409)
- Lectura pública del catálogo y escritura restringida a `ADMIN` / `BIBLIOTECARIO` con JWT (HS256)
- Validación de entrada y manejo global de excepciones
- Documentación OpenAPI/Swagger y registro en Eureka

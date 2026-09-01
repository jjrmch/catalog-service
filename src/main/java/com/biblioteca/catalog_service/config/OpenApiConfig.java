package com.biblioteca.catalog_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI catalogOpenAPI() {
        Server server = new Server();
        server.setUrl("/");   // ruta relativa: usará el mismo host desde donde se carga (el gateway)

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Catalog Service API")
                        .description("Gestión del catálogo de libros y su inventario")
                        .version("1.0"));
    }
}
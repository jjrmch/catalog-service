package com.biblioteca.catalog_service;

import com.biblioteca.catalog_service.model.Libro;
import com.biblioteca.catalog_service.repository.LibroRepository;
import com.biblioteca.catalog_service.support.TestJwtFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class LibroSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibroRepository libroRepository;

    private Long libroId;

    private final String tokenBibliotecario = TestJwtFactory.token("biblio@test.com", "BIBLIOTECARIO");
    private final String tokenAdmin = TestJwtFactory.token("admin@test.com", "ADMIN");
    private final String tokenCliente = TestJwtFactory.token("cliente@test.com", "CLIENTE");

    @BeforeEach
    void crearLibro() {
        Libro libro = new Libro(null, "El Quijote", "Cervantes", "isbn-" + UUID.randomUUID(), 20.0, 5);
        libroId = libroRepository.save(libro).getId();
    }

    private String body(String isbn) {
        return """
                {"titulo":"Nuevo","autor":"Autor","isbn":"%s","precio":10.0,"stock":3}
                """.formatted(isbn);
    }

    @Test
    void laLecturaDelCatalogoEsPublica() throws Exception {
        mockMvc.perform(get("/libros")).andExpect(status().isOk());
        mockMvc.perform(get("/libros/" + libroId)).andExpect(status().isOk());
        mockMvc.perform(get("/libros/estadisticas")).andExpect(status().isOk());
    }

    @Test
    void crearSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/libros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("isbn-sin-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void crearConRolClienteDevuelve403() throws Exception {
        mockMvc.perform(post("/libros")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("isbn-cliente-" + UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void crearConBibliotecarioDevuelve201() throws Exception {
        mockMvc.perform(post("/libros")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenBibliotecario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("isbn-" + UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void actualizarConAdminDevuelve200() throws Exception {
        mockMvc.perform(put("/libros/" + libroId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("isbn-" + UUID.randomUUID())))
                .andExpect(status().isOk());
    }

    @Test
    void ajustarStockConBibliotecarioDevuelve200() throws Exception {
        mockMvc.perform(patch("/libros/" + libroId + "/stock")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenBibliotecario)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cantidad":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void eliminarSinTokenDevuelve401() throws Exception {
        mockMvc.perform(delete("/libros/" + libroId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void eliminarConAdminDevuelve204() throws Exception {
        mockMvc.perform(delete("/libros/" + libroId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());
    }
}

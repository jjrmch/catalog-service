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

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class LibroControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LibroRepository libroRepository;

    private final String tokenAdmin = TestJwtFactory.token("admin@test.com", "ADMIN");

    private String titulo;
    private String isbn;
    private Long libroId;

    @BeforeEach
    void crearLibroDePruebas() {
        String sufijo = UUID.randomUUID().toString();
        titulo = "Libro " + sufijo;
        isbn = "isbn-" + sufijo;
        Libro libro = new Libro(null, titulo, "Autor de pruebas", isbn, 20.0, 5);
        libroId = libroRepository.save(libro).getId();
    }

    private String body(String titulo, String autor, String isbn, Double precio, Integer stock) {
        return """
                {"titulo":"%s","autor":"%s","isbn":"%s","precio":%s,"stock":%s}
                """.formatted(titulo, autor, isbn, precio, stock);
    }

    @Test
    void listarLibrosIncluyeElLibroGuardado() throws Exception {
        mockMvc.perform(get("/libros"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(isbn)));
    }

    @Test
    void obtenerLibroPorIdDevuelveElLibro() throws Exception {
        mockMvc.perform(get("/libros/" + libroId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(libroId))
                .andExpect(jsonPath("$.titulo").value(titulo))
                .andExpect(jsonPath("$.isbn").value(isbn));
    }

    @Test
    void obtenerLibroInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/libros/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value(containsString("Libro no encontrado")));
    }

    @Test
    void obtenerLibroPorIsbnDevuelveElLibro() throws Exception {
        mockMvc.perform(get("/libros/isbn/" + isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(isbn));
    }

    @Test
    void obtenerLibroPorIsbnInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/libros/isbn/no-existe-" + UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(containsString("Libro no encontrado")));
    }

    @Test
    void buscarLibrosPorTituloDevuelveCoincidencias() throws Exception {
        mockMvc.perform(get("/libros/buscar").param("q", titulo))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(isbn)));
    }

    @Test
    void estadisticasDevuelvenLosTotalesDelCatalogo() throws Exception {
        mockMvc.perform(get("/libros/estadisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLibros").isNumber())
                .andExpect(jsonPath("$.totalEjemplares").isNumber())
                .andExpect(jsonPath("$.librosAgotados").isNumber());
    }

    @Test
    void crearLibroDevuelve201YPersisteElLibro() throws Exception {
        String nuevoIsbn = "isbn-nuevo-" + UUID.randomUUID();

        mockMvc.perform(post("/libros")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Nuevo Libro", "Autor Nuevo", nuevoIsbn, 15.5, 3)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.isbn").value(nuevoIsbn));

        assertTrue(libroRepository.findByIsbn(nuevoIsbn).isPresent());
    }

    @Test
    void crearLibroConDatosInvalidosDevuelve400ConErrores() throws Exception {
        mockMvc.perform(post("/libros")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("", "", "", -1.0, -1)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Error de validación"))
                .andExpect(jsonPath("$.errores.titulo").value("El título es obligatorio"))
                .andExpect(jsonPath("$.errores.autor").value("El autor es obligatorio"))
                .andExpect(jsonPath("$.errores.isbn").value("El ISBN es obligatorio"))
                .andExpect(jsonPath("$.errores.precio").value("El precio no puede ser negativo"))
                .andExpect(jsonPath("$.errores.stock").value("El stock no puede ser negativo"));
    }

    @Test
    void actualizarLibroCambiaLosDatos() throws Exception {
        mockMvc.perform(put("/libros/" + libroId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Titulo Actualizado", "Autor Actualizado", isbn, 25.0, 8)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Titulo Actualizado"))
                .andExpect(jsonPath("$.precio").value(25.0))
                .andExpect(jsonPath("$.stock").value(8));
    }

    @Test
    void actualizarLibroInexistenteDevuelve404() throws Exception {
        mockMvc.perform(put("/libros/99999999")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("Nadie", "Nadie", "isbn-nadie", 10.0, 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(containsString("Libro no encontrado")));
    }

    @Test
    void eliminarLibroDevuelve204YLoBorra() throws Exception {
        mockMvc.perform(delete("/libros/" + libroId).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        assertTrue(libroRepository.findById(libroId).isEmpty());
    }

    @Test
    void eliminarLibroInexistenteDevuelve404() throws Exception {
        mockMvc.perform(delete("/libros/99999999").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value(containsString("Libro no encontrado")));
    }

    @Test
    void ajustarStockRestaEjemplares() throws Exception {
        mockMvc.perform(patch("/libros/" + libroId + "/stock")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cantidad":-2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    void ajustarStockInsuficienteDevuelve409() throws Exception {
        mockMvc.perform(patch("/libros/" + libroId + "/stock")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cantidad":-999}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").exists());
    }
}

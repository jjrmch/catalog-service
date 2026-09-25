package com.biblioteca.catalog_service.service;

import com.biblioteca.catalog_service.dto.EstadisticasResponse;
import com.biblioteca.catalog_service.dto.LibroRequest;
import com.biblioteca.catalog_service.dto.LibroResponse;
import com.biblioteca.catalog_service.exception.RecursoNoEncontradoException;
import com.biblioteca.catalog_service.exception.StockInsuficienteException;
import com.biblioteca.catalog_service.model.Libro;
import com.biblioteca.catalog_service.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libroRepository;

    @InjectMocks
    private LibroService libroService;

    private Libro libro(Integer stock) {
        return new Libro(1L, "El Quijote", "Cervantes", "978-84-376-0494-7", 20.0, stock);
    }

    private LibroRequest request() {
        LibroRequest request = new LibroRequest();
        request.setTitulo("El Quijote");
        request.setAutor("Cervantes");
        request.setIsbn("978-84-376-0494-7");
        request.setPrecio(20.0);
        request.setStock(5);
        return request;
    }

    @Test
    void listarTodosMapeaLaLista() {
        when(libroRepository.findAll()).thenReturn(List.of(libro(5), libro(0)));

        List<LibroResponse> libros = libroService.listarTodos();

        assertEquals(2, libros.size());
        assertEquals("El Quijote", libros.get(0).getTitulo());
    }

    @Test
    void buscarPorIdDevuelveElLibro() {
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(5)));

        LibroResponse response = libroService.buscarPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals(5, response.getStock());
    }

    @Test
    void buscarPorIdInexistenteLanza404() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> libroService.buscarPorId(99L));
    }

    @Test
    void buscarPorIsbnInexistenteLanza404() {
        when(libroRepository.findByIsbn("no-existe")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> libroService.buscarPorIsbn("no-existe"));
    }

    @Test
    void busquedaVaciaNoConsultaElRepositorio() {
        assertTrue(libroService.listarPorBusqueda("  ").isEmpty());
        verify(libroRepository, never()).findByTituloContainingIgnoreCaseOrAutorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                anyString(), anyString(), anyString());
    }

    @Test
    void guardarPersisteElLibro() {
        when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        LibroResponse response = libroService.guardar(request());

        assertEquals("El Quijote", response.getTitulo());
        assertEquals(5, response.getStock());
    }

    @Test
    void actualizarModificaLosCampos() {
        Libro existente = libro(5);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(libroRepository.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        LibroRequest request = request();
        request.setTitulo("El Quijote (ed. revisada)");
        request.setStock(8);
        LibroResponse response = libroService.actualizar(1L, request);

        assertEquals("El Quijote (ed. revisada)", response.getTitulo());
        assertEquals(8, response.getStock());
    }

    @Test
    void actualizarInexistenteLanza404() {
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> libroService.actualizar(99L, request()));
    }

    @Test
    void eliminarInexistenteLanza404() {
        when(libroRepository.existsById(99L)).thenReturn(false);

        assertThrows(RecursoNoEncontradoException.class, () -> libroService.eliminar(99L));
        verify(libroRepository, never()).deleteById(any());
    }

    @Test
    void estadisticasAgregaLosDatos() {
        when(libroRepository.count()).thenReturn(3L);
        when(libroRepository.sumarStock()).thenReturn(30L);
        when(libroRepository.countByStock(0)).thenReturn(1L);

        EstadisticasResponse estadisticas = libroService.estadisticas();

        assertEquals(3L, estadisticas.getTotalLibros());
        assertEquals(30L, estadisticas.getTotalEjemplares());
        assertEquals(1L, estadisticas.getLibrosAgotados());
    }

    @Test
    void ajustarStockDevuelveElLibroActualizado() {
        when(libroRepository.ajustarStock(1L, -2)).thenReturn(1);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(3)));

        LibroResponse response = libroService.ajustarStock(1L, -2);

        assertEquals(3, response.getStock());
    }

    @Test
    void ajustarStockSinExistenciasLanzaConflicto() {
        when(libroRepository.ajustarStock(1L, -5)).thenReturn(0);
        when(libroRepository.findById(1L)).thenReturn(Optional.of(libro(2)));

        StockInsuficienteException ex = assertThrows(StockInsuficienteException.class,
                () -> libroService.ajustarStock(1L, -5));
        assertTrue(ex.getMessage().contains("Disponible: 2"));
    }

    @Test
    void ajustarStockDeLibroInexistenteLanza404() {
        when(libroRepository.ajustarStock(99L, -1)).thenReturn(0);
        when(libroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> libroService.ajustarStock(99L, -1));
    }
}

package com.biblioteca.catalog_service.service;

import com.biblioteca.catalog_service.dto.AlquilerRequest;
import com.biblioteca.catalog_service.dto.AlquilerResponse;
import com.biblioteca.catalog_service.exception.EstadoInvalidoException;
import com.biblioteca.catalog_service.exception.RecursoNoEncontradoException;
import com.biblioteca.catalog_service.exception.StockInsuficienteException;
import com.biblioteca.catalog_service.model.Alquiler;
import com.biblioteca.catalog_service.model.EstadoAlquiler;
import com.biblioteca.catalog_service.model.Libro;
import com.biblioteca.catalog_service.repository.AlquilerRepository;
import com.biblioteca.catalog_service.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlquilerService {

    private final AlquilerRepository alquilerRepository;
    private final LibroRepository libroRepository;

    public AlquilerService(AlquilerRepository alquilerRepository, LibroRepository libroRepository) {
        this.alquilerRepository = alquilerRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional
    public AlquilerResponse alquilar(AlquilerRequest request) {
        Libro libro = libroRepository.findById(request.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Libro no encontrado con id: " + request.getLibroId()));

        if (libro.getStock() < 1) {
            throw new StockInsuficienteException(
                    "No hay ejemplares disponibles para alquilar");
        }

        // Baja el stock
        libro.setStock(libro.getStock() - 1);
        libroRepository.save(libro);

        Alquiler alquiler = new Alquiler();
        alquiler.setLibro(libro);
        alquiler.setCliente(request.getCliente());
        alquiler.setFechaAlquiler(LocalDateTime.now());
        alquiler.setFechaDevolucion(null);
        alquiler.setEstado(EstadoAlquiler.ACTIVO);

        return aResponse(alquilerRepository.save(alquiler));
    }

    @Transactional
    public AlquilerResponse devolver(Long alquilerId) {
        Alquiler alquiler = alquilerRepository.findById(alquilerId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Alquiler no encontrado con id: " + alquilerId));

        // No se puede devolver algo ya devuelto
        if (alquiler.getEstado() == EstadoAlquiler.DEVUELTO) {
            throw new EstadoInvalidoException("Este alquiler ya fue devuelto");
        }

        // Sube el stock
        Libro libro = alquiler.getLibro();
        libro.setStock(libro.getStock() + 1);
        libroRepository.save(libro);

        // Actualiza el alquiler
        alquiler.setEstado(EstadoAlquiler.DEVUELTO);
        alquiler.setFechaDevolucion(LocalDateTime.now());

        return aResponse(alquilerRepository.save(alquiler));
    }

    public List<AlquilerResponse> listarTodos() {
        return alquilerRepository.findAll()
                .stream()
                .map(alquiler -> aResponse(alquiler))
                .toList();
    }

    private AlquilerResponse aResponse(Alquiler alquiler) {
        return new AlquilerResponse(
                alquiler.getId(),
                alquiler.getLibro().getId(),
                alquiler.getLibro().getTitulo(),
                alquiler.getCliente(),
                alquiler.getFechaAlquiler(),
                alquiler.getFechaDevolucion(),
                alquiler.getEstado()
        );
    }
}
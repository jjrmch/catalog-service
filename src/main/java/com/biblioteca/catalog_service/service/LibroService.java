package com.biblioteca.catalog_service.service;

import com.biblioteca.catalog_service.dto.LibroRequest;
import com.biblioteca.catalog_service.dto.LibroResponse;
import com.biblioteca.catalog_service.model.Libro;
import com.biblioteca.catalog_service.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.biblioteca.catalog_service.exception.RecursoNoEncontradoException;
import com.biblioteca.catalog_service.exception.StockInsuficienteException;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    public List<LibroResponse> listarTodos() {
        return libroRepository.findAll()
                .stream()
                .map(libro -> aResponse(libro))
                .toList();
    }

    @Transactional
    public LibroResponse ajustarStock(Long id, Integer cantidad) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Libro no encontrado con id: " + id));

        int nuevoStock = libro.getStock() + cantidad;

        if (nuevoStock < 0) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Disponible: " + libro.getStock()
                    + ", solicitado: " + Math.abs(cantidad));
        }

        libro.setStock(nuevoStock);
        return aResponse(libroRepository.save(libro));
    }

    public LibroResponse buscarPorId(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado con id: " + id));
        return aResponse(libro);
    }

    public LibroResponse guardar(LibroRequest request) {
        Libro libro = aEntidad(request);
        Libro guardado = libroRepository.save(libro);
        return aResponse(guardado);
    }

    public LibroResponse actualizar(Long id, LibroRequest request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro no encontrado con id: " + id));

        libro.setTitulo(request.getTitulo());
        libro.setAutor(request.getAutor());
        libro.setIsbn(request.getIsbn());
        libro.setPrecio(request.getPrecio());
        libro.setStock(request.getStock());

        return aResponse(libroRepository.save(libro));
    }

    public void eliminar(Long id) {
        libroRepository.deleteById(id);
    }


    private LibroResponse aResponse(Libro libro) {
        return new LibroResponse(
                libro.getId(),
                libro.getTitulo(),
                libro.getAutor(),
                libro.getIsbn(),
                libro.getPrecio(),
                libro.getStock()
        );
    }

    private Libro aEntidad(LibroRequest request) {
        Libro libro = new Libro();
        libro.setTitulo(request.getTitulo());
        libro.setAutor(request.getAutor());
        libro.setIsbn(request.getIsbn());
        libro.setPrecio(request.getPrecio());
        libro.setStock(request.getStock());
        return libro;
    }
}
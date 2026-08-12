package com.biblioteca.catalog_service.service;

import com.biblioteca.catalog_service.dto.VentaRequest;
import com.biblioteca.catalog_service.dto.VentaResponse;
import com.biblioteca.catalog_service.exception.RecursoNoEncontradoException;
import com.biblioteca.catalog_service.exception.StockInsuficienteException;
import com.biblioteca.catalog_service.model.Libro;
import com.biblioteca.catalog_service.model.Venta;
import com.biblioteca.catalog_service.repository.LibroRepository;
import com.biblioteca.catalog_service.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final LibroRepository libroRepository;

    public VentaService(VentaRepository ventaRepository, LibroRepository libroRepository) {
        this.ventaRepository = ventaRepository;
        this.libroRepository = libroRepository;
    }

    @Transactional
    public VentaResponse vender(VentaRequest request) {
        //Buscar el libro
        Libro libro = libroRepository.findById(request.getLibroId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Libro no encontrado con id: " + request.getLibroId()));

        //Comprobar stock
        if (libro.getStock() < request.getCantidad()) {
            throw new StockInsuficienteException(
                    "Stock insuficiente. Disponible: " + libro.getStock()
                    + ", solicitado: " + request.getCantidad());
        }

        //Bajar el stock
        libro.setStock(libro.getStock() - request.getCantidad());
        libroRepository.save(libro);

        //Registrar la venta
        Venta venta = new Venta();
        venta.setLibro(libro);
        venta.setCantidad(request.getCantidad());
        venta.setPrecioTotal(libro.getPrecio() * request.getCantidad());
        venta.setCliente(request.getCliente());
        venta.setFecha(LocalDateTime.now());

        Venta guardada = ventaRepository.save(venta);

        return aResponse(guardada);
    }

    public List<VentaResponse> listarTodas() {
        return ventaRepository.findAll()
                .stream()
                .map(venta -> aResponse(venta))
                .toList();
    }

    private VentaResponse aResponse(Venta venta) {
        return new VentaResponse(
                venta.getId(),
                venta.getLibro().getId(),
                venta.getLibro().getTitulo(),
                venta.getCantidad(),
                venta.getPrecioTotal(),
                venta.getCliente(),
                venta.getFecha()
        );
    }
}
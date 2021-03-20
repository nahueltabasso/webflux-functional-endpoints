package com.springboot.webflux.apirest.app.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.springboot.webflux.apirest.app.models.documents.Producto;
import com.springboot.webflux.apirest.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.BodyInserters.*;

import java.net.URI;
import java.util.Date;

@Component
public class ProductoHandler {
	
	@Autowired
	private ProductoService service;
	
	@Autowired
	private Validator validator;

	public Mono<ServerResponse> getAll(ServerRequest request) {
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
				.body(service.findAll(), Producto.class);
	}
	
	public Mono<ServerResponse> getById(ServerRequest request) {
		String id = request.pathVariable("id");
		return service.findById(id).flatMap(p -> ServerResponse.ok().body(fromObject(p))).switchIfEmpty(ServerResponse.notFound().build());
	}
	
	public Mono<ServerResponse> add(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		return producto.flatMap(p -> {
			Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
			validator.validate(p, errors);
			
			if (errors.hasErrors()) {
				return Flux.fromIterable(errors.getFieldErrors())
						.map(error -> "El campo " + error.getField() + " " + error.getDefaultMessage())
						.collectList()
						.flatMap(list -> ServerResponse.badRequest().body(fromObject(list)));
			} else {
				if (p.getCreateAt() == null) p.setCreateAt(new Date());
				
				return service.save(p).flatMap(pdb -> ServerResponse.created(URI.create("/api/v2/producto/".concat(pdb.getId())))
						.contentType(MediaType.APPLICATION_JSON).body(fromObject(pdb)));
			}
		});
	}
	
	public Mono<ServerResponse> update(ServerRequest request) {
		Mono<Producto> producto = request.bodyToMono(Producto.class);
		String id = request.pathVariable("id");
		Mono<Producto> productoDb = service.findById(id);
		return productoDb.zipWith(producto, (db, req) -> {
			db.setNombre(req.getNombre());
			db.setPrecio(req.getPrecio());
			db.setCategoria(req.getCategoria());
			return db;
		}).flatMap(p -> ServerResponse.created(URI.create("/api/v2/producto/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON).body(service.save(p), Producto.class))
				.switchIfEmpty(ServerResponse.notFound().build());
	}

	public Mono<ServerResponse> delete(ServerRequest request) {
		String id = request.pathVariable("id");
		Mono<Producto> productoDb = service.findById(id);
		return productoDb.flatMap(p -> service.delete(p).then(ServerResponse.noContent().build()))
				.switchIfEmpty(ServerResponse.notFound().build());
	}
	
}

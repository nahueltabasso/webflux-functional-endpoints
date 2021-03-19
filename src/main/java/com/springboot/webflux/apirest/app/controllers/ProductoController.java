package com.springboot.webflux.apirest.app.controllers;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;

import com.springboot.webflux.apirest.app.models.documents.Producto;
import com.springboot.webflux.apirest.app.models.services.ProductoService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/producto")
public class ProductoController {

	@Autowired
	private ProductoService productoService;
	@Value("${config.uploads.path}")
	private String path;
	
	@GetMapping
	public Mono<ResponseEntity<Flux<Producto>>> getAll() {
		return Mono.just(ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(productoService.findAll()));
	}
	
	@GetMapping("/{id}")
	public Mono<ResponseEntity<Producto>> getById(@PathVariable String id) {
		return productoService.findById(id).map(p -> ResponseEntity.ok(p));
	}
	
	@PostMapping
	public Mono<ResponseEntity<Map<String, Object>>> add(@Valid @RequestBody Mono<Producto> monoProducto) {
		Map<String, Object> response = new HashMap<String, Object>();
		return monoProducto.flatMap(producto -> {
			if (producto.getCreateAt() == null) {
				producto.setCreateAt(new Date());
			}
			
			return productoService.save(producto).map(p -> {
				response.put("producto", p);
				return ResponseEntity.created(URI.create("/api/producto/".concat(p.getId())))
				 .contentType(MediaType.APPLICATION_JSON)
				 .body(response);
			});			
		}).onErrorResume(e -> {
			return Mono.just(e).cast(WebExchangeBindException.class)
					.flatMap(ex -> Mono.just(ex.getFieldErrors()))
					.flatMapMany(errors -> Flux.fromIterable(errors))
					.map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
					.collectList()
					.flatMap(listError -> {
						response.put("errors", listError);
						return Mono.just(ResponseEntity.badRequest().body(response));
					});
		});
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<Producto>> update(@RequestBody Producto producto, @PathVariable String id) {
		return productoService.findById(id).flatMap(p -> {
			p.setNombre(producto.getNombre());
			p.setPrecio(producto.getPrecio());
			p.setCategoria(producto.getCategoria());
			return productoService.save(p);
		}).map(p -> ResponseEntity.created(URI.create("/api/producto/".concat(p.getId())))
				.contentType(MediaType.APPLICATION_JSON)
				.body(p))
		.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
		return productoService.findById(id).flatMap(p -> {
			return productoService.delete(p).then(Mono.just(new ResponseEntity<Void>(HttpStatus.NO_CONTENT)));
		}).defaultIfEmpty(new ResponseEntity<Void>(HttpStatus.NOT_FOUND));
	}
	
	@PostMapping("/upload/{id}")
	public Mono<ResponseEntity<Producto>> upload(@PathVariable String id, @RequestPart FilePart file) {
		return productoService.findById(id).flatMap(p -> {
			p.setFoto(UUID.randomUUID().toString() + " - " + file.filename().replace(" ", "")
																			.replace(":", "")
																			.replace("\\", ""));
			return file.transferTo(new File(path + p.getFoto())).then(productoService.save(p));
		}).map(p -> ResponseEntity.ok(p)).defaultIfEmpty(ResponseEntity.notFound().build());
	}
}

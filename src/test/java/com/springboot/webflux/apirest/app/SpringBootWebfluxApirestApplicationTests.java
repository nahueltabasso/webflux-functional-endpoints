package com.springboot.webflux.apirest.app;

import java.util.Collections;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.springboot.webflux.apirest.app.models.documents.Producto;
import com.springboot.webflux.apirest.app.models.services.ProductoService;

import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringBootWebfluxApirestApplicationTests {
	
	@Autowired
	private WebTestClient client;
	@Autowired
	private ProductoService service;

	@Test
	public void getAllTest() {
		client.get()
			.uri("/api/v2/producto")
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBodyList(Producto.class)
			.hasSize(8);
	}
	
	@Test
	public void getByIdTest() {
		// Recuperamos algun producto
		Mono<Producto> producto = service.getByNombre("Iphone 11 64GB");
		
		client.get()
			.uri("/api/v2/producto/{id}", Collections.singletonMap("id", producto.block().getId()))
			.accept(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.nombre").isEqualTo("Iphone 11 64GB");
	}
	
	@Test
	public void addTest() {
		Producto p = new Producto();
		p.setNombre("Producto_Test");
		p.setPrecio(100.00);
		
		client.post()
			.uri("/api/v2/producto")
			.contentType(MediaType.APPLICATION_JSON)	// ContentType del request
			.accept(MediaType.APPLICATION_JSON)			// ContentType del response
			.body(Mono.just(p), Producto.class)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.nombre").isEqualTo("Producto_Test")
			.jsonPath("$.precio").isEqualTo(100.00);
	}
	
	@Test
	public void addV2Test() {
		Producto p = new Producto();
		p.setNombre("Producto_Test");
		p.setPrecio(100.00);
		
		client.post()
			.uri("/api/v2/producto")
			.contentType(MediaType.APPLICATION_JSON)	// ContentType del request
			.accept(MediaType.APPLICATION_JSON)			// ContentType del response
			.body(Mono.just(p), Producto.class)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody(Producto.class)
			.consumeWith(response -> {
				Producto prod = response.getResponseBody();
				Assertions.assertThat(prod.getId()).isNotEmpty();
				Assertions.assertThat(prod.getNombre()).isNotEmpty();
				Assertions.assertThat(prod.getPrecio()).isNotNull();
				Assertions.assertThat(prod.getCreateAt()).isNotNull();
				Assertions.assertThat(prod.getCategoria()).isNull();;
			});
	}
	
	@Test
	public void updateTest() {
		Producto producto = service.getByNombre("Iphone 11 64GB").block();
		
		Producto productoEditado = new Producto();
		productoEditado.setNombre("Iphone 12");
		
		client.put()
			.uri("/api/v2/producto/{id}", Collections.singletonMap("id", producto.getId()))
			.contentType(MediaType.APPLICATION_JSON)	// ContentType del request
			.accept(MediaType.APPLICATION_JSON)			// ContentType del response
			.body(Mono.just(productoEditado), Producto.class)
			.exchange()
			.expectStatus().isCreated()
			.expectHeader().contentType(MediaType.APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.id").isNotEmpty()
			.jsonPath("$.nombre").isEqualTo("Iphone 12")
			.jsonPath("$.precio").isEqualTo(producto.getPrecio());
	}
	
	@Test
	public void deleteTest() {
		Producto producto = service.getByNombre("Iphone 11 64GB").block();

		client.delete()
			.uri("/api/v2/producto/{id}", Collections.singletonMap("id", producto.getId()))
			.exchange()
			.expectStatus().isNoContent()
			.expectBody().isEmpty();
		
		// Verificamos que se haya eliminado
		client.get()
			.uri("/api/v2/producto/{id}", Collections.singletonMap("id", producto.getId()))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();
	}
}

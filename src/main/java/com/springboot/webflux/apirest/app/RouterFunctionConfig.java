package com.springboot.webflux.apirest.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.springboot.webflux.apirest.app.handler.ProductoHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.*;


@Configuration
public class RouterFunctionConfig {
	
	@Bean
	public RouterFunction<ServerResponse> routes(ProductoHandler handler) {
		return route(GET("/api/v2/producto"), request -> handler.getAll(request))
				.andRoute(GET("/api/v2/producto/{id}"), request -> handler.getById(request))
				.andRoute(POST("/api/v2/producto"), request -> handler.add(request))
				.andRoute(PUT("/api/v2/producto/{id}"), request -> handler.update(request))
				.andRoute(DELETE("/api/v2/producto/{id}"), request -> handler.delete(request));
	}
}

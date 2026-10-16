package com.fundigsac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.modulith.Modulithic;

/**
 * Backend de FUNDIGSAC: monolito modular. Cada paquete de primer nivel es un módulo de negocio (contexto acotado)
 * con capas domain → application → infrastructure/web; Spring Modulith verifica que ningún módulo use el interior de
 * otro (ver ModularityTests). Un módulo puede extraerse a un servicio propio cambiando sus eventos a un broker.
 */
@Modulithic(systemName = "FUNDIGSAC", sharedModules = "shared")
@SpringBootApplication
@ConfigurationPropertiesScan
public class FundigsacApplication {

	public static void main(String[] args) {
		SpringApplication.run(FundigsacApplication.class, args);
	}

}

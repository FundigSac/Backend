package com.fundigsac.shared.infrastructure;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Reloj inyectable (UTC) para que las reglas de tiempo sean comprobables, y tareas asíncronas y programadas. */
@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableScheduling
class PlatformConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}

package com.fundigsac.architecture;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import com.fundigsac.FundigsacApplication;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica los límites entre módulos (ningún módulo usa el interior de otro, solo sus interfaces "api", sin ciclos y
 * respetando allowedDependencies) y genera la documentación C4 de los módulos en target/spring-modulith-docs.
 */
class ModularityTests {

	static final ApplicationModules MODULES = ApplicationModules.of(FundigsacApplication.class);

	@Test
	void modulesRespectTheirBoundaries() {
		MODULES.verify();
	}

	@Test
	void writesModuleDocumentation() {
		new Documenter(MODULES).writeModulesAsPlantUml().writeIndividualModulesAsPlantUml().writeModuleCanvases();
		Path docs = Path.of("target", "spring-modulith-docs");
		assertThat(docs.resolve("components.puml")).isNotEmptyFile();
		assertThat(docs.resolve("module-identity.puml")).isNotEmptyFile();
		assertThat(docs.resolve("module-identity.adoc")).isNotEmptyFile();
	}

}

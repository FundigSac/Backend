package com.fundigsac.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Reglas de la arquitectura hexagonal dentro de cada módulo: el dominio es Java puro (sin Spring, JDBC, Jackson ni
 * Servlet), la aplicación no conoce la web ni los adaptadores, y los controladores no tocan la base de datos.
 */
class HexagonalArchitectureTests {

	private static final JavaClasses CLASSES = new ClassFileImporter()
		.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
		.importPackages("com.fundigsac");

	@Test
	void domainIsPureJava() {
		classes().that()
			.resideInAPackage("com.fundigsac..domain..")
			.should()
			.onlyDependOnClassesThat()
			.resideInAnyPackage("java..", "org.jspecify..", "com.fundigsac.shared.domain..", "com.fundigsac.shared.security..",
					"com.fundigsac.*.domain..", "com.fundigsac.*.api..")
			.check(CLASSES);
	}

	@Test
	void applicationDoesNotDependOnAdapters() {
		noClasses().that()
			.resideInAPackage("com.fundigsac..application..")
			.should()
			.dependOnClassesThat()
			.resideInAnyPackage("com.fundigsac..web..", "com.fundigsac..infrastructure..", "org.springframework.jdbc..",
					"jakarta.servlet..", "org.springframework.web..")
			.check(CLASSES);
	}

	@Test
	void controllersDoNotAccessPersistence() {
		noClasses().that()
			.resideInAPackage("com.fundigsac..web..")
			.should()
			.dependOnClassesThat()
			.resideInAnyPackage("com.fundigsac..infrastructure..", "org.springframework.jdbc..")
			.check(CLASSES);
	}

	@Test
	void infrastructureIsNotUsedByDomain() {
		noClasses().that()
			.resideInAPackage("com.fundigsac..domain..")
			.should()
			.dependOnClassesThat()
			.resideInAnyPackage("com.fundigsac..infrastructure..", "com.fundigsac..application..", "com.fundigsac..web..")
			.check(CLASSES);
	}

}

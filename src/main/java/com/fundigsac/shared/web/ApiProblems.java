package com.fundigsac.shared.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Errores de una operación que el contrato OpenAPI no puede deducir de su firma (reglas de negocio propias de la
 * operación). Los errores comunes los documenta {@link ProblemResponses}.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiProblems {

	int[] value();

}

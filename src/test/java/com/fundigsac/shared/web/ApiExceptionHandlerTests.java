package com.fundigsac.shared.web;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Caminos de error que no se alcanzan en un flujo normal: error inesperado y fallos de integridad de la base de datos. */
class ApiExceptionHandlerTests {

	private static final String SECRET_DETAIL = "SELECT * FROM user_accounts WHERE email = 'ana.perez@correo.pe'";

	@RestController
	static class FailingController {

		@GetMapping("/inesperado")
		void unexpected() {
			throw new IllegalStateException(SECRET_DETAIL);
		}

		@GetMapping("/duplicado")
		void duplicate() {
			throw violation("23505");
		}

		@GetMapping("/caracter-invalido")
		void invalidData() {
			throw violation("22021");
		}

		@GetMapping("/sin-estado")
		void withoutState() {
			throw new DataIntegrityViolationException("Sin causa SQL");
		}

		private static DataIntegrityViolationException violation(String sqlState) {
			return new DataIntegrityViolationException(SECRET_DETAIL, new SQLException(SECRET_DETAIL, sqlState));
		}

	}

	private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new FailingController())
		.setControllerAdvice(new ApiExceptionHandler())
		.build();

	@Test
	void anUnexpectedErrorIsAProblemWithoutInternalDetails() throws Exception {
		String body = this.mvc.perform(get("/inesperado"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertThat(body).doesNotContain("SELECT", "ana.perez", "IllegalStateException", "at com.fundigsac");
	}

	@Test
	void aDuplicateIsAConflictWithoutInternalDetails() throws Exception {
		String body = this.mvc.perform(get("/duplicado"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("CONFLICT"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertThat(body).doesNotContain("SELECT", "ana.perez");
	}

	@Test
	void aValueTheDatabaseCannotStoreIsABadRequest() throws Exception {
		String body = this.mvc.perform(get("/caracter-invalido"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_VALUE"))
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertThat(body).doesNotContain("SELECT", "ana.perez");
	}

	@Test
	void aViolationWithoutSqlStateStaysAConflict() throws Exception {
		this.mvc.perform(get("/sin-estado")).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CONFLICT"));
	}

}

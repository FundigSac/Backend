package com.fundigsac.support;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.MockMvcBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;

import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Comprueba contra el contrato OpenAPI cada respuesta que pasa por MockMvc en las pruebas de integración. Una respuesta
 * con un código no documentado o un cuerpo fuera de esquema hace fallar la prueba que la produjo.
 */
@TestConfiguration(proxyBeanMethods = false)
public class OpenApiContractConfiguration {

	@Bean
	ContractCheck contractCheck(ObjectProvider<MockMvc> mvc, JsonMapper json) {
		return new ContractCheck(mvc, json);
	}

	@Bean
	MockMvcBuilderCustomizer contractCheckCustomizer(ContractCheck check) {
		return (builder) -> builder.alwaysDo(check);
	}

	/** Carga el contrato una sola vez y valida cada resultado. */
	public static final class ContractCheck implements ResultHandler {

		private static final String DOCUMENT = "/v3/api-docs";

		private final ObjectProvider<MockMvc> mvc;

		private final JsonMapper json;

		private volatile OpenApiContract contract;

		ContractCheck(ObjectProvider<MockMvc> mvc, JsonMapper json) {
			this.mvc = mvc;
			this.json = json;
		}

		public OpenApiContract contract() throws Exception {
			if (this.contract == null) {
				synchronized (this) {
					if (this.contract == null) {
						byte[] document = this.mvc.getObject().perform(get(DOCUMENT)).andReturn().getResponse().getContentAsByteArray();
						this.contract = new OpenApiContract(this.json.readTree(document), this.json);
					}
				}
			}
			return this.contract;
		}

		@Override
		public void handle(MvcResult result) throws Exception {
			String uri = result.getRequest().getRequestURI();
			if (uri.startsWith(DOCUMENT)) {
				return;
			}
			List<String> violations = contract().violations(result.getRequest().getMethod(), uri, result.getResponse().getStatus(),
					result.getResponse().getContentType(), result.getResponse().getContentAsByteArray());
			if (!violations.isEmpty()) {
				throw new AssertionError("La respuesta no cumple el contrato OpenAPI:\n" + String.join("\n", violations));
			}
		}

	}

}

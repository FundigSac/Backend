package com.fundigsac.cart;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Carrito: cambiar líneas, vaciarlo, aislamiento entre clientes y reintentos seguros al enviar a cotizar. */
class CartManagementIT extends IntegrationTest {

	private static final String ITEMS = "/api/v1/cart/items";

	private static final String CART = "/api/v1/cart";

	private static final String SUBMIT = "/api/v1/cart/submit";

	private static final String UNKNOWN_ITEM = ITEMS + "/00000000-0000-7000-8000-00000000abcd";

	private String addItem(String token) throws Exception {
		return body(this.mvc.perform(authed(postJson(ITEMS, Map.of("productSlug", "vcp-f4-01", "quantity", 2)), token))
			.andExpect(status().isOk())
			.andReturn()).get("items").get(0).get("id").asString();
	}

	@Test
	void changesQuantityAndNote() throws Exception {
		String token = customerToken("carrito.cambia@andina.pe", "frase-de-paso-muy-larga");
		String itemId = addItem(token);
		this.mvc.perform(authed(patchJson(ITEMS + "/" + itemId, Map.of("quantity", 9, "note", "Con volante")), token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].quantity").value(9))
			.andExpect(jsonPath("$.items[0].note").value("Con volante"))
			.andExpect(jsonPath("$.totalUnits").value(9));
	}

	@Test
	void quantityMustStayInRange() throws Exception {
		String token = customerToken("carrito.rango@andina.pe", "frase-de-paso-muy-larga");
		String itemId = addItem(token);
		for (int quantity : new int[] { 0, 1000 }) {
			this.mvc.perform(authed(patchJson(ITEMS + "/" + itemId, Map.of("quantity", quantity)), token))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].field").value("quantity"))
				.andExpect(jsonPath("$.errors[0].code").value("OUT_OF_RANGE"));
		}
		this.mvc.perform(authed(patchJson(ITEMS + "/" + itemId, Map.of("quantity", 1, "precio", 10)), token))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("UNKNOWN_FIELD"));
	}

	@Test
	void anotherCustomersItemIsNotFound() throws Exception {
		String owner = customerToken("carrito.dueno@andina.pe", "frase-de-paso-muy-larga");
		String other = customerToken("carrito.ajeno@andina.pe", "frase-de-paso-muy-larga");
		String itemId = addItem(owner);
		addItem(other);

		this.mvc.perform(authed(patchJson(ITEMS + "/" + itemId, Map.of("quantity", 5)), other))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("CART_ITEM_NOT_FOUND"));
		this.mvc.perform(authed(delete(ITEMS + "/" + itemId).with(randomIp()), other))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("CART_ITEM_NOT_FOUND"));
		this.mvc.perform(authed(patchJson(UNKNOWN_ITEM, Map.of("quantity", 5)), owner)).andExpect(status().isNotFound());
		this.mvc.perform(getAs(CART, owner)).andExpect(jsonPath("$.items[0].quantity").value(2));
	}

	@Test
	void clearingEmptiesOnlyTheOwnCart() throws Exception {
		String owner = customerToken("carrito.vacia@andina.pe", "frase-de-paso-muy-larga");
		String other = customerToken("carrito.conserva@andina.pe", "frase-de-paso-muy-larga");
		addItem(owner);
		addItem(other);

		this.mvc.perform(authed(delete(CART).with(randomIp()), owner))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lineCount").value(0))
			.andExpect(jsonPath("$.totalUnits").value(0));
		this.mvc.perform(getAs(CART, owner)).andExpect(jsonPath("$.lineCount").value(0));
		this.mvc.perform(getAs(CART, other)).andExpect(jsonPath("$.lineCount").value(1));
	}

	@Test
	void cartRequiresACustomerSession() throws Exception {
		this.mvc.perform(patchJson(UNKNOWN_ITEM, Map.of("quantity", 5))).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
		this.mvc.perform(delete(CART).with(randomIp())).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
		String admin = adminToken();
		this.mvc.perform(authed(patchJson(UNKNOWN_ITEM, Map.of("quantity", 5)), admin)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
		this.mvc.perform(authed(delete(CART).with(randomIp()), admin)).andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	private static Map<String, Object> checkout(String department) {
		Map<String, Object> body = new HashMap<>();
		body.put("deliveryDepartment", department);
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	@Test
	void retryingTheSubmissionReturnsTheSameFolio() throws Exception {
		String token = customerToken("carrito.reintento@andina.pe", "frase-de-paso-muy-larga");
		addItem(token);
		String key = "carrito-reintento-001";

		String first = body(this.mvc.perform(authed(postJson(SUBMIT, checkout("Cusco")), token).header("Idempotency-Key", key))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		// El reintento llega con el carrito ya vacío: debe devolver la misma solicitud, sin crear otra.
		String second = body(this.mvc.perform(authed(postJson(SUBMIT, checkout("Cusco")), token).header("Idempotency-Key", key))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		assertThat(second).isEqualTo(first);
		assertThat(count("SELECT count(*) FROM quote_requests WHERE customer_id = CAST(:u AS uuid)",
				Map.of("u", userId("carrito.reintento@andina.pe")))).isEqualTo(1);
	}

	@Test
	void reusingTheKeyWithOtherDataIsRejected() throws Exception {
		String token = customerToken("carrito.clave@andina.pe", "frase-de-paso-muy-larga");
		addItem(token);
		String key = "carrito-reintento-002";
		this.mvc.perform(authed(postJson(SUBMIT, checkout("Cusco")), token).header("Idempotency-Key", key)).andExpect(status().isCreated());
		addItem(token);
		this.mvc.perform(authed(postJson(SUBMIT, checkout("Piura")), token).header("Idempotency-Key", key))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REUSED"));
		this.mvc.perform(authed(postJson(SUBMIT, checkout("Piura")), token).header("Idempotency-Key", "corta"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].code").value("INVALID_IDEMPOTENCY_KEY"));
	}

	@Test
	void idempotencyKeysAreIsolatedBetweenCustomers() throws Exception {
		String owner = customerToken("carrito.clave.uno@andina.pe", "frase-de-paso-muy-larga");
		String other = customerToken("carrito.clave.dos@andina.pe", "frase-de-paso-muy-larga");
		addItem(owner);
		addItem(other);
		String key = "carrito-reintento-003";
		String folio = body(this.mvc.perform(authed(postJson(SUBMIT, checkout("Cusco")), owner).header("Idempotency-Key", key))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		// Con la misma clave, otro cliente registra su propia solicitud y nunca recibe el folio ajeno.
		String otherFolio = body(this.mvc.perform(authed(postJson(SUBMIT, checkout("Cusco")), other).header("Idempotency-Key", key))
			.andExpect(status().isCreated())
			.andReturn()).get("folio").asString();
		assertThat(otherFolio).isNotEqualTo(folio);
		this.mvc.perform(getAs("/api/v1/account/quotes/" + folio, other)).andExpect(status().isNotFound());
	}

	@Test
	void aFailedSubmissionDoesNotConsumeTheKey() throws Exception {
		String token = customerToken("carrito.clave.vacio@andina.pe", "frase-de-paso-muy-larga");
		String key = "carrito-reintento-004";
		this.mvc.perform(authed(postJson(SUBMIT, checkout("Cusco")), token).header("Idempotency-Key", key))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("CART_EMPTY"));
		addItem(token);
		this.mvc.perform(authed(postJson(SUBMIT, checkout("Cusco")), token).header("Idempotency-Key", key)).andExpect(status().isCreated());
	}

}

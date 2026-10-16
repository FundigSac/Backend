package com.fundigsac.cart;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fundigsac.support.IntegrationTest;
import tools.jackson.databind.JsonNode;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Carrito del cliente: acumula líneas, valida contra el catálogo y se convierte en una cotización vinculada. */
class CartIT extends IntegrationTest {

	@Test
	void cartBecomesALinkedQuote() throws Exception {
		String token = customerToken("carrito@andina.pe", "frase-segura-para-compras");
		this.mvc.perform(authed(postJson("/api/v1/cart/items", Map.of("productSlug", "acople-gran-rango-agr",
				"variantLabel", "DN150 · 155–175 mm", "quantity", 4)), token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lineCount").value(1));
		this.mvc.perform(authed(postJson("/api/v1/cart/items", Map.of("productSlug", "acople-gran-rango-agr",
				"variantLabel", "DN150 · 155–175 mm", "quantity", 3)), token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].quantity").value(7));
		JsonNode cart = body(this.mvc.perform(authed(postJson("/api/v1/cart/items", Map.of("productSlug", "vcp-f4-01",
				"quantity", 2, "note", "Con volante")), token)).andExpect(status().isOk()).andReturn());
		String itemId = cart.get("items").get(1).get("id").asString();
		this.mvc.perform(authed(delete("/api/v1/cart/items/" + itemId).with(randomIp()), token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lineCount").value(1));
		this.mvc.perform(authed(postJson("/api/v1/cart/items", Map.of("productSlug", "no-existe", "quantity", 1)), token))
			.andExpect(status().isBadRequest());

		String folio = body(this.mvc.perform(authed(postJson("/api/v1/cart/submit",
				Map.of("deliveryDepartment", "Arequipa", "privacyNoticeVersion", "2026-10", "acceptPrivacyNotice", true)), token))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.folio").value(matchesPattern("COT-\\d{4}-\\d{6}")))
			.andReturn()).get("folio").asString();

		this.mvc.perform(authed(get("/api/v1/cart").with(randomIp()), token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.lineCount").value(0));
		this.mvc.perform(authed(get("/api/v1/account/quotes").with(randomIp()), token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].folio").value(folio))
			.andExpect(jsonPath("$.items[0].origin").value("CART"));
		this.mvc.perform(authed(get("/api/v1/account/quotes/" + folio).with(randomIp()), token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items[0].quantity").value(7))
			.andExpect(jsonPath("$.contact.ruc").value("20131312955"));

		// Otro cliente no ve la solicitud (autorización por objeto: 404, no 403).
		String other = customerToken("otro.cliente@andina.pe", "otra-clave-larga-2026");
		this.mvc.perform(authed(get("/api/v1/account/quotes/" + folio).with(randomIp()), other)).andExpect(status().isNotFound());
		this.mvc.perform(authed(postJson("/api/v1/cart/submit",
				Map.of("privacyNoticeVersion", "2026-10", "acceptPrivacyNotice", true)), other))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.code").value("CART_EMPTY"));
	}

	@Test
	void staffCannotUseTheCustomerCart() throws Exception {
		this.mvc.perform(authed(get("/api/v1/cart").with(randomIp()), adminToken())).andExpect(status().isForbidden());
	}

}

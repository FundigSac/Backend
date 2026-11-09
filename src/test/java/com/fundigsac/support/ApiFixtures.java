package com.fundigsac.support;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.fundigsac.shared.domain.PeruTime;

/** Cuerpos válidos de la API, compartidos por las pruebas que recorren muchas operaciones. */
public final class ApiFixtures {

	private static final AtomicLong SEQUENCE = new AtomicLong(System.currentTimeMillis() % 1_000_000);

	private ApiFixtures() {
	}

	/** Sufijo único dentro de la corrida: la base de datos de pruebas es compartida. */
	public static long unique() {
		return SEQUENCE.incrementAndGet();
	}

	public static Map<String, Object> product(String slug) {
		Map<String, Object> body = new HashMap<>();
		body.put("categorySlug", "valvulas");
		body.put("slug", slug);
		body.put("sku", "SKU-" + unique());
		body.put("name", "Válvula de ensayo de permisos");
		body.put("summary", "Producto de prueba para recorrer operaciones del personal.");
		body.put("family", "ventosa");
		body.put("joint", "bridada");
		body.put("dnMinMm", 50);
		body.put("dnMaxMm", 200);
		body.put("pressureRatings", List.of(10, 16));
		body.put("standards", List.of("EN 1074-4"));
		body.put("specs", List.of(Map.of("label", "Material", "value", "Hierro dúctil")));
		body.put("images", List.of(Map.of("url", "/media/catalog/ensayo.webp", "alt", "Válvula de ensayo", "illustrative", true)));
		body.put("sectors", List.of("agricultura"));
		body.put("variants", List.of(Map.of("label", "DN80 · PN16", "dnMm", 80, "pn", 16)));
		body.put("sourceReference", "Ficha técnica del fabricante, rev. 3");
		body.put("dataVerified", true);
		return body;
	}

	public static Map<String, Object> document(String slug) {
		Map<String, Object> file = new HashMap<>();
		file.put("url", "/docs/" + slug + ".pdf");
		file.put("mediaType", "application/pdf");
		file.put("sizeBytes", 12345);
		file.put("sha256", "0a2c86f4c5234cbd538a50bf9836c62d95de283a3de94f459082953751d99ee9");
		Map<String, Object> body = new HashMap<>();
		body.put("slug", slug);
		body.put("code", "DOC-" + unique());
		body.put("title", "Manual de ensayo " + slug);
		body.put("description", "Documento de prueba de la biblioteca técnica.");
		body.put("type", "MANUAL");
		body.put("families", List.of("compuerta"));
		body.put("availability", "DOWNLOAD");
		body.put("file", file);
		body.put("revision", "A");
		body.put("language", "es");
		body.put("productSlugs", List.of("vcp-f4-01"));
		return body;
	}

	public static Map<String, Object> claim() {
		Map<String, Object> body = new HashMap<>();
		body.put("kind", "CLAIM");
		body.put("consumerName", "Ana Pérez Rojas");
		body.put("documentType", "DNI");
		body.put("documentNumber", "45678912");
		body.put("email", "ana.perez@correo.pe");
		body.put("phone", "987654321");
		body.put("address", "Av. Los Próceres 456, Lima");
		body.put("minor", false);
		body.put("goodType", "PRODUCT");
		body.put("goodDescription", "Válvula de compuerta DN150");
		body.put("claimedAmount", 1500.50);
		body.put("currency", "PEN");
		body.put("incidentDate", LocalDate.now(PeruTime.ZONE).minusDays(3).toString());
		body.put("facts", "La válvula llegó con la brida dañada y no cierra herméticamente.");
		body.put("consumerRequest", "Cambio de la válvula por una nueva.");
		body.put("responseChannel", "EMAIL");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	public static Map<String, Object> inquiry(String email) {
		Map<String, Object> body = new HashMap<>();
		body.put("reason", "TECHNICAL");
		body.put("fullName", "María Quispe");
		body.put("companyName", "Agua Limpia SAC");
		body.put("email", email);
		body.put("phone", "987654321");
		body.put("message", "Necesito la ficha técnica de la válvula de compuerta DN200.");
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	public static Map<String, Object> quote() {
		Map<String, Object> item = new HashMap<>();
		item.put("productSlug", "vcp-f4-01");
		item.put("quantity", 12);
		item.put("unit", "und");
		Map<String, Object> body = new HashMap<>();
		body.put("origin", "PRODUCT_FORM");
		body.put("contact", Map.of("companyName", "Constructora Andina SAC", "ruc", "20131312955", "contactName", "Luis Torres",
				"email", "compras@andina.pe", "phone", "+51 987 654 321"));
		body.put("project", Map.of("projectType", "Saneamiento", "deliveryDepartment", "Lima", "message", "Obra de agua potable en Ate."));
		body.put("items", List.of(item));
		body.put("privacyNoticeVersion", "2026-10");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	public static Map<String, Object> rights(String email) {
		Map<String, Object> body = new HashMap<>();
		body.put("type", "ACCESS");
		body.put("fullName", "Jorge Ramos");
		body.put("email", email);
		body.put("details", "Solicito acceso a los datos asociados a mi correo.");
		body.put("acceptPrivacyNotice", true);
		return body;
	}

	public static Map<String, Object> invitation(String email, String role) {
		return Map.of("email", email, "fullName", "Personal de Prueba", "roles", List.of(role));
	}

	public static Map<String, Object> legalVersion(String version) {
		Map<String, Object> body = new HashMap<>();
		body.put("version", version);
		body.put("title", "Términos de ensayo");
		body.put("contentUrl", "/terminos-b2b");
		body.put("body", "Texto completo de los términos comerciales.");
		body.put("effectiveFrom", Instant.parse("2031-01-01T05:00:00Z").toString());
		return body;
	}

}

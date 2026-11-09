package com.fundigsac.support;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/** El comprobador de contrato detecta de verdad lo que promete: prueba con un contrato mínimo escrito a mano. */
class OpenApiContractTests {

	private static final String DOCUMENT = """
			{"paths":{"/items/{id}":{"get":{"responses":{
			  "200":{"content":{"application/json":{"schema":{"$ref":"#/components/schemas/Item"}}}},
			  "404":{"content":{"application/problem+json":{"schema":{"$ref":"#/components/schemas/Problem"}}}}}}},
			  "/items/special":{"get":{"responses":{"204":{}}}}},
			 "components":{"schemas":{
			  "Item":{"type":"object","required":["id"],"properties":{
			    "id":{"type":"string","format":"uuid"},"state":{"type":"string","enum":["A","B"]},
			    "tags":{"type":"array","items":{"type":"string"}},"note":{"type":"string"},"at":{"type":"string","format":"date-time"}}},
			  "Problem":{"type":"object","required":["code"],"properties":{"code":{"type":"string"}}}}}}""";

	private static final String ID = "01a1c1de-fa83-7e65-923c-ed07b93e7b57";

	private final JsonMapper json = JsonMapper.builder().build();

	private final OpenApiContract contract = new OpenApiContract(this.json.readTree(DOCUMENT), this.json);

	private List<String> check(int status, String contentType, String body) {
		return this.contract.violations("GET", "/items/" + ID, status, contentType, body.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void aDocumentedResponseFollowingItsSchemaPasses() {
		assertThat(check(200, "application/json", "{\"id\":\"" + ID + "\",\"state\":\"A\",\"tags\":[\"x\"],\"note\":null}")).isEmpty();
		assertThat(check(404, "application/problem+json", "{\"code\":\"NOT_FOUND\"}")).isEmpty();
	}

	@Test
	void anUndocumentedStatusIsReported() {
		assertThat(check(500, "application/problem+json", "{\"code\":\"X\"}")).singleElement().asString().contains("código no documentado");
	}

	@Test
	void anUndocumentedContentTypeIsReported() {
		assertThat(check(404, "application/json", "{\"code\":\"NOT_FOUND\"}")).singleElement().asString().contains("tipo de contenido");
	}

	@Test
	void aMissingRequiredFieldIsReported() {
		assertThat(check(200, "application/json", "{\"state\":\"A\"}")).singleElement().asString().contains("obligatorio");
	}

	@Test
	void anUndocumentedFieldIsReported() {
		assertThat(check(200, "application/json", "{\"id\":\"" + ID + "\",\"secreto\":true}")).singleElement().asString().contains("no documentado");
	}

	@Test
	void aWrongTypeFormatOrEnumValueIsReported() {
		assertThat(check(200, "application/json", "{\"id\":\"no-es-uuid\"}")).singleElement().asString().contains("formato uuid");
		assertThat(check(200, "application/json", "{\"id\":\"" + ID + "\",\"state\":\"Z\"}")).singleElement().asString().contains("enum");
		assertThat(check(200, "application/json", "{\"id\":\"" + ID + "\",\"tags\":[1]}")).singleElement().asString().contains("tags[0]");
		assertThat(check(200, "application/json", "{\"id\":\"" + ID + "\",\"at\":\"ayer\"}")).singleElement().asString().contains("formato date-time");
	}

	@Test
	void aLiteralRouteWinsOverOneWithAVariable() {
		assertThat(this.contract.violations("GET", "/items/special", 204, null, new byte[0])).isEmpty();
		assertThat(this.contract.operationCount()).isEqualTo(2);
	}

	@Test
	void aRouteOutsideTheContractIsNotChecked() {
		JsonNode document = this.json.readTree(DOCUMENT);
		assertThat(new OpenApiContract(document, this.json).violations("GET", "/otra", 500, null, new byte[0])).isEmpty();
	}

}

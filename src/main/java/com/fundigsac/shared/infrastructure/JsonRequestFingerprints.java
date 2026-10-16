package com.fundigsac.shared.infrastructure;

import org.springframework.stereotype.Component;

import com.fundigsac.shared.application.RequestFingerprints;
import com.fundigsac.shared.domain.SecureTokens;
import tools.jackson.databind.json.JsonMapper;

/** SHA-256 del JSON del comando (los records se serializan siempre en el mismo orden de campos). */
@Component
class JsonRequestFingerprints implements RequestFingerprints {

	private final JsonMapper json;

	JsonRequestFingerprints(JsonMapper json) {
		this.json = json;
	}

	@Override
	public String of(Object payload) {
		return SecureTokens.sha256(payload.getClass().getName() + ":" + this.json.writeValueAsString(payload));
	}

}

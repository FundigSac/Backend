package com.fundigsac.shared.domain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectsTests {

	@ParameterizedTest
	@ValueSource(strings = { "20131312955", "20100047218", "10467793549" })
	void acceptsRucWithValidCheckDigit(String ruc) {
		assertThat(new Ruc(ruc).value()).isEqualTo(ruc);
	}

	@ParameterizedTest
	@ValueSource(strings = { "20131312956", "30131312955", "2013131295", "abcdefghijk" })
	void rejectsInvalidRuc(String ruc) {
		assertThatThrownBy(() -> new Ruc(ruc)).isInstanceOf(ValidationException.class);
	}

	@Test
	void emailIsNormalizedAndMasked() {
		Email email = new Email("  Compras@Empresa.PE ");
		assertThat(email.value()).isEqualTo("compras@empresa.pe");
		assertThat(email.masked()).isEqualTo("c***@empresa.pe");
		assertThat(email.toString()).doesNotContain("compras");
	}

	@ParameterizedTest
	@ValueSource(strings = { "sin-arroba", "a@b", "@empresa.pe", "a b@empresa.pe", "x@empresa.p" })
	void rejectsInvalidEmail(String value) {
		assertThatThrownBy(() -> new Email(value)).isInstanceOf(ValidationException.class);
	}

	@Test
	void phoneKeepsOnlyDigitsAndPlus() {
		assertThat(new PhoneNumber("+51 908 849 664").value()).isEqualTo("+51908849664");
		assertThatThrownBy(() -> new PhoneNumber("12-ab")).isInstanceOf(ValidationException.class);
	}

	@Test
	void identityDocumentValidatesByType() {
		assertThat(new IdentityDocument(IdentityDocument.Type.DNI, "4567 8912").number()).isEqualTo("45678912");
		assertThatThrownBy(() -> new IdentityDocument(IdentityDocument.Type.DNI, "1234")).isInstanceOf(ValidationException.class);
		assertThat(new IdentityDocument(IdentityDocument.Type.DNI, "45678912").masked()).isEqualTo("DNI ****8912");
	}

	@Test
	void folioFormatsAndParses() {
		Folio folio = new Folio("COT", 2026, 42);
		assertThat(folio.value()).isEqualTo("COT-2026-000042");
		assertThat(Folio.parse("REC-2026-000001")).isEqualTo(new Folio("REC", 2026, 1));
		assertThatThrownBy(() -> Folio.parse("COT-26-1")).isInstanceOf(ValidationException.class);
	}

	@Test
	void moneyRejectsNegativeOrTooPrecise() {
		assertThat(new Money(new BigDecimal("1500.5"), Money.Currency.PEN).amount()).isEqualByComparingTo("1500.50");
		assertThatThrownBy(() -> new Money(new BigDecimal("-1"), Money.Currency.PEN)).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> new Money(new BigDecimal("1.001"), Money.Currency.USD)).isInstanceOf(ValidationException.class);
	}

	@Test
	void guardStripsControlCharactersAndChecksLength() {
		assertThat(Guard.text("f", "  Hola\u0000   mundo ", 2, 20)).isEqualTo("Hola mundo");
		assertThat(Guard.multiline("f", "línea 1\r\n\r\n\r\n\r\nlínea 2", 1, 50)).isEqualTo("línea 1\n\nlínea 2");
		assertThatThrownBy(() -> Guard.text("f", "x", 2, 20)).isInstanceOf(ValidationException.class);
	}

	@Test
	void uuidV7IsTimeOrderedAndUnique() {
		Set<UUID> ids = new HashSet<>();
		UUID previous = Ids.newId();
		for (int i = 0; i < 1000; i++) {
			UUID id = Ids.newId();
			assertThat(id.version()).isEqualTo(7);
			assertThat(id.variant()).isEqualTo(2);
			assertThat(id.getMostSignificantBits() >>> 16).isGreaterThanOrEqualTo(previous.getMostSignificantBits() >>> 16);
			ids.add(id);
			previous = id;
		}
		assertThat(ids).hasSize(1000);
	}

	@Test
	void slugFoldRemovesAccents() {
		assertThat(Slug.fold("Válvula  de COMPUERTA Ñ")).isEqualTo("valvula de compuerta n");
		assertThatThrownBy(() -> new Slug("Con Espacios")).isInstanceOf(ValidationException.class);
	}

}

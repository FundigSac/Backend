package com.fundigsac.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.fundigsac.catalog.domain.DnRange;
import com.fundigsac.catalog.domain.Sku;
import com.fundigsac.catalog.domain.TechnicalDocument;
import com.fundigsac.catalog.domain.Variant;
import com.fundigsac.complaint.domain.ResponseDeadlinePolicy;
import com.fundigsac.identity.domain.LoginPolicy;
import com.fundigsac.identity.domain.OneTimeToken;
import com.fundigsac.identity.domain.PasswordPolicy;
import com.fundigsac.identity.domain.SignupProfile;
import com.fundigsac.privacy.domain.ConsentRecord;
import com.fundigsac.quotation.domain.ProjectDetails;
import com.fundigsac.quotation.domain.QuoteItem;
import com.fundigsac.quotation.domain.QuoteStatus;
import com.fundigsac.quotation.domain.QuoteUnit;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.IdentityDocument;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.ValidationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Límites exactos de los valores de dominio: justo dentro se aceptan, justo fuera se rechazan. */
class ValueInvariantsTests {

	private static final Instant NOW = Instant.parse("2026-11-09T15:00:00Z");

	private static final int[] RUC_WEIGHTS = { 5, 4, 3, 2, 7, 6, 5, 4, 3, 2 };

	private static void rejected(org.assertj.core.api.ThrowableAssert.ThrowingCallable callable) {
		assertThatThrownBy(callable).isInstanceOf(ValidationException.class);
	}

	/** RUC válido con el prefijo dado y cuyo cálculo deja el residuo pedido. */
	private static String rucWithResidue(String prefix, int residue) {
		for (int body = 0; body < 100_000_000; body++) {
			String digits = prefix + "%08d".formatted(body);
			int sum = 0;
			for (int i = 0; i < 10; i++) {
				sum += (digits.charAt(i) - '0') * RUC_WEIGHTS[i];
			}
			if (sum % 11 == residue) {
				int check = 11 - residue;
				return digits + (check == 10 ? 0 : check == 11 ? 1 : check);
			}
		}
		throw new IllegalStateException("Sin RUC para el residuo pedido");
	}

	@ParameterizedTest
	@ValueSource(strings = { "10", "15", "16", "17", "20" })
	void acceptsEveryTaxpayerPrefix(String prefix) {
		assertThat(new Ruc(rucWithResidue(prefix, 3)).value()).startsWith(prefix);
	}

	@Test
	void theCheckDigitWrapsWhenTheFormulaGivesTenOrEleven() {
		assertThat(new Ruc(rucWithResidue("20", 1)).value()).endsWith("0");
		assertThat(new Ruc(rucWithResidue("20", 0)).value()).endsWith("1");
	}

	@Test
	void rucFactoriesReportTheFieldAndTreatBlankAsAbsent() {
		assertThat(Ruc.optional("ruc", null)).isNull();
		assertThat(Ruc.optional("ruc", "  ")).isNull();
		assertThat(Ruc.optional("ruc", "20131312955")).isNotNull();
		rejected(() -> Ruc.of("ruc", null));
		rejected(() -> Ruc.of("ruc", "  "));
		rejected(() -> Ruc.of("ruc", "20131312956"));
		rejected(() -> new Ruc(null));
		assertThat(new Ruc("20131312955")).hasToString("20131312955");
	}

	@Test
	void phoneFactoriesAndLimits() {
		assertThat(PhoneNumber.optional("phone", null)).isNull();
		assertThat(PhoneNumber.optional("phone", " ")).isNull();
		assertThat(PhoneNumber.of("phone", "987654321").value()).isEqualTo("987654321");
		rejected(() -> PhoneNumber.of("phone", null));
		rejected(() -> PhoneNumber.of("phone", " "));
		rejected(() -> PhoneNumber.of("phone", "abc"));
		rejected(() -> new PhoneNumber(null));
		rejected(() -> new PhoneNumber("1234567890123456"));
		assertThat(new PhoneNumber("987654321")).hasToString("***321");
	}

	@Test
	void slugLimits() {
		assertThat(Slug.of("slug", " valvula-a ").value()).isEqualTo("valvula-a");
		assertThat(new Slug("a".repeat(120)).value()).hasSize(120);
		rejected(() -> new Slug("a".repeat(121)));
		rejected(() -> new Slug(null));
		rejected(() -> Slug.of("slug", null));
		rejected(() -> Slug.of("slug", "Con Espacios"));
		assertThat(Slug.fold(null)).isEmpty();
		assertThat(new Slug("valvula")).hasToString("valvula");
	}

	@ParameterizedTest
	@CsvSource({ "ABC,2000,1,true", "ABC,9999,999999,true", "AB,2026,1,false", "ABC,1999,1,false", "ABC,10000,1,false", "ABC,2026,0,false",
			"ABC,2026,1000000,false" })
	void folioLimits(String prefix, int year, int sequence, boolean valid) {
		if (valid) {
			assertThatCode(() -> new Folio(prefix, year, sequence)).doesNotThrowAnyException();
		}
		else {
			assertThatThrownBy(() -> new Folio(prefix, year, sequence)).isInstanceOf(IllegalArgumentException.class);
		}
	}

	@Test
	void folioNeedsAPrefixAndAParsableValue() {
		assertThatThrownBy(() -> new Folio(null, 2026, 1)).isInstanceOf(IllegalArgumentException.class);
		rejected(() -> Folio.parse(null));
		assertThat(Folio.parse(" COT-2026-000001 ").sequence()).isEqualTo(1);
		assertThat(new Folio("COT", 2026, 1)).hasToString("COT-2026-000001");
	}

	@Test
	void identityDocumentByType() {
		rejected(() -> IdentityDocument.of(IdentityDocument.Type.DNI, null));
		rejected(() -> IdentityDocument.of(IdentityDocument.Type.DNI, " "));
		rejected(() -> IdentityDocument.of(null, "45678912"));
		rejected(() -> new IdentityDocument(IdentityDocument.Type.DNI, null));
		assertThat(new IdentityDocument(IdentityDocument.Type.RUC, "20131312955").number()).isEqualTo("20131312955");
		rejected(() -> new IdentityDocument(IdentityDocument.Type.RUC, "20131312956"));
		assertThat(IdentityDocument.of(IdentityDocument.Type.CE, "ab123456c").number()).isEqualTo("AB123456C");
	}

	@Test
	void emailLimits() {
		rejected(() -> Email.of("email", null));
		rejected(() -> Email.of("email", " "));
		assertThat(Email.of("email", "uno@empresa.pe").value()).isEqualTo("uno@empresa.pe");
		rejected(() -> new Email("a".repeat(65) + "@empresa.pe"));
		rejected(() -> new Email("a".repeat(250) + "@empresa.pe"));
	}

	@Test
	void guardRules() {
		rejected(() -> Guard.text("f", null, 1, 5));
		rejected(() -> Guard.text("f", "   ", 1, 5));
		rejected(() -> Guard.text("f", "demasiado largo", 1, 5));
		rejected(() -> Guard.multiline("f", " \n ", 1, 5));
		assertThat(Guard.optionalText("f", " ", 5)).isNull();
		assertThat(Guard.optionalMultiline("f", null, 5)).isNull();
		rejected(() -> Guard.notNull("f", null));
		assertThat(Guard.notNull("f", "valor")).isEqualTo("valor");
		rejected(() -> Guard.isTrue(false, "f", "CODE", "mensaje"));
		assertThatCode(() -> Guard.isTrue(true, "f", "CODE", "mensaje")).doesNotThrowAnyException();
		rejected(() -> Guard.maxSize("f", List.of(1, 2), 1));
		assertThat(Guard.maxSize("f", List.of(1), 1)).hasSize(1);
	}

	@Test
	void dnRangeLimitsAndContainment() {
		assertThat(new DnRange(1, 4000).contains(1)).isTrue();
		assertThat(new DnRange(1, 4000).contains(4000)).isTrue();
		assertThat(new DnRange(50, 100).contains(49)).isFalse();
		assertThat(new DnRange(50, 100).contains(101)).isFalse();
		rejected(() -> new DnRange(0, 100));
		rejected(() -> new DnRange(1, 4001));
		rejected(() -> new DnRange(100, 50));
		assertThat(DnRange.of(null, null)).isNull();
		rejected(() -> DnRange.of(1, null));
		rejected(() -> DnRange.of(null, 1));
		assertThat(DnRange.of(10, 20)).isEqualTo(new DnRange(10, 20));
	}

	@Test
	void variantLimits() {
		UUID id = UUID.randomUUID();
		assertThat(new Variant(id, null, "DN80", null, null).dnMm()).isNull();
		assertThat(new Variant(id, null, "DN80", 1, 1).pn()).isEqualTo(1);
		assertThat(new Variant(id, null, "DN80", 4000, 100).dnMm()).isEqualTo(4000);
		rejected(() -> new Variant(id, null, "DN80", 0, null));
		rejected(() -> new Variant(id, null, "DN80", 4001, null));
		rejected(() -> new Variant(id, null, "DN80", null, 0));
		rejected(() -> new Variant(id, null, "DN80", null, 101));
	}

	@Test
	void skuNormalization() {
		assertThat(new Sku(" vcp-01 ").value()).isEqualTo("VCP-01");
		assertThat(Sku.optional(null)).isNull();
		assertThat(Sku.optional(" ")).isNull();
		assertThat(Sku.optional("A1")).isNotNull();
		rejected(() -> new Sku(null));
		rejected(() -> new Sku("-X"));
		assertThat(new Sku("A1")).hasToString("A1");
	}

	@Test
	void fileReferenceLimits() {
		String sha = "0a2c86f4c5234cbd538a50bf9836c62d95de283a3de94f459082953751d99ee9";
		assertThat(new TechnicalDocument.FileReference("/docs/a.pdf", "application/pdf", 1, sha).sizeBytes()).isEqualTo(1);
		assertThat(new TechnicalDocument.FileReference("https://cdn.fundigsac.pe/a.pdf", "application/pdf", 500L * 1024 * 1024, sha)).isNotNull();
		rejected(() -> new TechnicalDocument.FileReference("ftp://x/a", "application/pdf", 1, sha));
		rejected(() -> new TechnicalDocument.FileReference("/docs/a.pdf", "PDF", 1, sha));
		rejected(() -> new TechnicalDocument.FileReference("/docs/a.pdf", "application/pdf", 0, sha));
		rejected(() -> new TechnicalDocument.FileReference("/docs/a.pdf", "application/pdf", 500L * 1024 * 1024 + 1, sha));
		rejected(() -> new TechnicalDocument.FileReference("/docs/a.pdf", "application/pdf", 1, "corto"));
	}

	@Test
	void quoteItemLimits() {
		BigDecimal one = BigDecimal.ONE;
		assertThat(new QuoteItem(50, null, null, "Codo", null, null, one, QuoteUnit.UNIT, null, null).addons()).isEmpty();
		assertThatThrownBy(() -> new QuoteItem(0, null, null, "Codo", null, null, one, QuoteUnit.UNIT, List.of(), null))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new QuoteItem(51, null, null, "Codo", null, null, one, QuoteUnit.UNIT, List.of(), null))
			.isInstanceOf(IllegalArgumentException.class);
		rejected(() -> new QuoteItem(1, null, null, "Codo", null, null, new BigDecimal("1000000.001"), QuoteUnit.UNIT, List.of(), null));
		assertThat(new QuoteItem(1, null, null, "Codo", null, null, new BigDecimal("1000000"), QuoteUnit.UNIT, List.of("a", "a"), null).addons()).hasSize(1);
	}

	@Test
	void deliveryDateWindow() {
		LocalDate today = LocalDate.of(2026, 11, 9);
		assertThatCode(() -> project(null).checkDeliveryDate(today)).doesNotThrowAnyException();
		assertThatCode(() -> project(today).checkDeliveryDate(today)).doesNotThrowAnyException();
		assertThatCode(() -> project(today.plusYears(3)).checkDeliveryDate(today)).doesNotThrowAnyException();
		rejected(() -> project(today.minusDays(1)).checkDeliveryDate(today));
		rejected(() -> project(today.plusYears(3).plusDays(1)).checkDeliveryDate(today));
	}

	private static ProjectDetails project(LocalDate date) {
		return new ProjectDetails(null, null, null, date, null);
	}

	@Test
	void quoteStatusFinalStates() {
		assertThat(QuoteStatus.CLOSED.isFinal()).isTrue();
		assertThat(QuoteStatus.CANCELLED.isFinal()).isTrue();
		assertThat(QuoteStatus.RECEIVED.isFinal()).isFalse();
		assertThat(QuoteStatus.RESPONDED.next()).containsExactlyInAnyOrder(QuoteStatus.CLOSED, QuoteStatus.IN_REVIEW);
		assertThat(QuoteStatus.CLOSED.next()).isEmpty();
		assertThat(QuoteStatus.ASSIGNED.next()).contains(QuoteStatus.IN_REVIEW);
		assertThat(QuoteStatus.IN_REVIEW.next()).contains(QuoteStatus.RESPONDED);
	}

	@Test
	void loginPolicyLimits() {
		assertThat(new LoginPolicy(1, Duration.ofSeconds(1)).maxFailedAttempts()).isEqualTo(1);
		assertThatThrownBy(() -> new LoginPolicy(0, Duration.ofMinutes(1))).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new LoginPolicy(1, Duration.ZERO)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new LoginPolicy(1, Duration.ofSeconds(-1))).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void oneTimeTokenUsability() {
		UUID id = UUID.randomUUID();
		OneTimeToken valid = new OneTimeToken(id, id, OneTimeToken.Purpose.PASSWORD_RESET, "h", NOW, NOW.plusSeconds(60), null);
		assertThat(valid.isUsable(OneTimeToken.Purpose.PASSWORD_RESET, NOW)).isTrue();
		assertThat(valid.isUsable(OneTimeToken.Purpose.STAFF_INVITATION, NOW)).isFalse();
		assertThat(valid.isUsable(OneTimeToken.Purpose.PASSWORD_RESET, NOW.plusSeconds(60))).isFalse();
		assertThat(new OneTimeToken(id, id, OneTimeToken.Purpose.PASSWORD_RESET, "h", NOW, NOW.plusSeconds(60), NOW).isUsable(OneTimeToken.Purpose.PASSWORD_RESET, NOW)).isFalse();
	}

	@Test
	void signupSourceIsAClosedList() {
		assertThat(SignupProfile.source(null)).isNull();
		assertThat(SignupProfile.source(" ")).isNull();
		assertThat(SignupProfile.source(" carrito ")).isEqualTo("carrito");
		rejected(() -> SignupProfile.source("https://otro-sitio"));
		assertThat(new SignupProfile(SignupProfile.Method.EMAIL, null, NOW, null).newsletter()).isTrue();
		assertThat(SignupProfile.email().newsletter()).isFalse();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void passwordIsRequired(String password) {
		rejected(() -> PasswordPolicy.check("password", password, null));
	}

	@Test
	void passwordLengthAndPredictability() {
		assertThatCode(() -> PasswordPolicy.check("password", "a-b-c-d-e-f-g-h", null)).doesNotThrowAnyException();
		rejected(() -> PasswordPolicy.check("password", "corta-clave", null));
		rejected(() -> PasswordPolicy.check("password", "x".repeat(129), null));
		rejected(() -> PasswordPolicy.check("password", "abababababab", null));
		rejected(() -> PasswordPolicy.check("password", "mi-correo-luis-2026", "luis@empresa.pe"));
		assertThatCode(() -> PasswordPolicy.check("password", "mi-correo-ab-2026", "ab@empresa.pe")).doesNotThrowAnyException();
	}

	@Test
	void consentNeedsExactCategoriesAndAValidVersion() {
		UUID id = UUID.randomUUID();
		Map<String, Boolean> valid = Map.of("necessary", true, "thirdParty", false);
		assertThat(new ConsentRecord(id, id, valid, "2026-10", ConsentRecord.Source.BANNER, NOW).policyVersion()).isEqualTo("2026-10");
		rejected(() -> new ConsentRecord(id, id, null, "2026-10", ConsentRecord.Source.BANNER, NOW));
		rejected(() -> new ConsentRecord(id, id, Map.of("necessary", true), "2026-10", ConsentRecord.Source.BANNER, NOW));
		rejected(() -> new ConsentRecord(id, id, Map.of("necessary", false, "thirdParty", false), "2026-10", ConsentRecord.Source.BANNER, NOW));
		rejected(() -> new ConsentRecord(id, id, valid, null, ConsentRecord.Source.BANNER, NOW));
		rejected(() -> new ConsentRecord(id, id, valid, "versión inválida", ConsentRecord.Source.BANNER, NOW));
	}

	@Test
	void deadlineLimits() {
		assertThat(new ResponseDeadlinePolicy(1, Set.of()).businessDays()).isEqualTo(1);
		assertThat(new ResponseDeadlinePolicy(60, Set.of()).businessDays()).isEqualTo(60);
		assertThatThrownBy(() -> new ResponseDeadlinePolicy(0, Set.of())).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new ResponseDeadlinePolicy(61, Set.of())).isInstanceOf(IllegalArgumentException.class);
	}

}

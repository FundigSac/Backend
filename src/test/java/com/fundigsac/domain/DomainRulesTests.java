package com.fundigsac.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fundigsac.catalog.domain.Product;
import com.fundigsac.catalog.domain.ProductDetails;
import com.fundigsac.catalog.domain.ProductImage;
import com.fundigsac.catalog.domain.ProductStatus;
import com.fundigsac.complaint.domain.Complaint;
import com.fundigsac.complaint.domain.ResponseDeadlinePolicy;
import com.fundigsac.identity.domain.AccountStatus;
import com.fundigsac.identity.domain.LoginPolicy;
import com.fundigsac.identity.domain.PasswordPolicy;
import com.fundigsac.identity.domain.Role;
import com.fundigsac.identity.domain.UserAccount;
import com.fundigsac.quotation.domain.ProjectDetails;
import com.fundigsac.quotation.domain.QuoteItem;
import com.fundigsac.quotation.domain.QuoteOrigin;
import com.fundigsac.quotation.domain.QuoteRequest;
import com.fundigsac.quotation.domain.QuoteStatus;
import com.fundigsac.quotation.domain.QuoteUnit;
import com.fundigsac.quotation.domain.Requester;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Folio;
import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.domain.IdentityDocument;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Reglas de negocio de los agregados, sin Spring ni base de datos. */
class DomainRulesTests {

	private static final Instant NOW = Instant.parse("2026-10-16T15:00:00Z");

	// ----- Libro de Reclamaciones -----

	@Test
	void deadlineCountsBusinessDaysFromTheNextDay() {
		ResponseDeadlinePolicy policy = new ResponseDeadlinePolicy(15, Set.of());
		// Viernes 16/10/2026 → 15 días hábiles: termina el viernes 06/11/2026.
		assertThat(policy.dueDate(LocalDate.of(2026, 10, 16))).isEqualTo(LocalDate.of(2026, 11, 6));
		ResponseDeadlinePolicy withHoliday = new ResponseDeadlinePolicy(15, Set.of(LocalDate.of(2026, 11, 2)));
		assertThat(withHoliday.dueDate(LocalDate.of(2026, 10, 16))).isEqualTo(LocalDate.of(2026, 11, 9));
	}

	@Test
	void minorNeedsGuardianAndClaimNeedsGoodType() {
		var consumer = new Complaint.Consumer("Ana Pérez", new IdentityDocument(IdentityDocument.Type.DNI, "45678912"),
				new Email("ana@correo.pe"), new PhoneNumber("987654321"), "Av. Principal 123, Lima");
		var details = new Complaint.Details(null, "Válvula de compuerta DN150", null, null, LocalDate.of(2026, 10, 10), null,
				"La válvula llegó con la brida dañada y no cierra bien.", "Cambio de la pieza");
		ResponseDeadlinePolicy policy = new ResponseDeadlinePolicy(15, Set.of());
		assertThatThrownBy(() -> Complaint.file(Ids.newId(), new Folio("REC", 2026, 1), Complaint.Kind.GRIEVANCE, consumer, true,
				null, details, Complaint.ResponseChannel.EMAIL, "2026-10", policy, NOW))
			.isInstanceOf(ValidationException.class)
			.hasMessageContaining("Revise");
		assertThatThrownBy(() -> Complaint.file(Ids.newId(), new Folio("REC", 2026, 1), Complaint.Kind.CLAIM, consumer, false,
				null, details, Complaint.ResponseChannel.EMAIL, "2026-10", policy, NOW))
			.isInstanceOf(ValidationException.class);
	}

	@Test
	void complaintIsAnsweredOnceAndClosedAfterwards() {
		var consumer = new Complaint.Consumer("Ana Pérez", new IdentityDocument(IdentityDocument.Type.DNI, "45678912"),
				new Email("ana@correo.pe"), new PhoneNumber("987654321"), "Av. Principal 123, Lima");
		var details = new Complaint.Details(Complaint.GoodType.PRODUCT, "Válvula de compuerta DN150", null, null,
				LocalDate.of(2026, 10, 10), null, "La válvula llegó con la brida dañada y no cierra bien.", "Cambio de la pieza");
		Complaint complaint = Complaint.file(Ids.newId(), new Folio("REC", 2026, 1), Complaint.Kind.CLAIM, consumer, false,
				null, details, Complaint.ResponseChannel.EMAIL, "2026-10", new ResponseDeadlinePolicy(15, Set.of()), NOW);
		assertThat(complaint.responseDueOn()).isEqualTo(LocalDate.of(2026, 11, 6));
		assertThatThrownBy(() -> complaint.close(UUID.randomUUID(), NOW)).isInstanceOf(RuleViolationException.class);
		complaint.answer("Reemplazaremos la válvula sin costo en un plazo de 5 días.", UUID.randomUUID(), NOW);
		assertThatThrownBy(() -> complaint.answer("Otra respuesta distinta a la primera emitida.", UUID.randomUUID(), NOW))
			.isInstanceOf(ConflictException.class);
		complaint.close(UUID.randomUUID(), NOW);
		assertThat(complaint.status()).isEqualTo(Complaint.Status.CLOSED);
	}

	// ----- Cotizaciones -----

	private static QuoteRequest quote(List<QuoteItem> items, String message) {
		return QuoteRequest.submit(Ids.newId(), new Folio("COT", 2026, 7), QuoteOrigin.GENERAL_FORM,
				new Requester("Constructora Andina SAC", new Ruc("20131312955"), "Luis Torres", null,
						new Email("compras@andina.pe"), new PhoneNumber("987654321")),
				new ProjectDetails("Saneamiento", "Lima", null, null, message), items, "2026-10", null, NOW);
	}

	@Test
	void quoteNeedsProductsOrADescribedNeed() {
		assertThatThrownBy(() -> quote(List.of(), "corto")).isInstanceOf(RuleViolationException.class);
		assertThat(quote(List.of(), "Necesitamos válvulas DN200 para una línea de impulsión.").status())
			.isEqualTo(QuoteStatus.RECEIVED);
	}

	@Test
	void quoteFollowsAllowedTransitionsOnly() {
		QuoteItem item = new QuoteItem(1, null, null, "Tubería DN300", null, null, new BigDecimal("120.5"), QuoteUnit.METER,
				List.of(), null);
		QuoteRequest q = quote(List.of(item), null);
		UUID staff = UUID.randomUUID();
		assertThatThrownBy(() -> q.changeStatus(QuoteStatus.CLOSED, null, staff, NOW)).isInstanceOf(ConflictException.class);
		q.assign(staff, staff, NOW);
		assertThat(q.status()).isEqualTo(QuoteStatus.ASSIGNED);
		q.changeStatus(QuoteStatus.IN_REVIEW, null, staff, NOW);
		assertThatThrownBy(() -> q.changeStatus(QuoteStatus.CANCELLED, null, staff, NOW))
			.isInstanceOf(RuleViolationException.class);
		q.changeStatus(QuoteStatus.RESPONDED, "Propuesta enviada", staff, NOW);
		q.changeStatus(QuoteStatus.CLOSED, null, staff, NOW);
		assertThat(q.drainNewHistory()).hasSize(5);
	}

	@Test
	void quoteItemRejectsInvalidQuantities() {
		assertThatThrownBy(() -> new QuoteItem(1, null, null, "Codo", null, null, BigDecimal.ZERO, QuoteUnit.UNIT, List.of(), null))
			.isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> new QuoteItem(1, null, null, "Codo", null, null, new BigDecimal("1.0001"), QuoteUnit.UNIT,
				List.of(), null))
			.isInstanceOf(ValidationException.class);
	}

	// ----- Catálogo -----

	@Test
	void productPublicationRequiresAnotherReviewer() {
		ProductDetails details = new ProductDetails(UUID.randomUUID(), new Slug("valvula-prueba"), null, "Válvula de prueba",
				"Resumen suficientemente largo de la ficha.", null, "compuerta", null, null, null, List.of(16), List.of(),
				Set.of(), List.of(), List.of(new ProductImage("/media/x.webp", "Foto de la válvula", false)), Set.of(), null,
				true);
		UUID editor = UUID.randomUUID();
		Product product = Product.draft(UUID.randomUUID(), details, List.of(), editor, NOW);
		product.submitForReview(editor, NOW);
		assertThatThrownBy(() -> product.publish(editor, NOW)).isInstanceOf(ForbiddenException.class);
		product.publish(UUID.randomUUID(), NOW);
		assertThat(product.status()).isEqualTo(ProductStatus.PUBLISHED);
		assertThatThrownBy(() -> product.edit(details, List.of(), NOW)).isInstanceOf(ConflictException.class);
	}

	// ----- Identidad -----

	@Test
	void accountLocksAfterRepeatedFailures() {
		UserAccount account = UserAccount.bootstrapAdministrator(UUID.randomUUID(), new Email("admin@fundigsac.pe"), "Admin",
				"hash", NOW);
		LoginPolicy policy = new LoginPolicy(5, Duration.ofMinutes(15));
		for (int i = 0; i < 4; i++) {
			account.recordFailedLogin(NOW, policy);
		}
		assertThat(account.isLocked(NOW)).isFalse();
		account.recordFailedLogin(NOW, policy);
		assertThat(account.isLocked(NOW)).isTrue();
		assertThat(account.isLocked(NOW.plus(Duration.ofMinutes(16)))).isFalse();
		assertThat(account.pullEvents()).hasSize(1);
	}

	@Test
	void sensitiveChangesBumpTheSecurityStamp() {
		UserAccount account = UserAccount.inviteStaff(UUID.randomUUID(), new Email("ventas@fundigsac.pe"), "Ventas",
				EnumSet.of(Role.SALES), NOW);
		account.acceptInvitation("hash", NOW);
		int stamp = account.securityStamp();
		account.changeRoles(EnumSet.of(Role.READ_ONLY), UUID.randomUUID(), NOW);
		assertThat(account.securityStamp()).isEqualTo(stamp + 1);
		assertThat(account.permissions()).contains(Permission.QUOTE_READ).doesNotContain(Permission.QUOTE_MANAGE);
		account.disable(UUID.randomUUID(), NOW);
		assertThat(account.status()).isEqualTo(AccountStatus.DISABLED);
		assertThatThrownBy(() -> account.changeRoles(EnumSet.of(Role.CUSTOMER), UUID.randomUUID(), NOW))
			.isInstanceOf(ValidationException.class);
	}

	@Test
	void passwordPolicyFollowsNist() {
		PasswordPolicy.check("p", "una frase de paso larga", "x@y.pe");
		assertThatThrownBy(() -> PasswordPolicy.check("p", "corta", null)).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> PasswordPolicy.check("p", "password1234", null)).isInstanceOf(ValidationException.class);
		assertThatThrownBy(() -> PasswordPolicy.check("p", "compras-2026-seguro", "compras@andina.pe"))
			.isInstanceOf(ValidationException.class);
	}

	@Test
	void deletingAnAccountAnonymizesIt() {
		UserAccount account = UserAccount.registerCustomer(UUID.randomUUID(), new Email("cliente@empresa.pe"), "hash",
				"Cliente Uno", "Empresa", new Ruc("20131312955"), new PhoneNumber("987654321"), "2026-10", NOW);
		account.delete(NOW);
		assertThat(account.status()).isEqualTo(AccountStatus.DELETED);
		assertThat(account.email().value()).endsWith("@cuentas-eliminadas.invalid");
		assertThat(account.ruc()).isNull();
		assertThat(account.passwordHash()).isNull();
	}

}

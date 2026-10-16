package com.fundigsac.cart.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.cart.domain.CartRepository;
import com.fundigsac.cart.domain.QuoteCart;
import com.fundigsac.catalog.api.CatalogLookup;
import com.fundigsac.catalog.api.CatalogLookup.ProductSnapshot;
import com.fundigsac.identity.api.CustomerDirectory;
import com.fundigsac.identity.api.CustomerDirectory.CustomerContact;
import com.fundigsac.identity.api.IdentityEvents.AccountDeleted;
import com.fundigsac.quotation.api.QuoteSubmission;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;

/** Carrito del cliente: agregar, cambiar, quitar y enviar a cotizar (crea una solicitud real con folio). */
@Service
public class CartService {

	public record CartView(List<QuoteCart.Item> items, int lineCount, int totalUnits, Instant updatedAt, long version) {

		static CartView of(QuoteCart cart) {
			return new CartView(cart.items(), cart.items().size(),
					cart.items().stream().mapToInt(QuoteCart.Item::quantity).sum(), cart.updatedAt(), cart.version());
		}

	}

	/** Datos para enviar a cotizar; los vacíos se completan con el perfil del cliente. */
	public record Checkout(@Nullable String companyName, @Nullable String ruc, @Nullable String contactName,
			@Nullable String position, @Nullable String phone, @Nullable String projectType,
			@Nullable String deliveryDepartment, @Nullable String deliveryPlace, @Nullable LocalDate requestedDeliveryDate,
			@Nullable String message, String privacyNoticeVersion) {
	}

	private final CartRepository carts;

	private final CatalogLookup catalog;

	private final CustomerDirectory customers;

	private final QuoteSubmission quotes;

	private final CurrentActor actor;

	private final Clock clock;

	CartService(CartRepository carts, CatalogLookup catalog, CustomerDirectory customers, QuoteSubmission quotes,
			CurrentActor actor, Clock clock) {
		this.carts = carts;
		this.catalog = catalog;
		this.customers = customers;
		this.quotes = quotes;
		this.actor = actor;
		this.clock = clock;
	}

	@PreAuthorize(Authz.CUSTOMER)
	@Transactional(readOnly = true)
	public CartView get() {
		return CartView.of(current());
	}

	@PreAuthorize(Authz.CUSTOMER)
	@Transactional
	public CartView add(String productSlug, @Nullable String variantLabel, int quantity, @Nullable String note) {
		ProductSnapshot product = this.catalog.findPublished(productSlug)
			.orElseThrow(() -> new ValidationException("productSlug", "UNKNOWN_PRODUCT", "El producto no existe o no está publicado."));
		if (!product.acceptsVariant(variantLabel)) {
			throw new ValidationException("variantLabel", "UNKNOWN_VARIANT", "La medida no corresponde al producto.");
		}
		QuoteCart cart = current();
		cart.add(product.id(), product.slug(), product.name(), variantLabel, quantity, note, this.clock.instant());
		return save(cart);
	}

	@PreAuthorize(Authz.CUSTOMER)
	@Transactional
	public CartView update(UUID itemId, int quantity, @Nullable String note) {
		QuoteCart cart = current();
		cart.update(itemId, quantity, note, this.clock.instant());
		return save(cart);
	}

	@PreAuthorize(Authz.CUSTOMER)
	@Transactional
	public CartView remove(UUID itemId) {
		QuoteCart cart = current();
		cart.remove(itemId, this.clock.instant());
		return save(cart);
	}

	@PreAuthorize(Authz.CUSTOMER)
	@Transactional
	public CartView clear() {
		QuoteCart cart = current();
		cart.clear(this.clock.instant());
		return save(cart);
	}

	/** Envía el carrito como solicitud de cotización (misma transacción: si falla, el carrito no se vacía). */
	@PreAuthorize(Authz.CUSTOMER)
	@Transactional
	public QuoteSubmission.Receipt submit(Checkout checkout, @Nullable String idempotencyKey) {
		UUID me = this.actor.requireUserId();
		CustomerContact contact = this.customers.findActiveCustomer(me)
			.orElseThrow(() -> new ForbiddenException("ACCOUNT_INACTIVE", "La cuenta no está activa."));
		QuoteCart cart = current();
		if (cart.items().isEmpty()) {
			throw new RuleViolationException("CART_EMPTY", "Agregue productos al carrito antes de solicitar la cotización.");
		}
		List<QuoteSubmission.Line> lines = cart.items()
			.stream()
			.map((i) -> new QuoteSubmission.Line(i.productSlug(), i.productName(), i.variantLabel(),
					BigDecimal.valueOf(i.quantity()), "UNIT", List.of(), i.note()))
			.toList();
		QuoteSubmission.Request request = new QuoteSubmission.Request("CART",
				new QuoteSubmission.Contact(pick(checkout.companyName(), contact.companyName(), "companyName"),
						pick(checkout.ruc(), contact.ruc(), "ruc"), pick(checkout.contactName(), contact.fullName(), "contactName"),
						checkout.position(), contact.email(), pick(checkout.phone(), contact.phone(), "phone")),
				new QuoteSubmission.Project(checkout.projectType(), checkout.deliveryDepartment(), checkout.deliveryPlace(),
						checkout.requestedDeliveryDate(), checkout.message()),
				lines, checkout.privacyNoticeVersion(), me);
		QuoteSubmission.Receipt receipt = this.quotes.submit(request, idempotencyKey);
		cart.clear(this.clock.instant());
		save(cart);
		return receipt;
	}

	/** Al eliminarse la cuenta se borra su carrito (no hay obligación de conservarlo). */
	@ApplicationModuleListener
	public void on(AccountDeleted event) {
		this.carts.delete(event.userId());
	}

	private CartView save(QuoteCart cart) {
		this.carts.save(cart);
		return CartView.of(this.carts.find(cart.ownerId()).orElseThrow());
	}

	private QuoteCart current() {
		UUID me = this.actor.requireUserId();
		return this.carts.find(me).orElseGet(() -> QuoteCart.empty(me, this.clock.instant()));
	}

	private static String pick(@Nullable String provided, @Nullable String fallback, String field) {
		String value = provided != null && !provided.isBlank() ? provided : fallback;
		if (value == null || value.isBlank()) {
			throw new ValidationException(field, "REQUIRED", "Complete este dato (no está en su perfil).");
		}
		return value;
	}

}

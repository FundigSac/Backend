package com.fundigsac.catalog.application;

import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fundigsac.catalog.application.CatalogReadModel.DocumentSearch;
import com.fundigsac.catalog.application.CatalogViews.DocumentView;
import com.fundigsac.catalog.domain.DocumentRepository;
import com.fundigsac.catalog.domain.TechnicalDocument;
import com.fundigsac.shared.application.CurrentActor;
import com.fundigsac.shared.application.DomainEvents;
import com.fundigsac.shared.application.PageQuery;
import com.fundigsac.shared.application.PageResult;
import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.Ids;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.Slug;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.domain.ValidationException;
import com.fundigsac.shared.security.Authz;

/** Gestión de documentos técnicos: registro con checksum, publicación y retiro de versiones obsoletas. */
@Service
public class DocumentAdminService {

	public record FileInput(String url, String mediaType, long sizeBytes, String sha256) {
	}

	public record DocumentDraft(String slug, @Nullable String code, String title, @Nullable String description, String type,
			@Nullable List<String> families, String availability, @Nullable FileInput file, @Nullable String revision,
			@Nullable String language, @Nullable List<String> productSlugs) {
	}

	private final DocumentRepository documents;

	private final CatalogReadModel readModel;

	private final CurrentActor actor;

	private final DomainEvents events;

	private final Clock clock;

	DocumentAdminService(DocumentRepository documents, CatalogReadModel readModel, CurrentActor actor,
			DomainEvents events, Clock clock) {
		this.documents = documents;
		this.readModel = readModel;
		this.actor = actor;
		this.events = events;
		this.clock = clock;
	}

	@PreAuthorize(Authz.CATALOG_READ)
	@Transactional(readOnly = true)
	public PageResult<DocumentView> list(@Nullable String q, PageQuery page) {
		String query = q == null || q.isBlank() ? null : Slug.fold(q);
		return this.readModel.documents(new DocumentSearch(null, null, null, query, false), page);
	}

	@PreAuthorize(Authz.CATALOG_READ)
	@Transactional(readOnly = true)
	public DocumentView get(UUID id) {
		return this.readModel.documentById(id).orElseThrow(DocumentAdminService::notFound);
	}

	@PreAuthorize(Authz.CATALOG_WRITE)
	@Transactional
	public DocumentView create(DocumentDraft draft) {
		UUID id = Ids.newId();
		TechnicalDocument document = TechnicalDocument.draft(id, content(draft, id), this.clock.instant());
		this.documents.insert(document);
		return get(id);
	}

	@PreAuthorize(Authz.CATALOG_WRITE)
	@Transactional
	public DocumentView update(UUID id, DocumentDraft draft, long expectedVersion) {
		TechnicalDocument document = load(id, expectedVersion);
		document.edit(content(draft, id), this.clock.instant());
		return save(document);
	}

	@PreAuthorize(Authz.CATALOG_PUBLISH)
	@Transactional
	public DocumentView publish(UUID id, long expectedVersion) {
		TechnicalDocument document = load(id, expectedVersion);
		document.publish(this.actor.requireUserId(), this.clock.instant());
		return save(document);
	}

	@PreAuthorize(Authz.CATALOG_PUBLISH)
	@Transactional
	public DocumentView withdraw(UUID id, long expectedVersion) {
		TechnicalDocument document = load(id, expectedVersion);
		document.withdraw(this.actor.requireUserId(), this.clock.instant());
		return save(document);
	}

	private DocumentView save(TechnicalDocument document) {
		this.documents.update(document);
		this.events.publishFrom(document);
		return get(document.id());
	}

	private TechnicalDocument load(UUID id, long expectedVersion) {
		TechnicalDocument document = this.documents.findById(id).orElseThrow(DocumentAdminService::notFound);
		if (document.version() != expectedVersion) {
			throw new StaleVersionException();
		}
		return document;
	}

	private TechnicalDocument.Content content(DocumentDraft d, UUID id) {
		Slug slug = Slug.of("slug", d.slug());
		if (this.documents.slugTaken(slug.value(), id)) {
			throw new ConflictException("SLUG_TAKEN", "Ya existe un documento con ese slug.");
		}
		Set<UUID> productIds = new HashSet<>();
		for (String productSlug : d.productSlugs() == null ? List.<String>of() : d.productSlugs()) {
			productIds.add(this.readModel.productBySlug(Slug.of("productSlugs", productSlug).value(), false)
				.orElseThrow(() -> new ValidationException("productSlugs", "UNKNOWN_PRODUCT", "Producto no encontrado: " + productSlug))
				.id());
		}
		TechnicalDocument.FileReference file = d.file() == null ? null
				: new TechnicalDocument.FileReference(d.file().url(), d.file().mediaType(), d.file().sizeBytes(),
						d.file().sha256());
		return new TechnicalDocument.Content(slug, d.code(), d.title(), d.description(),
				parse(TechnicalDocument.Type.class, "type", d.type()), d.families() == null ? List.of() : d.families(),
				parse(TechnicalDocument.Availability.class, "availability", d.availability()), file, d.revision(),
				d.language() == null ? "es" : d.language().toLowerCase(Locale.ROOT), productIds);
	}

	private static <E extends Enum<E>> E parse(Class<E> type, String field, @Nullable String value) {
		try {
			return Enum.valueOf(type, value == null ? "" : value.strip().toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw new ValidationException(field, "INVALID_VALUE", "Valor no válido.");
		}
	}

	private static NotFoundException notFound() {
		return new NotFoundException("DOCUMENT_NOT_FOUND", "El documento no existe.");
	}

}

package com.fundigsac.privacy.api;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.AuditableEvent;

/** Eventos de privacidad. */
public final class PrivacyEvents {

	private PrivacyEvents() {
	}

	public record DataSubjectRequestReceived(UUID requestId, String folio, String type, String fullName, String email,
			Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "DSR_RECEIVED";
		}

		@Override
		public String auditResourceType() {
			return "DATA_SUBJECT_REQUEST";
		}

		@Override
		public String auditResourceId() {
			return this.requestId.toString();
		}

		@Override
		public @Nullable UUID actorId() {
			return null;
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("folio", this.folio, "type", this.type);
		}

		@Override
		public String toString() {
			return "DataSubjectRequestReceived[folio=" + this.folio + "]";
		}

	}

	public record DataSubjectRequestUpdated(UUID requestId, String folio, String status, String fullName, String email,
			@Nullable String resolutionNote, UUID actorId, Instant occurredAt) implements AuditableEvent {

		@Override
		public String auditAction() {
			return "DSR_" + this.status;
		}

		@Override
		public String auditResourceType() {
			return "DATA_SUBJECT_REQUEST";
		}

		@Override
		public String auditResourceId() {
			return this.requestId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("folio", this.folio);
		}

		@Override
		public String toString() {
			return "DataSubjectRequestUpdated[folio=" + this.folio + ", status=" + this.status + "]";
		}

	}

	public record LegalNoticePublished(UUID noticeId, String type, String version, UUID actorId, Instant occurredAt)
			implements AuditableEvent {

		@Override
		public String auditAction() {
			return "LEGAL_NOTICE_PUBLISHED";
		}

		@Override
		public String auditResourceType() {
			return "LEGAL_NOTICE";
		}

		@Override
		public String auditResourceId() {
			return this.noticeId.toString();
		}

		@Override
		public Map<String, String> auditDetails() {
			return Map.of("type", this.type, "version", this.version);
		}

	}

}

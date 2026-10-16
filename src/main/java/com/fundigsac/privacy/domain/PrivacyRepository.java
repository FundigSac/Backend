package com.fundigsac.privacy.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Puerto de persistencia de textos legales, consentimientos y solicitudes de derechos. */
public interface PrivacyRepository {

	Optional<LegalNotice> currentNotice(LegalNotice.Type type, java.time.Instant at);

	List<LegalNotice> noticeVersions(LegalNotice.Type type);

	boolean noticeVersionExists(LegalNotice.Type type, String version);

	void insertNotice(LegalNotice notice);

	void insertConsent(ConsentRecord record);

	void insertRequest(DataSubjectRequest request);

	void updateRequest(DataSubjectRequest request);

	Optional<DataSubjectRequest> findRequest(UUID id);

	Optional<DataSubjectRequest> findRequestByFolio(String folio);

	List<DataSubjectRequest> searchRequests(@Nullable String status, int limit, long offset);

	long countRequests(@Nullable String status);

}

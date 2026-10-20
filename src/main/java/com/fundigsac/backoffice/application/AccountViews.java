package com.fundigsac.backoffice.application;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

/** Vistas del directorio de cuentas para el panel interno. */
public final class AccountViews {

	private AccountViews() {
	}

	/** Una cuenta con los datos que el panel muestra en la tabla y en la ficha. */
	public record AccountRow(UUID id, String kind, String fullName, String email, @Nullable String companyName,
			@Nullable String ruc, @Nullable String phone, String signupMethod, String status, List<String> roles,
			boolean emailVerified, boolean newsletter, @Nullable String privacyNoticeVersion, @Nullable String signupSource,
			Instant createdAt, @Nullable Instant lastLoginAt, long quotes, long cartItems, long activeSessions, long signIns,
			@Nullable String notes, long version) {
	}

	/** Página de cuentas más los conteos por estado con los mismos filtros (menos el estado), para las pestañas. */
	public record AccountPage(List<AccountRow> items, int page, int size, long totalItems, int totalPages,
			Map<String, Long> statusCounts) {
	}

	public record DayCount(LocalDate day, long count) {
	}

	public record SourceCount(String source, long count) {
	}

	/** Embudo del periodo: cuentas nuevas, cuántas verificaron el correo y cuántas enviaron al menos una cotización. */
	public record Funnel(long registered, long verified, long quoted) {
	}

	/** Indicadores de las cuentas de clientes en el periodo (y el anterior del mismo largo, para comparar). */
	public record AccountStats(long total, long newInRange, long newPrevRange, long pending, long activeWeek,
			long disabled, double verifiedRate, double newsletterRate, List<DayCount> series,
			Map<String, Long> byMethod, Map<String, Long> byStatus, List<SourceCount> bySource, List<AccountRow> latest,
			Funnel funnel, List<AccountRow> pendingOldest, long pendingOver48h, Instant generatedAt) {
	}

	/**
	 * Un hecho de la actividad de las cuentas.
	 *
	 * @param type SIGNUP, LOGIN, VERIFY, QUOTE, PASSWORD, DISABLE, ENABLE, ROLES, UPDATE, DELETE o INVITE
	 * @param actorName quién lo hizo cuando no fue el propio usuario (personal)
	 */
	public record ActivityRow(String id, Instant at, String type, UUID userId, String userName, @Nullable String actorName,
			@Nullable String detail) {
	}

}

package com.fundigsac.quotation.domain;

import java.time.LocalDate;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.ValidationException;

/** Contexto del proyecto: tipo, destino, fecha de entrega deseada y mensaje técnico. */
public record ProjectDetails(@Nullable String projectType, @Nullable String deliveryDepartment,
		@Nullable String deliveryPlace, @Nullable LocalDate requestedDeliveryDate, @Nullable String message) {

	public ProjectDetails {
		projectType = Guard.optionalText("project.projectType", projectType, 120);
		deliveryDepartment = Guard.optionalText("project.deliveryDepartment", deliveryDepartment, 60);
		deliveryPlace = Guard.optionalText("project.deliveryPlace", deliveryPlace, 200);
		message = Guard.optionalMultiline("project.message", message, 4000);
	}

	/** La fecha de entrega deseada no puede estar en el pasado ni a más de tres años (hora de Lima). */
	public void checkDeliveryDate(LocalDate today) {
		if (this.requestedDeliveryDate != null
				&& (this.requestedDeliveryDate.isBefore(today) || this.requestedDeliveryDate.isAfter(today.plusYears(3)))) {
			throw new ValidationException("project.requestedDeliveryDate", "INVALID_DATE",
					"La fecha de entrega debe ser hoy o posterior (máximo 3 años).");
		}
	}

}

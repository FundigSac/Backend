package com.fundigsac.complaint.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

/**
 * Plazo de respuesta en días hábiles contados desde el día siguiente al registro (sábados, domingos y feriados
 * configurados no cuentan). Por defecto 15 días hábiles (art. 24 del Código de Protección y Defensa del Consumidor,
 * modificado por la Ley N.° 31435): Legal debe confirmar el valor y mantener la lista de feriados. Sin feriados
 * configurados el plazo calculado es más corto, nunca más largo que el legal.
 */
public record ResponseDeadlinePolicy(int businessDays, Set<LocalDate> holidays) {

	public ResponseDeadlinePolicy {
		if (businessDays < 1 || businessDays > 60) {
			throw new IllegalArgumentException("Plazo de respuesta fuera de rango");
		}
		holidays = Set.copyOf(holidays);
	}

	public LocalDate dueDate(LocalDate filedOn) {
		LocalDate day = filedOn;
		int counted = 0;
		while (counted < this.businessDays) {
			day = day.plusDays(1);
			if (isBusinessDay(day)) {
				counted++;
			}
		}
		return day;
	}

	public boolean isBusinessDay(LocalDate day) {
		return day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY
				&& !this.holidays.contains(day);
	}

}

package com.fundigsac.complaint.application;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Parámetros legales del Libro de Reclamaciones (los define y mantiene Atención Legal).
 *
 * @param responseBusinessDays plazo máximo de respuesta en días hábiles
 * @param holidays feriados nacionales que no cuentan como hábiles
 */
@ConfigurationProperties("fundigsac.complaints")
public record ComplaintProperties(@DefaultValue("15") int responseBusinessDays, @DefaultValue List<LocalDate> holidays) {
}

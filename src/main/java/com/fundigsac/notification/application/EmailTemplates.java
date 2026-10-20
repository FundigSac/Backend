package com.fundigsac.notification.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Component;

import com.fundigsac.shared.domain.PeruTime;

/**
 * Textos de los correos (español, texto plano: legible en cualquier cliente y sin contenido remoto). Fechas en hora
 * de Lima. Los enlaces con token usan el fragmento (#) para que el token no viaje en cabeceras Referer ni en logs.
 */
@Component
class EmailTemplates {

	record Message(String subject, String body) {
	}

	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy, HH:mm 'h'",
			Locale.forLanguageTag("es-PE"));

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy",
			Locale.forLanguageTag("es-PE"));

	private static final String SIGNATURE = """

			Atentamente,
			FUNDIGSAC - Fundición y Desarrollo de Ingeniería y Gestión S.A.C.
			Este es un mensaje automático; no responda a este correo.""";

	private final NotificationProperties properties;

	EmailTemplates(NotificationProperties properties) {
		this.properties = properties;
	}

	Message quoteReceipt(String name, String folio, int lines, Instant at) {
		return new Message("FUNDIGSAC: recibimos su solicitud de cotización " + folio, """
				Hola %s:

				Registramos su solicitud de cotización con el folio %s el %s (hora de Lima), con %d producto(s).
				El equipo de Ventas revisará su requerimiento y le enviará la propuesta por este medio. Guarde su folio para
				cualquier consulta.
				""".formatted(name, folio, format(at), lines) + SIGNATURE);
	}

	Message quoteForSales(String folio, String company, String contact, String origin, int lines) {
		return new Message("Nueva solicitud de cotización " + folio + " - " + company, """
				Se registró la solicitud %s.
				Empresa: %s
				Contacto: %s
				Origen: %s
				Productos: %d

				Revísela en la bandeja de cotizaciones.
				""".formatted(folio, company, contact, origin, lines));
	}

	Message inquiryReceipt(String name, String folio, Instant at) {
		return new Message("FUNDIGSAC: recibimos su consulta " + folio, """
				Hola %s:

				Registramos su consulta con el folio %s el %s (hora de Lima). Le responderemos a la brevedad.
				""".formatted(name, folio, format(at)) + SIGNATURE);
	}

	Message inquiryForSales(String folio, String company, String name, String reason) {
		return new Message("Nueva consulta " + folio + " - " + company, """
				Se registró la consulta %s.
				Empresa: %s
				Contacto: %s
				Motivo: %s
				""".formatted(folio, company, name, reason));
	}

	Message complaintReceipt(String name, String folio, String kind, LocalDate dueOn, Instant at) {
		String type = "CLAIM".equals(kind) ? "reclamo" : "queja";
		return new Message("FUNDIGSAC: constancia de su " + type + " " + folio, """
				Hola %s:

				Su %s quedó registrado en nuestro Libro de Reclamaciones virtual con la hoja N.° %s el %s (hora de Lima).
				Le daremos respuesta a más tardar el %s, por el medio que eligió.
				""".formatted(name, type, folio, format(at), DATE.format(dueOn)) + SIGNATURE);
	}

	Message complaintForLegal(String folio, String kind, LocalDate dueOn) {
		return new Message("Libro de Reclamaciones: nueva hoja " + folio, """
				Se registró la hoja %s (%s). Fecha límite de respuesta: %s.
				Revísela en la bandeja de reclamaciones.
				""".formatted(folio, kind, DATE.format(dueOn)));
	}

	Message complaintAnswer(String name, String folio, String response) {
		return new Message("FUNDIGSAC: respuesta a su hoja de reclamación " + folio, """
				Hola %s:

				En atención a su hoja de reclamación N.° %s, le informamos:

				%s
				""".formatted(name, folio, response) + SIGNATURE);
	}

	Message complaintAnswerByPost(String folio) {
		return new Message("Libro de Reclamaciones: enviar respuesta por correo postal " + folio, """
				La hoja %s ya tiene respuesta registrada y el consumidor eligió recibirla en su domicilio.
				Envíela por correo postal y deje constancia en la hoja.
				""".formatted(folio));
	}

	Message rightsReceipt(String name, String folio, String type) {
		return new Message("FUNDIGSAC: recibimos su solicitud de datos personales " + folio, """
				Hola %s:

				Registramos su solicitud (%s) con el folio %s. La atenderemos dentro de los plazos de la Ley N.° 29733 y su
				reglamento, y le escribiremos a este correo.
				""".formatted(name, type, folio) + SIGNATURE);
	}

	Message rightsForPrivacy(String folio, String type) {
		return new Message("Nueva solicitud de derechos del titular " + folio, """
				Se registró la solicitud %s (%s). Revísela en la bandeja de privacidad.
				""".formatted(folio, type));
	}

	Message rightsResolution(String name, String folio, String status, String note) {
		String result = "RESOLVED".equals(status) ? "fue atendida" : "no pudo ser atendida";
		return new Message("FUNDIGSAC: resultado de su solicitud " + folio, """
				Hola %s:

				Su solicitud %s %s.

				%s
				""".formatted(name, folio, result, note) + SIGNATURE);
	}

	Message verifyEmail(String name, String token, Instant expiresAt) {
		return new Message("FUNDIGSAC: confirme su correo", """
				Hola %s:

				Para activar su cuenta, abra este enlace:
				%s/verificar-correo#token=%s

				El enlace vence el %s (hora de Lima). Si usted no creó la cuenta, ignore este mensaje.
				""".formatted(name, site(), token, format(expiresAt)) + SIGNATURE);
	}

	Message passwordReset(String name, String token, Instant expiresAt) {
		return new Message("FUNDIGSAC: restablezca su contraseña", """
				Hola %s:

				Recibimos un pedido para restablecer su contraseña. Abra este enlace:
				%s/restablecer#token=%s

				El enlace vence el %s (hora de Lima) y sirve una sola vez. Si usted no lo pidió, ignore este mensaje: su
				contraseña no cambia.
				""".formatted(name, site(), token, format(expiresAt)) + SIGNATURE);
	}

	/** El personal entra sin contraseña: la invitación solo indica dónde ingresar con su correo. */
	Message staffInvitation(String name, String email) {
		return new Message("FUNDIGSAC: invitación al panel interno", """
				Hola %s:

				Le crearon una cuenta en el panel interno de FUNDIGSAC. Para ingresar, abra:
				%s/login

				Escriba su correo (%s) y le enviaremos un código de 6 dígitos. No necesita contraseña.
				""".formatted(name, admin(), email) + SIGNATURE);
	}

	Message staffLoginCode(String name, String code, Instant expiresAt) {
		return new Message("FUNDIGSAC: su código de acceso al panel interno", """
				Hola %s:

				Su código para ingresar al panel interno es:

				    %s

				Vence el %s (hora de Lima) y sirve una sola vez. Nadie de FUNDIGSAC se lo pedirá: no lo comparta. Si usted
				no intentó ingresar, ignore este mensaje.
				""".formatted(name, code, format(expiresAt)) + SIGNATURE);
	}

	Message registrationAttempt(String name) {
		return new Message("FUNDIGSAC: intento de registro con su correo", """
				Hola %s:

				Alguien intentó crear una cuenta con su correo, que ya está registrado. Si fue usted, inicie sesión o use
				"¿Olvidó su contraseña?". Si no fue usted, no necesita hacer nada.
				""".formatted(name) + SIGNATURE);
	}

	Message passwordChanged(String name, Instant at) {
		return new Message("FUNDIGSAC: su contraseña cambió", """
				Hola %s:

				La contraseña de su cuenta cambió el %s (hora de Lima) y se cerraron las sesiones abiertas. Si no fue usted,
				restablézcala de inmediato y escríbanos.
				""".formatted(name, format(at)) + SIGNATURE);
	}

	Message accountLocked(String name, Instant until) {
		return new Message("FUNDIGSAC: bloqueamos temporalmente su cuenta", """
				Hola %s:

				Detectamos varios intentos fallidos de inicio de sesión y bloqueamos su cuenta hasta el %s (hora de Lima).
				Si no fue usted, le recomendamos restablecer su contraseña.
				""".formatted(name, format(until)) + SIGNATURE);
	}

	private String site() {
		return trimSlash(this.properties.siteUrl());
	}

	private String admin() {
		return trimSlash(this.properties.adminUrl());
	}

	private static String trimSlash(String url) {
		return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
	}

	private static String format(Instant instant) {
		return DATE_TIME.format(instant.atZone(PeruTime.ZONE));
	}

}

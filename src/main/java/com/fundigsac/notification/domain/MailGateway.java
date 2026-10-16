package com.fundigsac.notification.domain;

/** Puerto de envío de correo (SMTP en infraestructura). Lanza excepción si el proveedor rechaza el envío. */
public interface MailGateway {

	void send(String to, String subject, String body);

}

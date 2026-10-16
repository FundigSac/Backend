package com.fundigsac.notification.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.fundigsac.notification.application.NotificationProperties;
import com.fundigsac.notification.domain.MailGateway;

/**
 * Envío por SMTP (Mailpit en desarrollo, proveedor transaccional en producción) o, con delivery=log, solo registro
 * (sin destinatario ni contenido en el log).
 */
@Component
class ConfiguredMailGateway implements MailGateway {

	private static final Logger log = LoggerFactory.getLogger(ConfiguredMailGateway.class);

	private final ObjectProvider<JavaMailSender> sender;

	private final NotificationProperties properties;

	ConfiguredMailGateway(ObjectProvider<JavaMailSender> sender, NotificationProperties properties) {
		this.sender = sender;
		this.properties = properties;
	}

	@Override
	public void send(String to, String subject, String body) {
		if (!"smtp".equalsIgnoreCase(this.properties.delivery())) {
			log.info("Correo no enviado (delivery=log): \"{}\"", subject);
			return;
		}
		JavaMailSender mail = this.sender.getIfAvailable();
		if (mail == null) {
			throw new IllegalStateException("SMTP no configurado (spring.mail.host)");
		}
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(this.properties.from());
		message.setTo(to);
		message.setSubject(subject);
		message.setText(body);
		mail.send(message);
	}

}

package com.fundigsac.notification.infrastructure;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.fundigsac.notification.application.NotificationProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Envío real por SMTP frente al modo de solo registro. */
@ExtendWith(OutputCaptureExtension.class)
class ConfiguredMailGatewayTests {

	private final JavaMailSender sender = mock(JavaMailSender.class);

	@SuppressWarnings("unchecked")
	private final ObjectProvider<JavaMailSender> provider = mock(ObjectProvider.class);

	private ConfiguredMailGateway gateway(String delivery) {
		return new ConfiguredMailGateway(this.provider, new NotificationProperties(delivery, "FUNDIGSAC <de@fundigsac.pe>", "http://sitio",
				"http://panel", "ventas@fundigsac.pe", "legal@fundigsac.pe", "privacidad@fundigsac.pe", 8, Duration.ofSeconds(30), 20));
	}

	@Test
	void theLogModeDoesNotWriteTheSubject(CapturedOutput output) {
		gateway("log").send("cliente@empresa.pe", "Nueva consulta de Empresa Privada SAC", "Cuerpo");

		assertThat(output.getAll()).contains("delivery=log").doesNotContain("Empresa Privada", "cliente@empresa.pe", "Cuerpo");
	}

	@Test
	void sendsThroughSmtpWithTheConfiguredSender() {
		when(this.provider.getIfAvailable()).thenReturn(this.sender);

		gateway("SMTP").send("cliente@empresa.pe", "Asunto", "Cuerpo");

		ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(this.sender).send(message.capture());
		assertThat(message.getValue().getFrom()).isEqualTo("FUNDIGSAC <de@fundigsac.pe>");
		assertThat(message.getValue().getTo()).containsExactly("cliente@empresa.pe");
		assertThat(message.getValue().getSubject()).isEqualTo("Asunto");
		assertThat(message.getValue().getText()).isEqualTo("Cuerpo");
	}

	@Test
	void logDeliveryNeverTouchesSmtp() {
		gateway("log").send("cliente@empresa.pe", "Asunto", "Cuerpo");
		verify(this.provider, never()).getIfAvailable();
	}

	@Test
	void smtpWithoutAConfiguredServerFailsSoTheOutboxRetries() {
		when(this.provider.getIfAvailable()).thenReturn(null);
		assertThatThrownBy(() -> gateway("smtp").send("cliente@empresa.pe", "Asunto", "Cuerpo")).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("spring.mail.host");
	}

	@Test
	void anSmtpFailureReachesTheDispatcher() {
		when(this.provider.getIfAvailable()).thenReturn(this.sender);
		doThrow(new MailSendException("caído")).when(this.sender).send(any(SimpleMailMessage.class));
		assertThatThrownBy(() -> gateway("smtp").send("cliente@empresa.pe", "Asunto", "Cuerpo")).isInstanceOf(MailSendException.class);
	}

}

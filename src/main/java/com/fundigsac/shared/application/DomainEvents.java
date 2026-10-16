package com.fundigsac.shared.application;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.fundigsac.shared.domain.AggregateRoot;
import com.fundigsac.shared.domain.DomainEvent;

/**
 * Publica los eventos de un agregado ya persistido. Dentro de la transacción, Spring Modulith los registra en la
 * tabla event_publication (outbox) y los entrega a los otros módulos solo si la transacción se confirma.
 */
@Component
public class DomainEvents {

	private final ApplicationEventPublisher publisher;

	public DomainEvents(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	public void publishFrom(AggregateRoot aggregate) {
		aggregate.pullEvents().forEach(this.publisher::publishEvent);
	}

	public void publish(DomainEvent event) {
		this.publisher.publishEvent(event);
	}

}

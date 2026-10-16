package com.fundigsac.shared.domain;

import java.util.ArrayList;
import java.util.List;

/** Raíz de agregado: guarda los eventos que produjo hasta que la capa de aplicación los publica. */
public abstract class AggregateRoot {

	private final List<DomainEvent> pendingEvents = new ArrayList<>();

	protected void register(DomainEvent event) {
		this.pendingEvents.add(event);
	}

	/** Devuelve y vacía los eventos pendientes (se publican tras persistir el agregado). */
	public List<DomainEvent> pullEvents() {
		List<DomainEvent> events = List.copyOf(this.pendingEvents);
		this.pendingEvents.clear();
		return events;
	}

}

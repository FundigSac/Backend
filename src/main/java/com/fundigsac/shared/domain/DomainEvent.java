package com.fundigsac.shared.domain;

import java.time.Instant;

/** Hecho de negocio ya ocurrido. Los eventos son inmutables (records) y viajan entre módulos. */
public interface DomainEvent {

	Instant occurredAt();

}

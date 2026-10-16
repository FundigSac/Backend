package com.fundigsac.quotation.domain;

import org.jspecify.annotations.Nullable;

import com.fundigsac.shared.domain.Email;
import com.fundigsac.shared.domain.Guard;
import com.fundigsac.shared.domain.PhoneNumber;
import com.fundigsac.shared.domain.Ruc;

/** Empresa y persona que solicitan la cotización (datos mínimos para responder). */
public record Requester(String companyName, Ruc ruc, String contactName, @Nullable String position, Email email,
		PhoneNumber phone) {

	public Requester {
		companyName = Guard.text("contact.companyName", companyName, 2, 160);
		Guard.notNull("contact.ruc", ruc);
		contactName = Guard.text("contact.contactName", contactName, 2, 120);
		position = Guard.optionalText("contact.position", position, 120);
		Guard.notNull("contact.email", email);
		Guard.notNull("contact.phone", phone);
	}

}

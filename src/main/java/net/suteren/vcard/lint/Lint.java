package net.suteren.vcard.lint;

import net.wimpi.pim.contact.model.Contact;

public class Lint {

	public static boolean validateContact(Contact contact) {
		//TODO
		contact.getAddresses();
		contact.getCommunications();
		contact.getOrganizationalIdentity();
		contact.getPersonalIdentity()	;
		
		return false;
	}
}

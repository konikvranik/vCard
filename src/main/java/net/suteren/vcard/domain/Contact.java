package net.suteren.vcard.domain;

import java.util.Map;
import java.util.Set;

import net.suteren.vcard.Relations;

public class Contact {


	private Name name;
	private Map<Relations, PostalAddress> postalAddressess;
	private Set<Category> categories; //TODO
	private String content;
	private Map<Service, String> identifiers; //TODO
	private Map<Relations, Email> emails;
	private Map<Relations, InstantMessenger> instantMessengers;
	private Map<Relations, PhoneNumber> phoneNumbers;
	private Map<Relations, Organization> organizations;
	private Set<Group> groups;

}

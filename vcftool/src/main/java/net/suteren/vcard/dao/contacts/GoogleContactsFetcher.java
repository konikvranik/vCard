package net.suteren.vcard.dao.contacts;

import java.util.Iterator;

import com.google.gdata.data.contacts.ContactEntry;

public class GoogleContactsFetcher implements Iterable<ContactEntry> {

	
	private char[] password;
	private String username;

	public GoogleContactsFetcher(String username, char[] password) {
		this.username = username;
		this.password=password;	}
	
	@Override
	public Iterator<ContactEntry> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

}

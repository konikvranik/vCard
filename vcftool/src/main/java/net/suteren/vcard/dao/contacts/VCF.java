package net.suteren.vcard.dao.contacts;

import java.io.OutputStream;
import java.util.List;

import net.wimpi.pim.Pim;
import net.wimpi.pim.contact.io.ContactMarshaller;
import net.wimpi.pim.contact.model.Contact;
import net.wimpi.pim.factory.ContactIOFactory;

public class VCF {
	private static ContactIOFactory ciof = Pim.getContactIOFactory();
	private static ContactMarshaller marshaller = ciof.createContactMarshaller();
	
	public static void saveVCF(Contact contact, OutputStream outStream){
		marshaller.marshallContact(outStream, contact);	
	}
	
	public static void saveAddressbook(List<Contact> contacts, OutputStream outStream){
		for (Contact c:contacts){
			saveVCF(c, outStream);
		}
	}

}

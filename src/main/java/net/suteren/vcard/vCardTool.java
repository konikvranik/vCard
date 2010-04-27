package net.suteren.vcard;

import java.io.IOException;

import net.suteren.vcard.dao.contacts.Google;
import net.suteren.vcard.dao.contacts.VCF;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gdata.util.ServiceException;

public class vCardTool {

	private static Logger log = LoggerFactory.getLogger(vCardTool.class);

	public static void main(String[] args) {

		try {

			char[] pwd = { 'A', 'l',

			'p', 'h',

			'a', 'x', '1',

			'0' };
			Google g = new Google("petr.vranik.test@gmail.com", pwd);

			VCF.saveAddressbook(g.getPimContacts(""), System.out);

		} catch (IOException e) {
			log.error("Exception occured.", e);
		} catch (ServiceException e) {
			log.error("Exception occured.", e);
		}
		
	}

}

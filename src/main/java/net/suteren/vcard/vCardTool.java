package net.suteren.vcard;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;

import net.suteren.vcard.dao.contacts.Google;
import net.suteren.vcard.dao.contacts.VCF;
import net.wimpi.pim.contact.model.Contact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;

public class vCardTool {

	static {

	}
	private static Logger log = LoggerFactory.getLogger(vCardTool.class);
	private char[] pwd;
	private String username;
	private Google g;

	vCardTool() throws IOException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException,
			Base64DecoderException, AuthenticationException {
		Properties props = System.getProperties();

		InputStream vcp = vCardTool.class.getClassLoader().getResourceAsStream(
				"vcards.properties");

		props.load(vcp);

		username = props.getProperty("google.username");

		pwd = decodePassword(props.getProperty("google.password"), username);

		g = new Google(username, pwd);

	}

	public static void main(String[] args) throws IOException,
			Base64DecoderException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException, ServiceException {

		vCardTool vct = new vCardTool();

		List<ContactEntry> entries = vct.getContactEntriesFromGoogle();
		for (ContactEntry ce : entries) {
			boolean update = false;
			URL etditUrl;
			if (ce.getTitle() != null) {
				TextConstruct title = ce.getTitle();
				String name = title.getPlainText();
				if (name.matches(",.*,\\s*$")) {
					name = name.replaceAll("\\s*,\\s*", " ");
					name = name.trim();
					update = true;
				} else if (name.matches(",.*[^,]\\s*$")) {
					String[] x = name.split(",", 2);
					name = x[1] + " " + x[0];
					update = true;
				}
				if (update) {
					log.warn("Updating name " + ce.getTitle().getPlainText()
							+ " to " + name);
					ce.setTitle(new PlainTextConstruct(name));
				}
			}
			List<PhoneNumber> pn = ce.getPhoneNumbers();
			for (PhoneNumber p : pn) {
				String n = p.getPhoneNumber();
				n = n.trim();

				if (n.matches("^420")) {
					n = "+" + n;
				}
				if (n.matches("^+?(\\d|\\s)+$")) {
					n = n.replaceAll("\\s", "");
				}
				if (n != null && !n.equals(p.getPhoneNumber())) {
					log.warn("Updating " + ce.getTitle().getPlainText()
							+ " phone " + p.getPhoneNumber() + " to " + n);
					p.setPhoneNumber(n);
				}
				update = true;
			}
			if (update) {
				vct.updateGoogleContact(ce);

			}

		}

	}

	private char[] decodePassword(String password, String keyString)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException,
			BadPaddingException, Base64DecoderException {

		byte[] a = Base64.decode(password);
		Cipher dcipher = Cipher.getInstance("DES");
		dcipher.init(Cipher.DECRYPT_MODE, prepareKey(keyString));
		return new String(dcipher.doFinal(new String(a).getBytes()))
				.toCharArray();

	}

	private SecretKeySpec prepareKey(String key) {

		for (int i = 0; i < 3; i++) {
			key = key + key;
		}
		key = key.substring(0, 8);
		return new SecretKeySpec(key.getBytes(), "DES");

	}

	private String encodePassword(String password, String keyString)
			throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher ecipher = Cipher.getInstance("DES");
		ecipher.init(Cipher.ENCRYPT_MODE, prepareKey(keyString));

		return Base64.encode(ecipher.doFinal(password.getBytes()).toString()
				.getBytes());

	}

	private List<ContactEntry> getContactEntriesFromGoogle()
			throws IOException, ServiceException {
		return g.getGoogleContacts("");
	}

	private List<Contact> getContactsFromGoogle() throws IOException,
			ServiceException {
		return g.getPimContacts("");
	}

	private ContactEntry updateGoogleContact(ContactEntry ce)
			throws IOException, ServiceException {
		return g.updateContact(ce);

	}

	public List<Contact> cleanupContacts(List<Contact> originalContacts) {
		// TODO Auto-generated method stub
		return originalContacts;
	}

}

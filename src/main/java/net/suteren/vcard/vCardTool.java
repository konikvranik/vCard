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

import com.google.gdata.data.BaseEntry;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.TextConstruct;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.Nickname;
import com.google.gdata.data.extensions.AdditionalName;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.Name;
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

		// vct.listGoogleContacts();

		vct.upodate();
	}

	private void upodate() throws IOException, ServiceException {

		List<ContactEntry> ce = g.getGoogleContacts("");
		modifyGoogleContacts(ce);

	}

	private void listGoogleContacts() throws IOException, ServiceException {
		List<ContactEntry> entries = getContactEntriesFromGoogle();
		int cnt = 1;
		for (ContactEntry ce : entries) {
			Name name = ce.getName();
			FamilyName family = null;
			GivenName given = null;
			AdditionalName middle = null;
			FullName fullName = null;
			if (name != null) {
				family = name.getFamilyName();
				given = name.getGivenName();
				middle = name.getAdditionalName();
				fullName = name.getFullName();
			}
			Nickname nick = ce.getNickname();
			TextConstruct title = ce.getTitle();

			log.info((cnt++) + ". # "
					+ (family == null ? "FAMILY" : family.getValue()) + " # "
					+ (given == null ? "GIVEN" : given.getValue()) + " # "
					+ (middle == null ? "MIDDLE" : middle.getValue()) + " # "
					+ (fullName == null ? "FULL" : fullName.getValue()) + " # "
					+ (nick == null ? "NICK" : nick.getValue()) + " # "
					+ (title == null ? "TITLE" : title.getPlainText()) + " # ");

		}
	}

	private void modifyGoogleContacts(List<ContactEntry> entries)
			throws IOException, ServiceException {
		int cnt = 1;
		for (ContactEntry ce : entries) {
			String name = null;
			boolean update = false;
			if (ce.getTitle() == null) {
				Name n = ce.getName();
				if (n != null) {
					FullName fn = n.getFullName();
					if (fn != null) {
						name = fn.getValue();
					}
				}
			} else {
				TextConstruct title = ce.getTitle();
				name = title.getPlainText();
			}
			log.info((cnt++) + ". == " + name + " ==");

			if (name.matches(".*,.*,\\s*")) {
				name = name.replaceAll("\\s*,\\s*", " ");
				name = name.trim();
				update = true;
			} else if (name.matches(".*,.*[^,]\\s*")) {
				String[] x = name.split(",", 2);
				name = x[1] + " " + x[0];
				update = true;
			}
			if (update) {
				log.warn("Updating TITLE " + ce.getTitle().getPlainText()
						+ " to " + name);
				ce.setTitle(new PlainTextConstruct(name));
			}

			Name gName = ce.getName();
			if (gName != null) {

				FullName fn = gName.getFullName();
				if (fn != null) {
					name = fn.getValue();

					if (name.matches(".*,.*,\\s*")) {
						name = name.replaceAll("\\s*,\\s*", " ");
						name = name.trim();
						update = true;
					} else if (name.matches(".*,.*[^,]\\s*")) {
						String[] x = name.split(",", 2);
						name = x[1] + " " + x[0];
						update = true;
					}

					if (update) {
						log.warn("Updating FULLNAME "
								+ ce.getTitle().getPlainText() + " to " + name);
						fn.setValue(name);
					}

				}

				GivenName given = gName.getGivenName();
				FamilyName family = gName.getFamilyName();
				if (given != null && family != null) {
					if (given.getValue().matches(".*,")) {
						if (family.getValue().matches(".*,")) {
							log.warn("Swapping GIVEN and FAMILY "
									+ given.getValue() + " "
									+ family.getValue());
							name = family.getValue();
							family.setValue(given.getValue());
							given.setValue(name);
							update = true;
						}
					}
				}

				if (given != null && given.getValue().matches(".*,\\s*")) {
					log.warn("Updating GIVEN " + given.getValue());

					given.setValue(given.getValue().replaceAll(",\\s*", " ")
							.trim());
					update = true;
				}
				if (family != null && family.getValue().matches(".*,\\s*")) {
					log.warn("Updating FAMILY " + family.getValue());
					family.setValue(family.getValue().replaceAll(",\\s*", " ")
							.trim());
					update = true;
				}
			}

			List<PhoneNumber> pn = ce.getPhoneNumbers();
			for (PhoneNumber p : pn) {
				String n = p.getPhoneNumber();
				n = n.trim();

				if (n.matches("^\\s*420(\\d|\\s)+")) {
					n = "+" + n;
				}
				if (n.matches("^\\+?(\\d|\\s)+")) {
					n = n.replaceAll("\\s+", "");
				}
				if (n != null && !n.equals(p.getPhoneNumber())) {
					log.warn("Updating " + ce.getTitle().getPlainText()
							+ " phone " + p.getPhoneNumber() + " to " + n);
					p.setPhoneNumber(n);
				}
				update = true;
			}
			if (update) {
				boolean ok = true;
				int rpt = 1;
				do {
					ok = true;
					try {
						updateGoogleContact(ce);
					} catch (Exception e) {
						ok = false;
						log.warn("Update failed. Try " + rpt + ". ");
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e1) {
						}
					}
				} while (!ok & rpt++ < 30);
				if (!ok) {
					log.error("Update entr failed!");
				}
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

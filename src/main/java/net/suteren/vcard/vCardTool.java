package net.suteren.vcard;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;
import com.google.gdata.util.common.util.Base64;
import com.google.gdata.util.common.util.Base64DecoderException;

public class vCardTool {

	static {

	}
	private static Logger log = LoggerFactory.getLogger(vCardTool.class);

	public static void main(String[] args) throws IOException,
			Base64DecoderException, InvalidKeyException,
			NoSuchAlgorithmException, NoSuchPaddingException,
			IllegalBlockSizeException, BadPaddingException {

		Properties props = System.getProperties();

		InputStream vcp = vCardTool.class.getClassLoader().getResourceAsStream(
				"vcards.properties");

		props.load(vcp);

		String username = props.getProperty("google.username");

		vCardTool vct = new vCardTool();

		char[] pwd = vct.decodePassword(props.getProperty("google.password"),
				username);

		List<Contact> originalContacts;
		try {
			originalContacts = vct.getContactsFromGoogle(username, pwd);
			log.info("Count of original contacts: " + originalContacts.size());
			List<Contact> cleanedContacts = vct
					.cleanupContacts(originalContacts);
			log.info("Count of cleaned contacts: " + originalContacts.size());
			// VCF.saveAddressbook(cleanedContacts, System.out);
		} catch (IOException e) {
			log.error("Failed to get contacts from Google", e);
		} catch (ServiceException e) {
			log.error("Failed to get contacts from Google", e);
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

	private List<Contact> getContactsFromGoogle(String string, char[] pwd)
			throws IOException, ServiceException {
		Google g = new Google(string, pwd);
		return g.getPimContacts("");
	}

	public List<Contact> cleanupContacts(List<Contact> originalContacts) {
		// TODO Auto-generated method stub
		return originalContacts;
	}

}

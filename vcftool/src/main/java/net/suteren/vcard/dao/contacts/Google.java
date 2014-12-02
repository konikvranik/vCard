package net.suteren.vcard.dao.contacts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.suteren.vcard.Relations;
import net.wimpi.pim.Pim;
import net.wimpi.pim.contact.model.Address;
import net.wimpi.pim.contact.model.Communications;
import net.wimpi.pim.contact.model.Contact;
import net.wimpi.pim.contact.model.EmailAddress;
import net.wimpi.pim.contact.model.GeographicalInformation;
import net.wimpi.pim.contact.model.Image;
import net.wimpi.pim.contact.model.OrganizationalIdentity;
import net.wimpi.pim.contact.model.PersonalIdentity;
import net.wimpi.pim.factory.ContactModelFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gdata.client.Service;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.Category;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.contacts.Birthday;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.AdditionalName;
import com.google.gdata.data.extensions.City;
import com.google.gdata.data.extensions.Country;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FamilyName;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.GivenName;
import com.google.gdata.data.extensions.HouseName;
import com.google.gdata.data.extensions.Im;
import com.google.gdata.data.extensions.Name;
import com.google.gdata.data.extensions.NamePrefix;
import com.google.gdata.data.extensions.NameSuffix;
import com.google.gdata.data.extensions.Neighborhood;
import com.google.gdata.data.extensions.Organization;
import com.google.gdata.data.extensions.PhoneNumber;
import com.google.gdata.data.extensions.PoBox;
import com.google.gdata.data.extensions.PostCode;
import com.google.gdata.data.extensions.Region;
import com.google.gdata.data.extensions.Street;
import com.google.gdata.data.extensions.StructuredPostalAddress;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class Google {

	enum GoogleRelations {
		HOME("http://schemas.google.com/g/2005#home"), OTHER(
				"http://schemas.google.com/g/2005#other"), WORK(
				"http://schemas.google.com/g/2005#work"), FAX(
				"http://schemas.google.com/g/2005#fax"), HOME_FAX(
				"http://schemas.google.com/g/2005#home_fax"), MOBILE(
				"http://schemas.google.com/g/2005#mobile"), PAGER(
				"http://schemas.google.com/g/2005#pager"), WORK_FAX(
				"http://schemas.google.com/g/2005#work_fax"), NETMEETING(
				"http://schemas.google.com/g/2005#netmeeting"), ASSISTANT(
				"http://schemas.google.com/g/2005#assistant"), CALLBACK(
				"http://schemas.google.com/g/2005#callback"), CAR(
				"http://schemas.google.com/g/2005#car"), COMPANY_MAIN(
				"http://schemas.google.com/g/2005#company_main"), ISDN(
				"http://schemas.google.com/g/2005#isdn"), MAIN(
				"http://schemas.google.com/g/2005#main"), OTHER_FAX(
				"http://schemas.google.com/g/2005#other_fax"), RADIO(
				"http://schemas.google.com/g/2005#radio"), TELEX(
				"http://schemas.google.com/g/2005#telex"), TTY_TDD(
				"http://schemas.google.com/g/2005#tty_tdd"), WORK_MOBILE(
				"http://schemas.google.com/g/2005#work_mobile"), WORK_PAGER(
				"http://schemas.google.com/g/2005#work_pager"),

		OVERALL("http://schemas.google.com/g/2005#overall"), PRICE(
				"http://schemas.google.com/g/2005#price"), QUALITY(
				"http://schemas.google.com/g/2005#quality"),

		EVENT("http://schemas.google.com/g/2005#event"), EVENT_ALTERNATE(
				"http://schemas.google.com/g/2005#event.alternate"), EVENT_PARKING(
				"http://schemas.google.com/g/2005#event.parking"), EVENT_ATTENDEE(
				"http://schemas.google.com/g/2005#event.attendee"), EVENT_ORGANIZER(
				"http://schemas.google.com/g/2005#event.organizer"), EVENT_PERFORMER(
				"http://schemas.google.com/g/2005#event.performer"), EVENT_SPEAKER(
				"http://schemas.google.com/g/2005#event.speaker"),

		MESSAGE_BCC("http://schemas.google.com/g/2005#message.bcc"), MESSAGE_CC(
				"http://schemas.google.com/g/2005#message.cc"), MESSAGE_FROM(
				"http://schemas.google.com/g/2005#message.from"), MESSAGE_REPLY_TO(
				"http://schemas.google.com/g/2005#message.reply-to"), MESSAGE_TO(
				"http://schemas.google.com/g/2005#message.to"),

		REGULAR("http://schemas.google.com/g/2005#regular"), REVIEWS(
				"http://schemas.google.com/g/2005#reviews");

		static Map<String, GoogleRelations> byGoogle = new TreeMap<String, GoogleRelations>();
		private String googleType = null;

		static {
			for (GoogleRelations value : GoogleRelations.values()) {
				byGoogle.put(value.getGoogleType(), value);

			}
		}

		GoogleRelations(String gData) {
			googleType = gData;

		}

		public static GoogleRelations getByGoogle(String data) {
			if (data == null)
				return OTHER;
			return byGoogle.get(data);
		}

		public String getGoogleType() {
			return googleType;
		}

		public Relations getRelation() {
			return Relations.valueOf(this.name());
		}

		public boolean isHome() {
			switch (this) {
			case HOME:
			case HOME_FAX:
				return true;
			default:
				return false;
			}

		}

		public boolean isWork() {
			switch (this) {
			case WORK:
			case WORK_FAX:
			case WORK_MOBILE:
			case WORK_PAGER:
				return true;
			default:
				return false;
			}

		}

		public boolean isBBS() {
			switch (this) {
			default:
				return false;
			}
		}

		public boolean isCar() {
			switch (this) {
			case CAR:
				return true;
			default:
				return false;
			}
		}

		public boolean isMobile() {
			switch (this) {
			case MOBILE:
			case WORK_MOBILE:
				return true;
			default:
				return false;
			}
		}

		public boolean isFax() {
			switch (this) {
			case FAX:
			case WORK_FAX:
			case HOME_FAX:
			case OTHER_FAX:
				return true;
			default:
				return false;
			}
		}

		public boolean isISDN() {
			switch (this) {
			case ISDN:
				return true;
			default:
				return false;
			}
		}

		public boolean isModem() {
			switch (this) {
			default:
				return false;
			}
		}

		public boolean isPager() {
			switch (this) {
			case PAGER:
			case WORK_PAGER:
				return true;
			default:
				return false;
			}
		}

		public boolean isPCS() {
			switch (this) {
			default:
				return false;
			}
		}

		public boolean isPreferred() {
			switch (this) {
			case MAIN:
			case COMPANY_MAIN:
				return true;
			default:
				return false;
			}
		}

		public boolean isVideo() {
			switch (this) {
			case NETMEETING:
				return true;
			default:
				return false;
			}
		}

		public boolean isVoice() {
			switch (this) {
			case MOBILE:
			case WORK_MOBILE:
			case ASSISTANT:
			case CAR:
			case COMPANY_MAIN:
			case HOME:
			case OTHER:
			case WORK:
			case RADIO:
			case NETMEETING:
				return true;
			default:
				return false;
			}
		}

	}

	enum MailClass {
		BOTH("http://schemas.google.com/g/2005#both"), LETTERS(
				"http://schemas.google.com/g/2005#letters"), PARCELS(
				"http://schemas.google.com/g/2005#parcels"), NEIGHTER(
				"http://schemas.google.com/g/2005#neither");

		static Map<String, MailClass> byGoogle = new TreeMap<String, MailClass>();
		private String googleType = null;

		static {
			for (MailClass value : MailClass.values()) {
				byGoogle.put(value.getGoogleType(), value);

			}
		}

		MailClass(String gData) {
			googleType = gData;
		}

		public static MailClass getByGoogle(String data) {
			if (data == null)
				return BOTH;
			return byGoogle.get(data);
		}

		public String getGoogleType() {
			return googleType;
		}

		public boolean isParcell() {
			switch (this) {
			case PARCELS:
			case BOTH:
				return true;
			default:
				return false;
			}

		}

		public boolean isPostal() {
			switch (this) {
			case LETTERS:
			case BOTH:
				return true;
			default:
				return false;
			}

		}

	};

	enum Usage {
		LOCAL("http://schemas.google.com/g/2005#local"), GENERAL(
				"http://schemas.google.com/g/2005#general");

		static Map<String, Usage> byGoogle = new TreeMap<String, Usage>();
		private String googleType = null;

		static {
			for (Usage value : Usage.values()) {
				byGoogle.put(value.getGoogleType(), value);

			}
		}

		Usage(String gData) {
			googleType = gData;
		}

		public static Usage getByGoogle(String data) {
			if (data == null)
				return GENERAL;
			return byGoogle.get(data);
		}

		public String getGoogleType() {
			return googleType;
		}

		public boolean isLocal() {
			switch (this) {
			case LOCAL:
				return true;
			default:
				return false;
			}

		}

		public boolean isGeneral() {
			switch (this) {
			case GENERAL:
				return true;
			default:
				return false;
			}

		}

	};

	private static final String DEFAULT_FEED = "http://www.google.com/m8/feeds/";
	private static final String DEFAULT_PROJECTION = "full";

	private ContactsService service;
	private ContactModelFactory cmf = Pim.getContactModelFactory();
	private String feedUrlBase;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public Google(String userName, char[] password)
			throws AuthenticationException {
		service = new ContactsService("Google-contactsExampleApp-3");
		service.setUserCredentials(userName, new String(password));
		feedUrlBase = DEFAULT_FEED + "contacts/" + userName + "/"
				+ DEFAULT_PROJECTION;
	}

	public List<ContactEntry> getGoogleContacts(String query)
			throws IOException, ServiceException {

		String url = feedUrlBase + "?"+"max-results=10000"; // + "q=" + URLEncoder.encode(query,
		// "UTF-8");
		URL feedUrl = new URL(url);
		log.debug("Feed URL: " + feedUrl);
		ContactFeed resultFeed = service.getFeed(feedUrl, ContactFeed.class);
		resultFeed.setTotalResults(5000);
		List<ContactEntry> result = resultFeed.getEntries();
		log.debug("Google contacts count: " + result.size() + "/"
				+ resultFeed.getTotalResults());
		return result;

	}

	public List<Contact> getPimContacts(String query) throws IOException,
			ServiceException {
		List<Contact> l = new ArrayList<Contact>();
		for (ContactEntry contact : getGoogleContacts(query)) {
			l.add(createPimContact(contact));
		}
		return l;
	}

	public Contact createPimContact(ContactEntry entry) {
		Contact contact = cmf.createContact();

		contact.setPersonalIdentity(personalIdentityFromGoogle(entry));

		for (Organization org : entry.getOrganizations()) {
			contact
					.setOrganizationalIdentity(organizationalidentityFromGoogle(org));
			break;
		}

		contact
				.setGeographicalInformation(geographicalInformationFRomGoogle(entry));

		contact.setURL(urlFromGoogle(entry));

		for (StructuredPostalAddress address : entry
				.getStructuredPostalAddresses()) {
			contact.addAddress(addressFromGoogle(address));
		}

		for (Category gCat : entry.getCategories()) {
			contact.addCategory(gCat.getLabel());
		}

		contact.setCommunications(comunicationsFromGoogle(entry));
		return contact;
	}

	private String urlFromGoogle(ContactEntry entry) {
		for (Link l : entry.getLinks()) {
			if (GoogleRelations.getByGoogle(l.getRel()) == null) {

			}

		}

		// TODO Auto-generated method stub
		return null;
	}

	private GeographicalInformation geographicalInformationFRomGoogle(
			ContactEntry entry) {

		GeographicalInformation geoinfo = cmf.createGeographicalInformation();
		// TODO Auto-generated method stub
		return geoinfo;
	}

	private OrganizationalIdentity organizationalidentityFromGoogle(
			Organization googleOrg) {
		OrganizationalIdentity org = cmf.createOrganizationalIdentity();
		net.wimpi.pim.contact.model.Organization org3 = cmf
				.createOrganization();
		if (googleOrg.getOrgDepartment() != null)
			org3.addUnit(googleOrg.getOrgDepartment().getValue());
		if (googleOrg.getOrgName() != null)
			org3.setName(googleOrg.getOrgName().getValue());

		org.setOrganization(org3);
		if (googleOrg.getOrgJobDescription() != null)
			org.setRole(googleOrg.getOrgJobDescription().getValue());
		if (googleOrg.getOrgTitle() != null)
			org.setTitle(googleOrg.getOrgTitle().getValue());
		return org;
	}

	private Communications comunicationsFromGoogle(ContactEntry entry) {
		Communications com = cmf.createCommunications();

		for (PhoneNumber pn : entry.getPhoneNumbers()) {
			com.addPhoneNumber(phoneNumberFromGoogle(pn));
		}

		for (Im im : entry.getImAddresses()) {
			com.addPhoneNumber(imFromGoogle(im));
		}

		for (Email email : entry.getEmailAddresses()) {
			com.addEmailAddress(emailFromGoogle(email));
		}

		return com;
	}

	private EmailAddress emailFromGoogle(Email email) {
		EmailAddress eml = cmf.createEmailAddress();
		eml.setAddress(email.getAddress());
		eml.setType(GoogleRelations.getByGoogle(email.getRel()).toString());
		return eml;
	}

	private net.wimpi.pim.contact.model.PhoneNumber imFromGoogle(Im im) {
		net.wimpi.pim.contact.model.PhoneNumber ims = cmf.createPhoneNumber();
		ims.setMessaging(true);
		String proto = "IM";
		if (im.getProtocol() != null && !im.getProtocol().equals("")) {
			proto = im.getProtocol();
		}
		ims.setPreferred(im.getPrimary());
		ims.setNumber(proto + ":" + im.getAddress());
		return ims;
	}

	private net.wimpi.pim.contact.model.PhoneNumber phoneNumberFromGoogle(
			PhoneNumber pn) {
		net.wimpi.pim.contact.model.PhoneNumber phn = cmf.createPhoneNumber();
		phn.setNumber(pn.getPhoneNumber());
		phn.setBBS(GoogleRelations.getByGoogle(pn.getRel()).isBBS());
		phn.setCar(GoogleRelations.getByGoogle(pn.getRel()).isCar());
		phn.setCellular(GoogleRelations.getByGoogle(pn.getRel()).isMobile());
		phn.setFax(GoogleRelations.getByGoogle(pn.getRel()).isFax());
		phn.setHome(GoogleRelations.getByGoogle(pn.getRel()).isHome());
		phn.setISDN(GoogleRelations.getByGoogle(pn.getRel()).isISDN());
		phn.setMessaging(false);
		phn.setMODEM(GoogleRelations.getByGoogle(pn.getRel()).isModem());
		phn.setPager(GoogleRelations.getByGoogle(pn.getRel()).isPager());
		phn.setPCS(GoogleRelations.getByGoogle(pn.getRel()).isPCS());
		phn.setPreferred(pn.getPrimary());
		phn.setVideo(GoogleRelations.getByGoogle(pn.getRel()).isVideo());
		phn.setVoice(GoogleRelations.getByGoogle(pn.getRel()).isVoice());
		phn.setWork(GoogleRelations.getByGoogle(pn.getRel()).isWork());
		return phn;
	}

	private Address addressFromGoogle(StructuredPostalAddress address) {
		Address addr = cmf.createAddress();

		City city = address.getCity();
		if (city != null)
			addr.setCity(city.getValue());

		Country country = address.getCountry();
		if (country != null)
			addr.setCountry(country.getValue());

		HouseName houseName = address.getHousename();
		Neighborhood neighborhood = address.getNeighborhood();

		String houseNameString = null;
		String neighborhoodString = null;

		if (houseName != null)
			houseNameString = houseName.getValue();

		if (neighborhood != null)
			neighborhoodString = neighborhood.getValue();

		addr.setExtended(houseNameString == null ? neighborhoodString
				: houseNameString);

		addr.setLabel(address.getLabel());
		PostCode postCode = address.getPostcode();
		if (postCode != null)
			addr.setPostalCode(postCode.getValue());
		PoBox poBox = address.getPobox();
		if (poBox != null)
			addr.setPostBox(poBox.getValue());
		Region region = address.getRegion();
		if (region != null)
			addr.setRegion(region.getValue());
		Street street = address.getStreet();
		if (street != null)
			addr.setStreet(street.getValue());

		addr.setParcel(MailClass.getByGoogle(address.getMailClass())
				.isParcell());
		addr
				.setPostal(MailClass.getByGoogle(address.getMailClass())
						.isPostal());
		addr.setWork(GoogleRelations.getByGoogle(address.getRel()).isWork());
		addr.setHome(GoogleRelations.getByGoogle(address.getRel()).isHome());

		address.getUsage();

		addr.setDomestic(Usage.getByGoogle(address.getUsage()).isLocal());
		addr
				.setInternational(Usage.getByGoogle(address.getUsage())
						.isGeneral());

		return addr;
	}

	private PersonalIdentity personalIdentityFromGoogle(ContactEntry entry) {
		PersonalIdentity pi = cmf.createPersonalIdentity();

		Name gName = entry.getName();

		if (gName != null) {

			FamilyName familyName = gName.getFamilyName();
			if (familyName != null)
				pi.setLastname(familyName.getValue());
			GivenName givenName = gName.getGivenName();
			if (givenName != null)
				pi.setFirstname(givenName.getValue());
			FullName fullName = gName.getFullName();
			if (fullName != null)
				pi.setFormattedName(fullName.getValue());
			AdditionalName additionalName = gName.getAdditionalName();
			if (additionalName != null)
				pi.addAdditionalName(additionalName.getValue());
			NamePrefix namePrefix = gName.getNamePrefix();
			if (namePrefix != null)
				pi.addPrefix(namePrefix.getValue());
			NameSuffix nameSuffix = gName.getNameSuffix();
			if (nameSuffix != null)
				pi.addSuffix(nameSuffix.getValue());

		}

		Birthday birthday = entry.getBirthday();
		if (birthday != null)
			pi.setBirthDate(new Date(DateTime.parseDate(birthday.getWhen())
					.getValue()));

		try {
			pi.setPhoto(photoFromGoogle(entry));
		} catch (IOException e) {
			log.error("Exception occured.", e);
		} catch (ServiceException e) {
			log.error("Exception occured.", e);
		}

		return pi;
	}

	private Image photoFromGoogle(ContactEntry entry) throws IOException,
			ServiceException {
		Image im = cmf.createImage();

		Link photoLink = entry.getLink(
				"http://schemas.google.com/contacts/2008/rel#photo", "image/*");
		if (photoLink.getEtag() != null) {
			Service.GDataRequest request = service
					.createLinkQueryRequest(photoLink);
			request.execute();
			InputStream in = request.getResponseStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			byte[] buffer = new byte[4096];
			for (int read = 0; (read = in.read(buffer)) != -1; out.write(
					buffer, 0, read)) {
			}
			im.setData(out.toByteArray());
			im.setContentType("image/*");
			return im;
		} else {
			return null;
		}
	}
}

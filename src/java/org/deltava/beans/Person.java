// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.text.*;
import java.security.Principal;

import org.deltava.beans.schedule.Airport;
import org.deltava.util.StringUtils;

/**
 * A class storing information about a Person.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class Person extends DatabaseBlobBean implements Principal, EMailAddress, ViewEntry {

	/**
	 * Notify when new System News entries are created.
	 */
	public static final String NEWS = "NEWS";

	/**
	 * Notify when new Online Event entries are created.
	 */
	public static final String EVENT = "EVENT";

	/**
	 * Notify when new Fleet Library entries are created.
	 */
	public static final String FLEET = "FLEET";

	/**
	 * Notify when Flight Reports Approved.
	 */
	public static final String PIREP = "PIREP";
	
	/**
	 * Notification type codes.
	 */
	public static final String[] NOTIFY_CODES = { Person.NEWS, Person.EVENT, Person.FLEET, Person.PIREP };
	
	/**
	 * Notification type descriptions.
	 */
	public static final String[] NOTIFY_NAMES = { "Send News Notifications", "Send Event Notifications",
			"Send Library Notifications", "Send Flight Approval Notifications" };

	/**
	 * Hide e-mail address from all non-staff users.
	 */
	public static final int HIDE_EMAIL = 0;

	/**
	 * Hide e-mail address from unauthenticated users.
	 */
	public static final int AUTH_EMAIL = 1;

	/**
	 * Show e-mail address to all users.
	 */
	public static final int SHOW_EMAIL = 2;

	private String _firstName;
	private String _lastName;
	private String _password;
	private String _ldapDN;
	private String _email;
	private int _emailAccess;

	private final Map<String, String> _imHandles = new TreeMap<String, String>();

	private String _eqType;
	private String _rank;
	private String _location;
	private String _homeAirport;
	private TZInfo _tz;

	private int _status;

	private Date _created;

	private Date _lastLogin;
	private Date _lastLogoff;
	private int _loginCount;
	private String _loginHost;

	protected final Map<String, String> _networkIDs = new HashMap<String, String>();
	protected final Map<String, Boolean> _notifyOptions = new HashMap<String, Boolean>();

	private double _legacyHours;

	private String _nFormat = "#,##0.0";
	private String _dFormat = "MM/dd/yyyy";
	private String _tFormat = "hh:mm:ss";
	private int _airportCodeType;
	private String _uiScheme;

	/**
	 * Creates a Person object with a given first and last name, converted to "proper case".
	 * @param firstName the Person's first name.
	 * @param lastName the Person's last name.
	 * @throws NullPointerException if either name is null
	 */
	protected Person(String firstName, String lastName) {
		super();
		setFirstName(firstName);
		setLastName(lastName);
		_created = new Date();
	}

	/**
	 * Add a security role to the Person's list of roles.
	 * @param roleName the name of the role
	 */
	public abstract void addRole(String roleName);

	/**
	 * Returns a list of security roles this Person belongs to.
	 * @return a Collection of role names
	 */
	public abstract Collection<String> getRoles();

	/**
	 * Queries if a Person is a member of a particular role.
	 * @param roleName the role name
	 * @return TRUE if the Person is a member of this role, otherwise FALSE
	 */
	public abstract boolean isInRole(String roleName);

	/**
	 * Returns this Person's Full name.
	 * @return the Person's first and last names.
	 */
	public String getName() {
		StringBuilder buf = new StringBuilder(_firstName);
		buf.append(' ');
		buf.append(_lastName);
		return buf.toString();
	}

	/**
	 * Return this Person's First Name.
	 * @return Person's first (given) name.
	 * @see Person#Person(String, String)
	 */
	public String getFirstName() {
		return _firstName;
	}

	/**
	 * Return this Person's Last Name.
	 * @return Person's last (family) name.
	 * @see Person#Person(String, String)
	 */
	public String getLastName() {
		return _lastName;
	}

	/**
	 * Return this Person's LDAP/JNDI Directory Name.
	 * @return Person's unique DN in the repository. If this is null then the Person does not have a JNDI entry.
	 * @see Person#setDN(String)
	 */
	public String getDN() {
		return _ldapDN;
	}

	/**
	 * Return the Person's password.
	 * @return the unencrypted password. This will only be non-null when changing a password or registering a new
	 *         Person.
	 * @see Person#setPassword(String)
	 */
	public String getPassword() {
		return _password;
	}

	/**
	 * Return the Person's equipment type program. In the case of an Applicant not yet hired, this will be null.
	 * @return the equipment type, or null
	 * @see Person#setEquipmentType(String)
	 */
	public String getEquipmentType() {
		return _eqType;
	}

	/**
	 * Return the Person's e-mail address.
	 * @return the e-mail address
	 * @see Person#setEmail(String)
	 * @see Person#getEmailAccess()
	 */
	public String getEmail() {
		return _email;
	}

	/**
	 * Returns the access to a Person's e-mail address.
	 * @return the access level
	 * @see Person#getEmail()
	 */
	public int getEmailAccess() {
		return _emailAccess;
	}

	/**
	 * Return the Person's rank. In the case of an Applicant not yet hired this will be null.
	 * @return the rank, or null
	 * @see Person#setRank(String)
	 */
	public String getRank() {
		return _rank;
	}

	/**
	 * Return this Person's status. It is up to each implementation to detrmine valid values for this property.
	 * @return the Person's status code
	 * @see Person#getStatusName()
	 * @see Person#setStatus(int)
	 */
	public int getStatus() {
		return _status;
	}
	
	/**
	 * Returns the Person's status.
	 * @return the status name
	 * @see Person#getStatus()
	 * @see Person#setStatus(int)
	 */
	public abstract String getStatusName();

	/**
	 * Return this Person's Time Zone.
	 * @return the time zone where this Person is located
	 * @see Person#setTZ(TZInfo)
	 */
	public TZInfo getTZ() {
		return _tz;
	}

	/**
	 * Return this Person's legacy flight hours.
	 * @return Person's Legacy flight hours
	 * @see Person#setLegacyHours(double)
	 */
	public double getLegacyHours() {
		return _legacyHours;
	}

	/**
	 * Return the Person's location.
	 * @return the part of the world this Person lives in
	 * @see Person#setLocation(String)
	 */
	public String getLocation() {
		return _location;
	}

	/**
	 * Returns the Person's Instant Messenger handle.
	 * @param service the IM service name
	 * @return the IM handle, or null if not found
	 * @throws NullPointerException if service is null
	 * @see Person#getIMServices()
	 * @see Person#getIMHandle()
	 * @see Person#setIMHandle(String, String)
	 */
	public String getIMHandle(String service) {
		return _imHandles.get(service.toUpperCase());
	}
	
	/**
	 * Returns a Map containing this Person's Instant Messenger handles.
	 * @return the Instant Messenger IDs
	 * @see Person#getIMHandle(String)
	 */
	public Map<String, String> getIMHandle() {
		return new HashMap<String, String>(_imHandles);
	}

	/**
	 * Returns the Person's registed Instant Messenger services.
	 * @return a Collection of service names
	 * @see Person#getIMHandle(String)
	 * @see Person#setIMHandle(String, String)
	 */
	public Collection<String> getIMServices() {
		return new LinkedHashSet<String>(_imHandles.keySet());
	}

	/**
	 * Return the Person's home airport code.
	 * @return the closest Airport code this this Person''s location. This is self-selected.
	 * @see Person#setHomeAirport(String)
	 */
	public String getHomeAirport() {
		return _homeAirport;
	}

	/**
	 * Retun the Person's online network IDs.
	 * @return the network user IDs
	 * @see Person#setNetworkID(String, String)
	 */
	public Map<String, String> getNetworkIDs() {
		return new HashMap<String, String>(_networkIDs);
	}

	/**
	 * Return a particular e-mail notification option for this Person.
	 * @param notifyType the NotificationType, use constants if possible
	 * @return TRUE if this Person should be notified
	 * @see Person#setNotifyOption(String, boolean)
	 * @see Person#getNotifyOptions()
	 */
	public boolean getNotifyOption(String notifyType) {
		Boolean notify = _notifyOptions.get(notifyType);
		return (notify == null) ? false : notify.booleanValue();
	}

	/**
	 * Returns all selected notification options.
	 * @return a List of notification types
	 * @see Person#setNotifyOption(String, boolean)
	 * @see Person#getNotifyOption(String)
	 */
	public Collection<String> getNotifyOptions() {
		Collection<String> results = new HashSet<String>();
		for (Iterator<String> i = _notifyOptions.keySet().iterator(); i.hasNext();) {
			String optName = i.next();
			Boolean isSet = _notifyOptions.get(optName);
			if (isSet.booleanValue())
				results.add(optName);
		}

		return results;
	}

	/**
	 * Return the number of times this Person has logged into the system.
	 * @return the number of times this person has logged in
	 * @see Person#setLoginCount(int)
	 */
	public int getLoginCount() {
		return _loginCount;
	}

	/**
	 * Return the hostname or IP address that this Person last logged in from.
	 * @return the host name or IP address
	 * @see Person#setLoginHost(String)
	 */
	public String getLoginHost() {
		return _loginHost;
	}

	/**
	 * Return the date/time this Person registered.
	 * @return the date/time the person was registered
	 * @see Person#setCreatedOn(Date)
	 */
	public Date getCreatedOn() {
		return _created;
	}

	/**
	 * Return the Person's latest login date/time.
	 * @return the date/time this person last logged into the system
	 * @see Person#setLastLogin(Date)
	 * @see Person#getLastLogoff()
	 */
	public Date getLastLogin() {
		return _lastLogin;
	}

	/**
	 * Return the Person's last logoff date/time.
	 * @return the date/time this person last logged off the system
	 * 
	 */
	public Date getLastLogoff() {
		return _lastLogoff;
	}

	/**
	 * Returns the Person's preferred date format pattern.
	 * @return the date format pattern
	 * @see Person#setDateFormat(String)
	 * @see java.text.SimpleDateFormat#applyPattern(String)
	 */
	public String getDateFormat() {
		return _dFormat;
	}

	/**
	 * Returns the Person's preferred time format pattern.
	 * @return the time format pattern
	 * @see Person#setTimeFormat(String)
	 * @see java.text.SimpleDateFormat#applyPattern(String)
	 */
	public String getTimeFormat() {
		return _tFormat;
	}

	/**
	 * Returns the Person's preferred number format pattern.
	 * @return the number format pattern
	 * @see Person#setNumberFormat(String)
	 * @see java.text.DecimalFormat#applyPattern(String)
	 */
	public String getNumberFormat() {
		return _nFormat;
	}

	/**
	 * Returns the Person's preferred web site UI scheme.
	 * @return the web site UI scheme name
	 * @see Person#setUIScheme(String)
	 */
	public String getUIScheme() {
		return _uiScheme;
	}

	/**
	 * Returns the Person's preferred airport code type (IATA/ICAO).
	 * @return the Airport Code type
	 * @see Person#setAirportCodeType(int)
	 * @see Person#setAirportCodeType(String)
	 * @see Person#getAirportCodeTypeName()
	 */
	public int getAirportCodeType() {
		return _airportCodeType;
	}

	/**
	 * Returns the Person's preferred airport code type name (IATA/ICAO).
	 * @return the Airport Code type name
	 * @see Person#setAirportCodeType(int)
	 * @see Person#setAirportCodeType(String)
	 * @see Person#getAirportCodeType()
	 */
	public String getAirportCodeTypeName() {
		return Airport.CODETYPES[_airportCodeType];
	}

	/**
	 * Update the Person's directory name. Setting a non-null value implies the Person is in the directory.
	 * @param dn the full directory name
	 * @see Person#getDN()
	 */
	public void setDN(String dn) {
		_ldapDN = dn;
	}

	/**
	 * Updates the Person's first (given) name.
	 * @param name the given name
	 * @throws NullPointerException if name is null
	 */
	public void setFirstName(String name) {
		_firstName = StringUtils.strip(name.trim(), ",");
	}

	/**
	 * Updates the Person's last (family) name.
	 * @param name the family name
	 * @throws NullPointerException if name is null
	 */
	public void setLastName(String name) {
		_lastName = StringUtils.strip(name.trim(), ",");
	}

	/**
	 * Update the Person's network ID for a given online network.
	 * @param network the network name
	 * @param id the network ID
	 * @throws NullPointerException if the network name is null
	 * @see Person#getNetworkIDs()
	 */
	public void setNetworkID(String network, String id) {
		if (network == null)
			throw new NullPointerException("Network ID cannot be null");

		if (!StringUtils.isEmpty(id)) {
			StringBuilder buf = new StringBuilder();
			for (int x = 0; x < id.length(); x++) {
				char c = id.charAt(x);
				if (Character.isDigit(c))
					buf.append(c);
			}
			
			if (buf.length() > 5)
				_networkIDs.put(network, buf.toString());
		}
	}

	/**
	 * Update this Person's notification option for a given notificaiton type.
	 * @param option the notification type. Use constants if possible.
	 * @param notify TRUE if this person should be notified
	 * @throws NullPointerException if option is null
	 * @see Person#getNotifyOption(String)
	 * @see Person#getNotifyOptions()
	 */
	public void setNotifyOption(String option, boolean notify) {
		if (option == null)
			throw new NullPointerException("Notify Option cannot be null");

		_notifyOptions.put(option, Boolean.valueOf(notify));
	}

	/**
	 * Update the Person's password.
	 * @param pwd the new password
	 * @see Person#getPassword()
	 */
	public void setPassword(String pwd) {
		_password = pwd;
	}

	/**
	 * Update the Person's Equipment Type.
	 * @param eqType the equipment type name, or null if not hired
	 * @see Person#getEquipmentType()
	 */
	public void setEquipmentType(String eqType) {
		_eqType = eqType;
	}

	/**
	 * Update the Person's e-mail address.
	 * @param email the e-mail address
	 * @see Person#getEmail()
	 * @see Person#setEmailAccess(int)
	 */
	public void setEmail(String email) {
		_email = email;
	}

	/**
	 * Update the Person's e-mail address acess level.
	 * @param accessLevel the access level
	 * @throws IllegalArgumentException if the access level is negative or invalid
	 * @see Person#getEmailAccess()
	 * @see Person#setEmail(String)
	 */
	public void setEmailAccess(int accessLevel) {
		if ((accessLevel < 0) || (accessLevel > 2))
			throw new IllegalArgumentException("Invalid E-Maill Address access level - " + accessLevel);

		_emailAccess = accessLevel;
	}

	/**
	 * Update the person's Rank.
	 * @param rank the new Rank, or null if not hired
	 * @see Person#getRank()
	 */
	public void setRank(String rank) {
		_rank = rank;
	}

	/**
	 * Sets the Time Zone for this Person.
	 * @param tz the time zone where this Person is located
	 * @see Person#getTZ()
	 */
	public void setTZ(TZInfo tz) {
		_tz = tz;
	}

	/**
	 * Update this Person's status.
	 * @param status the new status code. It is up to each implementation to validate this beyond the < 0 check.
	 * @throws IllegalArgumentException if the new status is negative.
	 * @see Person#getStatus()
	 */
	protected void setStatus(int status) {
		if (status < 0)
			throw new IllegalArgumentException("Status cannot be negative");

		_status = status;
	}

	/**
	 * Update this Person's legacy flight hours.
	 * @param legacyHours the Legacy Flight hours.
	 * @throws IllegalArgumentException if the new hours are negative.
	 * @see Person#getLegacyHours()
	 */
	public void setLegacyHours(double legacyHours) {
		if (legacyHours < 0)
			throw new IllegalArgumentException("Legacy Hours cannot be negative");

		_legacyHours = legacyHours;
	}

	/**
	 * Update this Person's home airport
	 * @param aCode this person's IATA home airport code.
	 * @see Person#getHomeAirport()
	 */
	public void setHomeAirport(String aCode) {
		_homeAirport = (aCode == null) ? null : aCode.trim().toUpperCase();
	}

	/**
	 * Update this Person's location.
	 * @param location the location where this Person lives. It is up to the implementation as to what are valid
	 *            choices.
	 * @see Person#getLocation()
	 */
	public void setLocation(String location) {
		_location = location;
	}

	/**
	 * Update this Person's Instant Messaging handle. If the handle is empty, it will be cleared.
	 * @param service the messaging service
	 * @param handle the new handle
	 * @throws NullPointerException if service is null
	 * @see Person#getIMHandle(String)
	 * @see Person#getIMServices()
	 */
	public void setIMHandle(String service, String handle) {
		String svc = service.toUpperCase();
		if ((handle != null) && (!StringUtils.isEmpty(handle.trim())))
			_imHandles.put(svc, handle);
		else if (_imHandles.containsKey(svc))
			_imHandles.remove(svc);
	}

	/**
	 * Update this person's login count
	 * @param count the new login count.
	 * @throws IllegalArgumentException if the new login count is negative.
	 * @see Person#setLoginCount(int)
	 */
	public void setLoginCount(int count) {
		if (count < 0)
			throw new IllegalArgumentException("LoginCount cannot be negative");

		_loginCount = count;
	}

	/**
	 * Update this Person's createdOn date/time
	 * @param cd the Date/Time when the Person was created
	 * @see Person#getCreatedOn()
	 */
	public void setCreatedOn(Date cd) {
		_created = cd;
	}

	/**
	 * Update this Person's last Login date/time.
	 * @param lld the Date/Time when the Person last logged in
	 * @throws IllegalStateException if the timestamp is less than getCreatedOn()
	 * @see Person#getLastLogin()
	 * @see Person#setLastLogoff(Date)
	 */
	public void setLastLogin(Date lld) {
		if (lld == null)
			return;

		if (lld.getTime() > System.currentTimeMillis()) {
			throw new IllegalArgumentException("Last Login Date cannot be in the future");
		} else if (lld.before(getCreatedOn())) {
			throw new IllegalStateException("Last Login Date cannot be < Created Date");
		} else {
			_lastLogin = lld;
		}
	}

	/**
	 * Update the Person's last Logoff date/time.
	 * @param lld the Date/Time when the Person last logged out or had the session invalidated
	 * @throws IllegalStateException if the timestamp is less than getCreatedOn()
	 * @see Person#getLastLogoff()
	 * @see Person#setLastLogin(Date)
	 */
	public void setLastLogoff(Date lld) {
		if (lld == null)
			return;

		if (lld.getTime() > System.currentTimeMillis()) {
			throw new IllegalArgumentException("Last Logoff Date cannot be in the future");
		} else if (lld.before(getCreatedOn())) {
			throw new IllegalStateException("Last Logoff Date cannot be < Created Date");
		} else {
			_lastLogoff = lld;
		}
	}

	/**
	 * Updates the hostname or IP address that this Person last logged in from
	 * @param hostName the hostname or IP address
	 * @see Person#getLoginHost()
	 */
	public void setLoginHost(String hostName) {
		_loginHost = hostName;
	}

	/**
	 * Updates the Person's preferred date format.
	 * @param pattern the date format pattern
	 * @see Person#getDateFormat()
	 */
	public void setDateFormat(String pattern) {
		try {
			pattern = pattern.replace('m', 'M');
			DateFormat df = new SimpleDateFormat(pattern);
			if (!pattern.equals(df.format(new Date())))
				_dFormat = pattern;
		} catch (Exception e) {
			//empty
		}
	}

	/**
	 * Updates the Person's preferred time format.
	 * @param pattern the time format pattern
	 * @see Person#getTimeFormat()
	 */
	public void setTimeFormat(String pattern) {
		try {
			pattern = pattern.replace('M', 'm');
			DateFormat df = new SimpleDateFormat(pattern);
			if (!pattern.equals(df.format(new Date())))
				_tFormat = pattern;
		} catch (Exception e) {
			//empty
		}
	}

	/**
	 * Updates the Person's preferred number format.
	 * @param pattern the number format pattern
	 * @see Person#getNumberFormat()
	 */
	public void setNumberFormat(String pattern) {
		try {
			DecimalFormat nf = new DecimalFormat(pattern);
			_nFormat = nf.toPattern();
		} catch (Exception e) {
			// empty
		}
	}

	/**
	 * Updates this Person's preferred web site UI scheme.
	 * @param schemeName the scheme name
	 * @see Person#getUIScheme()
	 */
	public void setUIScheme(String schemeName) {
		_uiScheme = schemeName;
	}

	/**
	 * Updates the Person's preferred airport code type (IATA/ICAO).
	 * @param code the Airport code type code
	 * @see Person#setAirportCodeType(String)
	 * @see Person#getAirportCodeType()
	 * @see Person#getAirportCodeTypeName()
	 */
	public void setAirportCodeType(int code) {
		if ((code < 0) || (code >= Airport.CODETYPES.length))
			throw new IllegalArgumentException("Invalid Airport Code type -" + code);

		_airportCodeType = code;
	}

	/**
	 * Updates the Person's preferred airport code type (IATA/ICAO).
	 * @param codeName the Airport code type name
	 * @see Person#setAirportCodeType(int)
	 * @see Person#getAirportCodeType()
	 * @see Person#getAirportCodeTypeName()
	 */
	public void setAirportCodeType(String codeName) {
		setAirportCodeType(StringUtils.arrayIndexOf(Airport.CODETYPES, codeName, 0));
	}
}
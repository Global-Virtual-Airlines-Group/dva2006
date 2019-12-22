// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2012, 2013, 2014 2015, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.security.Principal;
import java.text.DecimalFormat;

import org.deltava.beans.schedule.Airport;
import org.deltava.util.StringUtils;

/**
 * An abstract class storing information about a Person.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public abstract class Person extends DatabaseBlobBean implements Principal, FormattedEMailRecipient, ViewEntry {

	private static final long serialVersionUID = -7815761435601664719L;

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
	private boolean _eMailInvalid;

	private final Map<IMAddress, String> _imHandles = new TreeMap<IMAddress, String>();

	private String _eqType;
	private Rank _rank;
	private String _location;
	private String _homeAirport;
	private TZInfo _tz;

	private Instant _created = Instant.now();

	private Instant _lastLogin;
	private Instant _lastLogoff;
	private int _loginCount;
	private String _loginHost;

	protected final Map<OnlineNetwork, String> _networkIDs = new HashMap<OnlineNetwork, String>();
	protected final Collection<Notification> _notifyOptions = new HashSet<Notification>();

	private double _legacyHours;

	private String _nFormat = "#,##0.0";
	private String _dFormat = "MM/dd/yyyy";
	private String _tFormat = "hh:mm:ss";
	private Airport.Code _airportCodeType;
	private DistanceUnit _distanceType;
	private WeightUnit _weightType;
	private String _uiScheme;
	private int _viewCount;

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
	@Override
	public String getName() {
		StringBuilder buf = new StringBuilder(_firstName).append(' ');
		return buf.append(_lastName).toString();
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
	@Override
	public String getEmail() {
		return _email;
	}
	
	@Override
	public boolean isInvalid() {
		return _eMailInvalid;
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
	 * Returns this person's e-mail domain.
	 * @return the domain part of the e-mail address, or null if no address.
	 */
	public String getEmailDomain() {
		if (_email == null)
			return null;
		
		return _email.contains("@") ? _email.substring(_email.indexOf('@') + 1) : null;
	}

	/**
	 * Return the Person's rank. In the case of an Applicant not yet hired this will be null.
	 * @return the rank, or null
	 * @see Person#setRank(Rank)
	 */
	public Rank getRank() {
		return _rank;
	}

	/**
	 * Return this Person's Time Zone.
	 * @return the time zone where this Person is located
	 * @see Person#setTZ(TZInfo)
	 */
	@Override
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
	 * @see Person#getIMHandle()
	 * @see Person#setIMHandle(IMAddress, String)
	 */
	public String getIMHandle(IMAddress service) {
		return _imHandles.get(service);
	}
	
	/**
	 * Returns a Map containing this Person's Instant Messenger handles.
	 * @return the Instant Messenger IDs
	 * @see Person#getIMHandle(IMAddress)
	 */
	public Map<IMAddress, String> getIMHandle() {
		return new TreeMap<IMAddress, String>(_imHandles);
	}
	
	/**
	 * Returns whether a Person has a specific Instant Messenger handle.
	 * @param svc an IMAddress object
	 * @return TRUE if the Person has an address for this service, otherwise FALSE
	 */
	public boolean hasIM(IMAddress svc) {
		return _imHandles.containsKey(svc);
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
	 * Returns the Online Networks this used has provided an ID for.
	 * @return the networks
	 */
	public Collection<OnlineNetwork> getNetworks() {
		return _networkIDs.keySet();
	}
	
	/**
	 * Retuns if this Person is a memebr of an online network.
	 * @param net the OnlineNetwork
	 * @return TRUE if the user has a network ID, otherwise false
	 */
	public boolean hasNetworkID(OnlineNetwork net) {
		return _networkIDs.containsKey(net);
	}
	
	/**
	 * Returns this Person's online network ID
	 * @param net the OnlineNetwork
	 * @return the network ID
	 * @see Person#setNetworkID(OnlineNetwork, CharSequence)
	 */
	public String getNetworkID(OnlineNetwork net) {
		return _networkIDs.get(net);
	}

	/**
	 * Return a particular e-mail notification option for this Person.
	 * @param notifyType the NotificationType, use constants if possible
	 * @return TRUE if this Person should be notified
	 * @see Person#setNotifyOption(Notification, boolean)
	 * @see Person#getNotifyOptions()
	 */
	public boolean hasNotifyOption(Notification notifyType) {
		return _notifyOptions.contains(notifyType);
	}

	/**
	 * Returns all selected notification options.
	 * @return a List of notification types
	 * @see Person#setNotifyOption(Notification, boolean)
	 * @see Person#hasNotifyOption(Notification)
	 */
	public Collection<Notification> getNotifyOptions() {
		return new TreeSet<Notification>(_notifyOptions);
	}
	
	/**
	 * Returns the aggregated notification code.
	 * @return the notification code.
	 */
	public int getNotifyCode() {
		int code = 0;
		for (Notification n : _notifyOptions)
			code |= n.getCode();
		
		return code;
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
	 * @see Person#setCreatedOn(Instant)
	 */
	public Instant getCreatedOn() {
		return _created;
	}

	/**
	 * Return the Person's latest login date/time.
	 * @return the date/time this person last logged into the system
	 * @see Person#setLastLogin(Instant)
	 * @see Person#getLastLogoff()
	 */
	public Instant getLastLogin() {
		return _lastLogin;
	}

	/**
	 * Return the Person's last logoff date/time.
	 * @return the date/time this person last logged off the system
	 */
	public Instant getLastLogoff() {
		return _lastLogoff;
	}

	/**
	 * Returns the Person's preferred date format pattern.
	 * @return the date format pattern
	 * @see Person#setDateFormat(String)
	 * @see java.text.SimpleDateFormat#applyPattern(String)
	 */
	@Override
	public String getDateFormat() {
		return _dFormat;
	}

	/**
	 * Returns the Person's preferred time format pattern.
	 * @return the time format pattern
	 * @see Person#setTimeFormat(String)
	 * @see java.text.SimpleDateFormat#applyPattern(String)
	 */
	@Override
	public String getTimeFormat() {
		return _tFormat;
	}

	/**
	 * Returns the Person's preferred number format pattern.
	 * @return the number format pattern
	 * @see Person#setNumberFormat(String)
	 * @see java.text.DecimalFormat#applyPattern(String)
	 */
	@Override
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
	 * Returns the Person's preferred view window size.
	 * @return the view size in rows
	 * @see Person#setViewCount(int)
	 */
	public int getViewCount() {
		return _viewCount;
	}
	
	/**
	 * Returns the Person's preferred distance unit.
	 * @return the unit type
	 * @see Person#setDistanceType(DistanceUnit)
	 */
	@Override
	public DistanceUnit getDistanceType() {
		return _distanceType;
	}
	
	/**
	 * Returns the Person's preferred weight unit.
	 * @return the unit type
	 * @see Person#setWeightType(WeightUnit)
	 */
	public WeightUnit getWeightType() {
		return _weightType;
	}
	
	/**
	 * Returns the Person's preferred airport code type (IATA/ICAO).
	 * @return the Airport Code type
	 * @see Person#setAirportCodeType(Airport.Code)
	 */
	@Override
	public Airport.Code getAirportCodeType() {
		return _airportCodeType;
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
	 * @param network the network
	 * @param id the network ID
	 * @see Person#getNetworks()
	 * @see Person#getNetworkID(OnlineNetwork)
	 */
	public void setNetworkID(OnlineNetwork network, CharSequence id) {
		if (!StringUtils.isEmpty(id)) {
			StringBuilder buf = new StringBuilder();
			for (int x = 0; x < id.length(); x++) {
				char c = id.charAt(x);
				if (Character.isDigit(c))
					buf.append(c);
			}
			
			if (buf.length() > ((network == OnlineNetwork.PILOTEDGE) ? 3 : 5))
				_networkIDs.put(network, buf.toString());
		} else
			_networkIDs.remove(network);
	}

	/**
	 * Update this Person's notification option for a given notificaiton type.
	 * @param option the notification type
	 * @param notify TRUE if this person should be notified
	 * @see Person#hasNotifyOption(Notification)
	 * @see Person#getNotifyOptions()
	 */
	public void setNotifyOption(Notification option, boolean notify) {
		if (notify)
			_notifyOptions.add(option);
		else
			_notifyOptions.remove(option);
	}
	
	/**
	 * Sets the notification options for a user from a single bitmap value.
	 * @param code the bitmap value
	 * @see Person#getNotifyCode()
	 */
	public void setNotificationCode(int code) {
		_notifyOptions.clear();
		for (Notification n : Notification.values()) {
			if ((code & n.getCode()) > 0)
				_notifyOptions.add(n);
		}
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
	 * Marks the email address as invalid.
	 * @param isInvalid TRUE if invalid, otherwise FALSE
	 * @see Person#isInvalid()
	 */
	public void setEmailInvalid(boolean isInvalid) {
		_eMailInvalid = isInvalid;
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
	public void setRank(Rank rank) {
		_rank = rank;
	}

	/**
	 * Sets the Time Zone for this Person.
	 * @param tz the time zone where this Person is located
	 * @see Person#getTZ()
	 */
	public void setTZ(TZInfo tz) {
		_tz = (tz == null) ? TZInfo.UTC : tz;
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
	 * @param svc the messaging service
	 * @param handle the new handle
	 * @throws NullPointerException if service is null
	 * @see Person#getIMHandle(IMAddress)
	 */
	public void setIMHandle(IMAddress svc, String handle) {
		if ((handle != null) && (!StringUtils.isEmpty(handle.trim())))
			_imHandles.put(svc, handle);
		else if (_imHandles.containsKey(svc))
			_imHandles.remove(svc);
	}

	/**
	 * Update this person's login count
	 * @param count the new login count.
	 * @see Person#setLoginCount(int)
	 */
	public void setLoginCount(int count) {
		_loginCount = Math.max(0, count);
	}

	/**
	 * Update this Person's createdOn date/time
	 * @param cd the Date/Time when the Person was created
	 * @see Person#getCreatedOn()
	 */
	public void setCreatedOn(Instant cd) {
		_created = cd;
	}

	/**
	 * Update this Person's last Login date/time.
	 * @param lld the Date/Time when the Person last logged in
	 * @throws IllegalStateException if the timestamp is less than getCreatedOn()
	 * @see Person#getLastLogin()
	 * @see Person#setLastLogoff(Instant)
	 */
	public void setLastLogin(Instant lld) {
		if (lld == null)
			return;
		else if (lld.isBefore(getCreatedOn()))
			throw new IllegalStateException("Last Login Date cannot be < Created Date");
		else
			_lastLogin = lld;
	}

	/**
	 * Update the Person's last Logoff date/time.
	 * @param lld the Date/Time when the Person last logged out or had the session invalidated
	 * @throws IllegalStateException if the timestamp is less than getCreatedOn()
	 * @see Person#getLastLogoff()
	 * @see Person#setLastLogin(Instant)
	 */
	public void setLastLogoff(Instant lld) {
		if (lld == null)
			return;
		else if (lld.isBefore(getCreatedOn()))
			throw new IllegalStateException("Last Logoff Date cannot be < Created Date");
		else
			_lastLogoff = lld;
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
			String p2 = pattern.replace('m', 'M').replace('D', 'd');
			DateTimeFormatter df = DateTimeFormatter.ofPattern(p2);
			if (!p2.equals(df.format(LocalDateTime.now())))
				_dFormat = p2;
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
			String p2 = pattern.replace('M', 'm').replace('S', 's');
			DateTimeFormatter df = DateTimeFormatter.ofPattern(p2);
			if (!p2.equals(df.format(LocalDateTime.now())))
				_tFormat = p2;
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
			_nFormat = new DecimalFormat(pattern).toPattern();
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
	 * Sets the size of the Person's preferred view window.
	 * @param size the size in rows
	 * @see Person#getViewCount()
	 */
	public void setViewCount(int size) {
		_viewCount = Math.min(500, Math.max(20, size));
	}
	
	/**
	 * Updates the Person's preferred distance units.
	 * @param un the DistanceUnit
	 * @see Person#getDistanceType()
	 */
	public void setDistanceType(DistanceUnit un) {
		_distanceType = un;
	}
	
	/**
	 * Updates the Person's preferred weight units.
	 * @param un the WeightUnit
	 * @see Person#getWeightType()
	 */
	public void setWeightType(WeightUnit un) {
		_weightType = un;
	}
	
	/**
	 * Updates the Person's preferred airport code type (IATA/ICAO).
	 * @param code the Airport code type
	 * @see Person#getAirportCodeType()
	 */
	public void setAirportCodeType(Airport.Code code) {
		_airportCodeType = code;
	}
	
	/**
	 * Returns the person's full name.
	 */
	@Override
	public String toString() {
		return getName();
	}
}
// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;
import java.text.DecimalFormat;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.Cacheable;

/**
 * A class for storing Pilot entries.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class Pilot extends Person implements Cacheable, ComboAlias {

	/**
	 * Pilot status codes.
	 */
	public static final int ACTIVE = 0;
	public static final int INACTIVE = 1;
	public static final int RETIRED = 2;
	public static final int TRANSFERRED = 3;
	public static final int SUSPENDED = 4;
	public static final int ON_LEAVE = 5;

	/**
	 * Valid pilot statuses.
	 */
	public static final String[] STATUS = { "Active", "Inactive", "Retired", "Transferred", "Suspended", "On Leave" };

	/**
	 * ACARS restriction codes.
	 */
	public static final int ACARS_ONLY = 4;
	public static final int ACARS_BLOCK = 3;
	public static final int ACARS_NOMSGS = 2;
	public static final int ACARS_RESTRICT = 1;
	public static final int ACARS_OK = 0;

	/**
	 * Valid ACARS restrictions.
	 */
	public static final String[] RESTRICT = { "Unlimited Usage", "Restricted Messaging", "Flight Reports Only",
			"Blocked", "No Manual Flight Reports" };

	/**
	 * Valid route mapping types.
	 */
	public static final String[] MAP_TYPES = { "Google Maps", "Falling Rain" };

	public static final int MAP_GOOGLE = 0;
	public static final int MAP_FALLINGRAIN = 1;

	private static final String[] ROW_CLASSES = { null, "opt2", "opt3", "opt1", "err", "warn" };

	private String _pCodePrefix;
	private int _pCodeId;

	private String _ldapID;

	private final Set<String> _ratings = new TreeSet<String>();
	private final Set<String> _roles = new TreeSet<String>();
	private final Set<String> _certs = new LinkedHashSet<String>();

	private long _miles;
	private Date _lastFlight;

	private String _motto;

	private int _legs;
	private int _acarsLegs = -1; // Set to -1 which is uninitialized
	private int _onlineLegs;
	private int _totalLegs;
	private double _hours;
	private double _acarsHours;
	private double _onlineHours;
	private double _totalHours;

	private boolean _showSigs;
	private boolean _showSSThreads;
	private boolean _showDefaultSignature;
	private boolean _noExams;
	private boolean _noVoice;
	private boolean _noCooler;

	private int _ACARSRestrict;
	private int _mapType;
	private String _sigExt;

	private final DecimalFormat _df = new DecimalFormat("##000");

	/**
	 * Creates a Pilot object with a given first and last name, converted to "proper case".
	 * @param firstName the Pilot's first name.
	 * @param lastName the Pilot's last name.
	 * @throws NullPointerException if either name is null
	 * @see org.deltava.util.StringUtils#properCase(String)
	 */
	public Pilot(String firstName, String lastName) {
		super(firstName, lastName);
		_roles.add("Pilot");
	}

	/**
	 * Returns the list of security roles this Pilot belongs to.
	 * @return a sorted List of role Names for this Pilot
	 */
	public Collection<String> getRoles() {
		return _roles;
	}

	/**
	 * Returns the airline code for this Pilot.
	 * @return the airline code
	 */
	public String getAirlineCode() {
		return _pCodePrefix;
	}

	/**
	 * Return the Pilot Code.
	 * @return the pilot code eg. DVA043. If the pilot has not logged any flights this will return an empty stirng ("").
	 * @see Pilot#setPilotCode(CharSequence)
	 */
	public String getPilotCode() {
		return (_pCodeId == 0) ? "" : _pCodePrefix + String.valueOf(_df.format(_pCodeId));
	}

	/**
	 * Return the pilot number.
	 * @return the pilot number eg. 43. If the pilot has not logged any flights this will return 0.
	 * @see Pilot#setPilotCode(CharSequence)
	 */
	public int getPilotNumber() {
		return _pCodeId;
	}

	public String getComboName() {
		return getName();
	}

	public String getComboAlias() {
		return String.valueOf(getID());
	}

	/**
	 * Returns wether the pilot will see Water Cooler signatures.
	 * @return TRUE if the Pilot views signature images, otherwise FALSE
	 * @see Pilot#setShowSignatures(boolean)
	 */
	public boolean getShowSignatures() {
		return _showSigs;
	}

	/**
	 * Returns wether the Pilot will see Water Cooler screen shot message threads.
	 * @return TRUE if the Pilot sees screen shot threads, otherwise FALSE
	 * @see Pilot#setShowSSThreads(boolean)
	 */
	public boolean getShowSSThreads() {
		return _showSSThreads;
	}

	/**
	 * Returns the Pilot's ACARS restrictions.
	 * @return an ACARS restriction code
	 * @see Pilot#getACARSRestrictionName()
	 * @see Pilot#setACARSRestriction(int)
	 */
	public int getACARSRestriction() {
		return _ACARSRestrict;
	}

	/**
	 * Returns the Pilot's ACARS restriction name.
	 * @return an ACARS restriction name
	 * @see Pilot#getACARSRestriction()
	 * @see Pilot#setACARSRestriction(int)
	 */
	public String getACARSRestrictionName() {
		return RESTRICT[_ACARSRestrict];
	}

	/**
	 * Returns wither the Pilot has been locked out of the Testing Center.
	 * @return TRUE if the Pilot cannot take any Examinations, otherwise FALSE
	 * @see Pilot#setNoExams(boolean)
	 */
	public boolean getNoExams() {
		return _noExams;
	}

	/**
	 * Returns wether the Pilot has been locked out of the Voice server.
	 * @return TRUE if the Pilot has been locked out of the Voice server, otherwise FALSE
	 * @see Pilot#setNoVoice(boolean)
	 */
	public boolean getNoVoice() {
		return _noVoice;
	}
	
	/**
	 * Returns wether the Pilot has been locked out of the Water Cooler.
	 * @return TRUE if the Pilot has been locked out of the Water Cooler, otherwise FALSE
	 * @see Pilot#setNoCooler(boolean)
	 */
	public boolean getNoCooler() {
		return _noCooler;
	}

	/**
	 * Returns the Pilot's status text for a JSP.
	 * @return the status name
	 * @see Person#getStatus()
	 * @see Pilot#setStatus(int)
	 * @see Pilot#setStatus(String)
	 */
	public String getStatusName() {
		return Pilot.STATUS[getStatus()];
	}

	/**
	 * Returns the Pilot's motto
	 * @return the motto text
	 * @see Pilot#setMotto(String)
	 */
	public String getMotto() {
		return _motto;
	}

	/**
	 * Returns the Pilot's preferred route map type.
	 * @return the map type code.
	 * @see Pilot#getMapTypeName()
	 * @see Pilot#setMapType(int)
	 * @see Pilot#setMapType(String)
	 */
	public int getMapType() {
		return _mapType;
	}

	/**
	 * Returns the Pilot's preferred route map type name.
	 * @return the map type name
	 * @see Pilot#getMapType()
	 * @see Pilot#setMapType(int)
	 * @see Pilot#setMapType(String)
	 */
	public String getMapTypeName() {
		return Pilot.MAP_TYPES[getMapType()];
	}

	/**
	 * Return the Pilot's rated equipment Types.
	 * @return a sorted list of the equipment types the Pilot is rated to fly.
	 * @see Pilot#addRating(String)
	 * @see Pilot#addRatings(Collection)
	 * @see Pilot#removeRatings(Collection)
	 */
	public Collection<String> getRatings() {
		return _ratings;
	}

	/**
	 * Returns the Pilot's Flight Academy certification <i>codes</i>.
	 * @return a list of certification codes
	 * @see Pilot#addCertification(String)
	 * @see Pilot#addCertifications(Collection)
	 */
	public Collection<String> getCertifications() {
		return _certs;
	}

	/**
	 * Return the number of legs flown by this Pilot.
	 * @return the number of legs flown, either from the List of FlightReport beans or directly from setFlights()
	 * @see Pilot#setLegs(int)
	 */
	public int getLegs() {
		return _legs;
	}

	/**
	 * Returns the number of miles flown by this Pilot
	 * @return the number of miles flown
	 * @see Pilot#setMiles(long)
	 */
	public long getMiles() {
		return _miles;
	}

	/**
	 * Returns the number of flight hours logged by this Pilot.
	 * @return the number of hours flown
	 * @see Pilot#getOnlineHours()
	 * @see Person#getLegacyHours()
	 * @see Pilot#setHours(double)
	 */
	public double getHours() {
		return _hours;
	}

	/**
	 * Returns the number of online flight legs logged by this Pilot.
	 * @return the number of hours flown
	 * @see Pilot#getOnlineHours()
	 * @see Person#getLegacyHours()
	 * @see Pilot#setOnlineLegs(int)
	 */
	public int getOnlineLegs() {
		return _onlineLegs;
	}

	/**
	 * Returns the number of online flight hours logged by this Pilot.
	 * @return the number of hours flown
	 * @see Pilot#getHours()
	 * @see Pilot#getLastFlight()
	 * @see Person#getLegacyHours()
	 * @see Pilot#getOnlineLegs()
	 * @see Pilot#setOnlineHours(double)
	 */
	public double getOnlineHours() {
		return _onlineHours;
	}
	
	/**
	 * Returns the number of ACARS flight legs logged by this Pilot.
	 * @return the number of hours flown
	 * @see Pilot#getACARSHours()
	 * @see Person#getLegacyHours()
	 * @see Pilot#setACARSLegs(int)
	 */
	public int getACARSLegs() {
		return _acarsLegs;
	}
	
	/**
	 * Return the number of ACARS flight hours logged by this Pilot.
	 * @return the number of hours flown
	 * @see Pilot#getHours()
	 * @see Person#getLegacyHours()
	 * @see Pilot#getOnlineLegs()
	 * @see Pilot#setACARSHours(double)
	 */
	public double getACARSHours() {
		return _acarsHours;
	}
	
	/**
	 * Returns the total number of flight hours logged by this pilot across all Airlines.
	 * @return the total number of flight hours flown
	 * @see Pilot#setTotalHours(double)
	 * @see Pilot#getTotalLegs()
	 */
	public double getTotalHours() {
		return _totalHours;
	}
	
	/**
	 * Returns the total number of flight legs logged by this Pilot across all Airlines.
	 * @return the total number of legs flown
	 * @see Pilot#setTotalLegs(int)
	 * @see Pilot#getTotalHours()
	 */
	public int getTotalLegs() {
		return _totalLegs;
	}

	/**
	 * Returns the date of the Pilot's last flight.
	 * @return the date of the latest Flight Report
	 * @see Pilot#getHours()
	 * @see Pilot#getOnlineHours()
	 * @see Pilot#getOnlineLegs()
	 * @see Pilot#setLastFlight(Date)
	 */
	public Date getLastFlight() {
		return _lastFlight;
	}

	/**
	 * Queries wether this Pilot is rated in a particular equipment type.
	 * @param eqType the name of the equipment type
	 * @return TRUE if the Pilot is rated for this equipment type, otherwise FALSE
	 */
	public boolean hasRating(String eqType) {
		return _ratings.contains(eqType);
	}

	/**
	 * Queries if the Pilot has a signature image.
	 * @return TRUE if the pilot has an image, otherwise FALSE
	 * @see Pilot#setSignatureExtension(String)
	 * @see Pilot#getHasDefaultSignature()
	 * @see Pilot#setHasDefaultSignature(boolean)
	 */
	public boolean getHasSignature() {
		return (_sigExt != null);
	}
	
	/**
	 * Returns the extension of the Signature Image.
	 * @return the extension
	 * @see Pilot#setSignatureExtension(String)
	 * @see Pilot#getHasSignature()
	 */
	public String getSignatureExtension() {
		return _sigExt;
	}

	/**
	 * Queries if the default signature image should be shown.
	 * @return TRUE if the default image should be shown, otherwise FALSE
	 * @see Pilot#setHasDefaultSignature(boolean)
	 * @see Pilot#getHasSignature()
	 * @see Pilot#setSignatureExtension(String)
	 */
	public boolean getHasDefaultSignature() {
		return _showDefaultSignature;
	}

	/**
	 * Returns the Pilot's LDAP uid, used for SubVersion and raw HTTP access.
	 * @return the LDAP uid, or null
	 * @see Pilot#setLDAPName(String)
	 */
	public String getLDAPName() {
		return _ldapID;
	}

	/**
	 * Queries wether this Pilot is a member of a particular security role.
	 * @param roleName the name of the role
	 * @return TRUE if the Pilot is a member of this role, otherwise FALSE
	 */
	public boolean isInRole(String roleName) {
		return ("*".equals(roleName) || _roles.contains(roleName) || _roles.contains("Admin"));
	}

	/**
	 * Update this Pilot's status.
	 * @param status the new Status code for the Pilot. Use PilotStatus constants if possible
	 * @throws IllegalArgumentException if the new status is not contained within PilotStatus, or is negative
	 * @see Person#setStatus(int)
	 */
	public final void setStatus(int status) {
		if (status >= Pilot.STATUS.length)
			throw new IllegalArgumentException("Invalid Pilot Status - " + status);

		super.setStatus(status);
	}

	/**
	 * Updates this Pilot's "Show Water Cooler signatures" flag.
	 * @param showSigs the new flag value
	 * @see Pilot#getShowSignatures()
	 */
	public void setShowSignatures(boolean showSigs) {
		_showSigs = showSigs;
	}

	/**
	 * Updates wether the Pilot is locked out of the ACARS server.
	 * @param code a valid ACARS restriction code
	 * @throws IllegalArgumentException if code is negative or invalid
	 * @see Pilot#getACARSRestriction()
	 * @see Pilot#getACARSRestrictionName()
	 */
	public void setACARSRestriction(int code) {
		if ((code < 0) || (code >= RESTRICT.length))
			throw new IllegalArgumentException("Invalid ACARS Restriction code - " + code);

		_ACARSRestrict = code;
	}

	/**
	 * Updates wether this Pilot is locked out from taking new Examinations.
	 * @param noExams TRUE if the Testing Center is locked out, otherwise FALSE
	 * @see Pilot#getNoExams()
	 */
	public void setNoExams(boolean noExams) {
		_noExams = noExams;
	}

	/**
	 * Updates wether the Pilot is locked out from the Voice server.
	 * @param noVoice TRUE if the Pilot cannot access the voice server, otherwise FALSE
	 * @see Pilot#getNoVoice()
	 */
	public void setNoVoice(boolean noVoice) {
		_noVoice = noVoice;
	}
	
	/**
	 * Updates wether the Pilot is locked out from the Water Cooler.
	 * @param noCooler TRUE if the Pilot cannot access the Water Cooler, otherwise FALSE
	 * @see Pilot#getNoCooler()
	 */
	public void setNoCooler(boolean noCooler) {
		_noCooler = noCooler;
	}

	/**
	 * Updates wether this Pilot will see Water Cooler screen shot threads.
	 * @param showThreads TRUE if screen show threads will be displayed, otherwise FALSE
	 * @see Pilot#getShowSSThreads()
	 */
	public void setShowSSThreads(boolean showThreads) {
		_showSSThreads = showThreads;
	}

	/**
	 * Sets if this Pilot has a signature image available. <i>This method will clear the default signature flag</i>.
	 * @param ext the Signature extension, or null
	 * @see Pilot#getHasSignature()
	 * @see Pilot#getHasDefaultSignature()
	 * @see Pilot#setHasDefaultSignature(boolean)
	 */
	public void setSignatureExtension(String ext) {
		_sigExt = StringUtils.isEmpty(ext) ? null : ext.toLowerCase();
		if (getHasSignature())
			_showDefaultSignature = false;
	}

	/**
	 * Sets if this Pilot's signature should be the default siganture. <i>This method has no effect if the Pilot has a
	 * signature image.</i>
	 * @param hasSig TRUE if the default signature should be used, otherwise FALSE
	 * @see Pilot#getHasDefaultSignature()
	 * @see Pilot#getHasSignature()
	 * @see Pilot#setSignatureExtension(String)
	 */
	public void setHasDefaultSignature(boolean hasSig) {
		if (!getHasSignature())
			_showDefaultSignature = hasSig;
	}

	/**
	 * Updates the Pilot's motto.
	 * @param txt the motto text
	 * @see Pilot#getMotto()
	 */
	public void setMotto(String txt) {
		_motto = txt;
	}

	/**
	 * Sets the Pilot's preferred Map type.
	 * @param mapType the Map Type code
	 * @throws IllegalArgumentException if mapType is negative or invalid
	 * @see Pilot#setMapType(String)
	 * @see Pilot#getMapType()
	 * @see Pilot#getMapTypeName()
	 */
	public void setMapType(int mapType) {
		if ((mapType < 0) || (mapType >= MAP_TYPES.length))
			throw new IllegalArgumentException("Invalid Map Type - " + mapType);

		_mapType = mapType;
	}

	/**
	 * Sets the Pilot's preferred Map type.
	 * @param mapType the map Type name
	 * @throws IllegalArgumentException if mapType is invalid
	 * @see Pilot#setMapType(int)
	 * @see Pilot#getMapType()
	 * @see Pilot#getMapTypeName()
	 */
	public void setMapType(String mapType) {
		setMapType(StringUtils.arrayIndexOf(Pilot.MAP_TYPES, mapType, 0));
	}

	/**
	 * Update this Pilot's status.
	 * @param status the new Status description for the Pilot. Use PilotStatus constants if possible
	 * @throws IllegalArgumentException if the new status is not contained within PilotStatus, or is negative
	 * @see Person#setStatus(int)
	 * @see Person#getStatus()
	 */
	public final void setStatus(String status) {
		setStatus(StringUtils.arrayIndexOf(Pilot.STATUS, status));
	}

	/**
	 * Update this Pilot's logged hours. This method will typically only be called from a DAO where we are querying the
	 * <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a <B>SUM(PIREPS.HOURS)</B>.
	 * @param hours the number of hours logged by this Pilot.
	 * @throws IllegalArgumentException if hours is negative
	 * @see Pilot#getHours()
	 */
	public void setHours(double hours) {
		if (hours < 0)
			throw new IllegalArgumentException("Hours cannot be negative");

		_hours = hours;
	}

	/**
	 * Update this Pilot's logged flight legs. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>COUNT(PIREPS.HOURS)</B>.
	 * @param legs the number of legs logged by this Pilot
	 * @throws IllegalArgumentException if legs is negative
	 * @see Pilot#setHours(double)
	 * @see Pilot#setOnlineHours(double)
	 * @see Pilot#getLegs()
	 */
	public void setLegs(int legs) {
		if (legs < 0)
			throw new IllegalArgumentException("Legs cannot be negative");

		_legs = legs;
	}

	/**
	 * Update this Pilot's logged onlne flight legs. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>COUNT(PIREPS.HOURS) WHERE ((PIREPS.ATTR & 0x0D) != 0)</B>.
	 * @param legs the number of online legs logged by this Pilot
	 * @throws IllegalArgumentException if legs is negative
	 * @see Pilot#setHours(double)
	 * @see Pilot#setOnlineHours(double)
	 * @see Pilot#getLegs()
	 */
	public void setOnlineLegs(int legs) {
		if (legs < 0)
			throw new IllegalArgumentException("Online Legs cannot be negative");

		_onlineLegs = legs;
	}
	
	/**
	 * Update this Pilot's logged ACARS flight legs. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>COUNT(PIREPS.HOURS) WHERE ((PIREPS.ATTR & 0x10) != 0)</B>.
	 * @param legs the number of ACARS legs logged by this Pilot
	 * @throws IllegalArgumentException if legs is negative
	 * @see Pilot#setHours(double)
	 * @see Pilot#setACARSHours(double)
	 * @see Pilot#getACARSLegs()
	 */
	public void setACARSLegs(int legs) {
		if (legs < 0)
			throw new IllegalArgumentException("ACARS Legs cannot be negative");
		
		_acarsLegs = legs;
	}

	/**
	 * Updates this Pilot's last flight report date. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a <B>MAX(PIREPS.DATE)</B>.
	 * @param dt the date of the last flight.
	 * @see Pilot#getLastFlight()
	 * @see Pilot#setHours(double)
	 * @see Pilot#setOnlineHours(double)
	 */
	public void setLastFlight(Date dt) {
		_lastFlight = dt;
	}

	/**
	 * Updates this Pilot's logged online hours. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>SUM(PIREPS.HOURS) WHERE ((PIREPS.ATTRS & 0x0D) != 0)</B>.
	 * @param hours the online hours logged by this Pilot
	 * @throws IllegalArgumentException if hours is negative
	 * @see Pilot#setHours(double)
	 * @see Pilot#setLegs(int)
	 * @see Pilot#getOnlineHours()
	 */
	public void setOnlineHours(double hours) {
		if (hours < 0)
			throw new IllegalArgumentException("Online hours cannot be negative");

		_onlineHours = hours;
	}
	
	/**
	 * Updates this Pilot's logged ACARS hours. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>SUM(PIREPS.HOURS) WHERE ((PIREPS.ATTRS & 0x10) != 0)</B>.
	 * @param hours the ACARS hours logged by this Pilot
	 * @throws IllegalArgumentException if hours is negative
	 * @see Pilot#setHours(double)
	 * @see Pilot#setLegs(int)
	 * @see Pilot#getACARSHours()
	 */
	public void setACARSHours(double hours) {
		if (hours < 0)
			throw new IllegalArgumentException("ACARS hours cannot be negative");
		
		_acarsHours = hours;
	}
	
	/**
	 * Updates the Pilot's total logged hours between all airlines.
	 * @param hours the total hours logged by this Pilot
	 * @throws IllegalArgumentException if hours is negative
	 * @see Pilot#getTotalHours()
	 */
	public void setTotalHours(double hours) {
		if (hours < 0)
			throw new IllegalArgumentException("Total hours cannot be negative");
		
		_totalHours = hours;
	}
	
	/**
	 * Updates the Pilot's total flight legs between all airlines.
	 * @param legs the total legs logged by the Pilot
	 * @throws IllegalArgumentException if legs is negative
	 * @see Pilot#getTotalLegs()
	 */
	public void setTotalLegs(int legs) {
		if (legs < 0)
			throw new IllegalArgumentException("Total legs cannot be negative");
		
		_totalLegs = legs;
	}

	/**
	 * Update this pilot's logged miles. This method will typically only be called from a DAO where we are querying the
	 * <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a <B>SUM(PIREPS.DISTANCE)</B>.
	 * @param miles the number of miles logged by this pilot
	 * @throws IllegalArgumentException if miles is negative
	 * @see Pilot#getMiles()
	 */
	public void setMiles(long miles) {
		if (miles < 0)
			throw new IllegalArgumentException("Miles cannot be negative");

		_miles = miles;
	}

	/**
	 * Updates this Pilot's LDAP uid attribute, for use by Subversion and raw HTTP access.
	 * @param uid the userid, which will be converted to lowercase
	 * @see Pilot#getLDAPName()
	 */
	public void setLDAPName(String uid) {
		_ldapID = StringUtils.isEmpty(uid) ? null : uid.toLowerCase();
	}

	/**
	 * Add a rated equipment type for this Pilot.
	 * @param rating the aircraft type to add to this Pilot's ratings.
	 * @throws NullPointerException if the rating is null
	 * @see Pilot#addRatings(Collection)
	 * @see Pilot#removeRatings(Collection)
	 * @see Pilot#getRatings()
	 */
	public void addRating(String rating) {
		_ratings.add(rating);
	}

	/**
	 * Add a number of equipment type ratings for this Pilot.
	 * @param ratings a Collection of ratings to add to this Pilot's ratings
	 * @throws NullPointerException if ratings is null
	 * @see Pilot#addRating(String)
	 * @see Pilot#removeRatings(Collection)
	 * @see Pilot#getRatings()
	 */
	public void addRatings(Collection<String> ratings) {
		_ratings.addAll(ratings);
	}

	/**
	 * Remove a number of equipment type ratings for this Pilot.
	 * @param ratings a Collection of ratings to remove from this Pilot's ratings
	 * @throws NullPointerException if ratings is null
	 * @see Pilot#addRating(String)
	 * @see Pilot#addRatings(Collection)
	 * @see Pilot#getRatings()
	 */
	public void removeRatings(Collection<String> ratings) {
		_ratings.removeAll(ratings);
	}

	/**
	 * Add membership in a security role to this Pilot.
	 * @param roleName the name of the role
	 */
	public void addRole(String roleName) {
		_roles.add(roleName);
	}

	/**
	 * Adds membership in a group of security roles to this Pilot.
	 * @param roles a Collection of role names
	 * @see Pilot#addRole(String)
	 * @see Pilot#getRoles()
	 * @see Pilot#removeRoles(Collection)
	 */
	public void addRoles(Collection<String> roles) {
		_roles.addAll(roles);
	}

	/**
	 * Removes this Pilot's membership in a group fo security roles.
	 * @param roles a Collection of role names
	 * @see Pilot#addRole(String)
	 * @see Pilot#addRoles(Collection)
	 * @see Pilot#getRoles()
	 */
	public void removeRoles(Collection<String> roles) {
		_roles.removeAll(roles);
		_roles.add("Pilot");
	}

	/**
	 * Adds a Flight Academy certification code for this Pilot.
	 * @param certCode the Certification code
	 * @see Pilot#addCertifications(Collection)
	 * @see Pilot#getCertifications()
	 */
	public void addCertification(String certCode) {
		_certs.add(certCode);
	}

	/**
	 * Adds Flight Academy certification codes for this Pilot.
	 * @param certCodes a Collection of Certification codes
	 * @see Pilot#addCertification(String)
	 * @see Pilot#getCertifications()
	 */
	public void addCertifications(Collection<String> certCodes) {
		_certs.addAll(certCodes);
	}

	/**
	 * Updates the Pilot's seniority number.
	 * @param pNumber the new seniority number
	 * @throws IllegalArgumentException if pNumber is zero or negative
	 * @see Pilot#getPilotNumber()
	 * @see Pilot#getPilotCode()
	 */
	public void setPilotNumber(int pNumber) {
		if (pNumber < 1)
			throw new IllegalArgumentException("Invalid Pilot Number - " + pNumber);

		_pCodeId = pNumber;
	}

	/**
	 * Set the pilot code for this Pilot.
	 * @param code the pilot code eg. DVA043.
	 * @throws NullPointerException if code is null
	 * @throws IllegalArgumentException if the code does not start with the prefix, or the remainder of the code cannot
	 *             be parsed to a number via DecimalFormat.parse("##000");
	 * @see Pilot#getPilotCode()
	 * @see Pilot#getPilotNumber()
	 * @see DecimalFormat#parse(java.lang.String)
	 */
	public void setPilotCode(CharSequence code) {
		if (code == null)
			return;

		StringBuilder pBuf = new StringBuilder();
		StringBuilder cBuf = new StringBuilder();
		for (int x = 0; x < code.length(); x++) {
			char c = Character.toUpperCase(code.charAt(x));
			if (Character.isDigit(c)) {
				cBuf.append(c);
			} else if (Character.isLetter(c)) {
				pBuf.append(c);
			}
		}

		// Save the prefix and the code
		_pCodePrefix = pBuf.toString();
		try {
			_pCodeId = Integer.parseInt(cBuf.toString());
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid Pilot Code - " + code);
		}
	}

	/**
	 * Tests equality by comparing the pilot ID. <i>This is used by the GetPilot DAO for cache lookups.</i>
	 */
	public Object cacheKey() {
		return new Integer(getID());
	}

	/**
	 * Selects a table row class based upon the Pilot's status.
	 * @return the row CSS class name
	 */
	public String getRowClassName() {
		return ROW_CLASSES[getStatus()];
	}

	/**
	 * Shallow-clone a Pilot by copying everything except FlightReport/StatusUpdate beans and the database ID.
	 * @return a copy of the current Pilot bean
	 * @see Pilot#clone()
	 */
	public Pilot cloneExceptID() {
		Pilot p2 = new Pilot(getFirstName(), getLastName());
		p2.setDN(getDN());
		p2.setAirportCodeType(getAirportCodeType());
		p2.setCreatedOn(getCreatedOn());
		p2.setDateFormat(getDateFormat());
		p2.setEmail(getEmail());
		p2.setEmailAccess(getEmailAccess());
		p2.setEquipmentType(getEquipmentType());
		p2.setHomeAirport(getHomeAirport());
		p2.setLastLogin(getLastLogin());
		p2.setLastLogoff(getLastLogoff());
		p2.setLocation(getLocation());
		p2.setLoginCount(getLoginCount());
		p2.setLoginHost(getLoginHost());
		p2.setNumberFormat(getNumberFormat());
		p2.setPassword(getPassword());
		p2.setRank(getRank());
		p2.setStatus(getStatus());
		p2.setTimeFormat(getTimeFormat());
		p2.setTZ(getTZ());
		p2.setUIScheme(getUIScheme());
		p2.setMotto(getMotto());
		p2.addRoles(getRoles());
		p2.addRatings(getRatings());
		p2.setSignatureExtension(getSignatureExtension());
		p2.setHours(getHours());
		p2.setLastFlight(getLastFlight());
		p2.setLegs(getLegs());
		p2.setMapType(getMapType());
		p2.setMiles(getMiles());
		p2.setOnlineHours(getOnlineHours());
		p2.setOnlineLegs(getOnlineLegs());
		p2.setACARSHours(getACARSHours());
		p2._acarsLegs = _acarsLegs;
		p2.setTotalHours(getTotalHours());
		p2._totalLegs = _totalLegs;
		p2.setShowSignatures(getShowSignatures());
		p2.setShowSSThreads(getShowSSThreads());
		p2._networkIDs.putAll(getNetworkIDs());
		if (!StringUtils.isEmpty(getPilotCode()))
			p2.setPilotCode(getPilotCode());

		for (Iterator<String> i = getNotifyOptions().iterator(); i.hasNext();)
			p2.setNotifyOption(i.next(), true);

		for (Iterator<String> i = getIMServices().iterator(); i.hasNext();) {
			String svc = i.next();
			p2.setIMHandle(svc, getIMHandle(svc));
		}

		return p2;
	}

	/**
	 * Shallow-clone a Pilot by copying everything except FlightReport/StatusUpdate beans.
	 * @return a copy of the current Pilot bean
	 * @see Pilot#cloneExceptID()
	 */
	public Object clone() {
		Pilot p2 = cloneExceptID();
		p2.setID(getID());
		return p2;
	}
}
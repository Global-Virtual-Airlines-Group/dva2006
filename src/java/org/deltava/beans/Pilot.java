// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import java.util.*;

import org.deltava.beans.acars.Restriction;
import org.deltava.beans.stats.DatedAccomplishmentID;

import org.deltava.util.StringUtils;

/**
 * A class for storing Pilot entries.
 * @author Luke
 * @version 6.4
 * @since 1.0
 */

public class Pilot extends Person implements ComboAlias, Cloneable {

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

	private static final String[] ROW_CLASSES = { null, "opt2", "opt3", "opt1", "err", "warn" };

	private String _pCodePrefix;
	private int _pCodeId;

	private String _ldapID;

	private final Collection<String> _ratings = new TreeSet<String>();
	private final Collection<String> _roles = new TreeSet<String>();
	private final Collection<String> _certs = new LinkedHashSet<String>();
	private final Collection<DatedAccomplishmentID> _accIDs = new TreeSet<DatedAccomplishmentID>();

	private long _miles;
	private Date _lastFlight;

	private String _motto;

	private int _legs;
	private int _acarsLegs = -1; // Set to -1 which is uninitialized
	private int _onlineLegs;
	private int _eventLegs;
	private int _totalLegs;
	private int _dispatchFlights = -1;  // Set to -1 which is uninitialized
	private double _hours;
	private double _acarsHours;
	private double _onlineHours;
	private double _eventHours;
	private double _totalHours;
	private double _dispatchHours;
	private int _eventSignups = -1; // Set to -1 which is uninitialized

	private boolean _showSigs;
	private boolean _showSSThreads;
	private boolean _showDefaultSignature;
	private boolean _showNewPosts;
	private boolean _noExams;
	private boolean _noVoice;
	private boolean _noCooler;
	private boolean _noTimeCompression;

	private Restriction _ACARSRestrict;
	private MapType _mapType;
	
	private String _sigExt;
	private Date _sigModified;

	private boolean _showNavBar;
	private boolean _isPermanent;

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
	@Override
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
		return (_pCodeId == 0) ? "" : _pCodePrefix + StringUtils.format(_pCodeId, "##000");
	}

	/**
	 * Return the pilot number.
	 * @return the pilot number eg. 43. If the pilot has not logged any flights this will return 0.
	 * @see Pilot#setPilotCode(CharSequence)
	 */
	public int getPilotNumber() {
		return _pCodeId;
	}

	@Override
	public String getComboName() {
		return getName();
	}

	@Override
	public String getComboAlias() {
		return String.valueOf(getID());
	}

	/**
	 * Returns whether the pilot will see Water Cooler signatures.
	 * @return TRUE if the Pilot views signature images, otherwise FALSE
	 * @see Pilot#setShowSignatures(boolean)
	 */
	public boolean getShowSignatures() {
		return _showSigs;
	}

	/**
	 * Returns whether the Pilot will see Water Cooler screen shot message threads.
	 * @return TRUE if the Pilot sees screen shot threads, otherwise FALSE
	 * @see Pilot#setShowSSThreads(boolean)
	 */
	public boolean getShowSSThreads() {
		return _showSSThreads;
	}
	
	/**
	 * Returns whether a navigation bar or a side menu will be rendered.
	 * @return TRUE if a navigation bar should be displayed, otherwise FALSE
	 * @see Pilot#setShowNavBar(boolean)
	 */
	public boolean getShowNavBar() {
		return _showNavBar;
	}
	
	/**
	 * Returns whether new Water Cooler posts will be scrolled to.
	 * @return TRUE if new posts are scrolled to, otherwise FALSE
	 * @see Pilot#setShowNewPosts(boolean)
	 */
	public boolean getShowNewPosts() {
		return _showNewPosts;
	}

	/**
	 * Returns the Pilot's ACARS restrictions.
	 * @return an ACARS Restriction
	 * @see Pilot#setACARSRestriction(Restriction)
	 */
	public Restriction getACARSRestriction() {
		return _ACARSRestrict;
	}
	
	/**
	 * Returns whether the Pilot cannot submit flight reports with Time Compression.
	 * @return TRUE if Time Compression disabled, otherwise FALSE
	 * @see Pilot#setNoCooler(boolean)
	 */
	public boolean getNoTimeCompression() {
		return _noTimeCompression;
	}

	/**
	 * Returns whether the Pilot has been locked out of the Testing Center.
	 * @return TRUE if the Pilot cannot take any Examinations, otherwise FALSE
	 * @see Pilot#setNoExams(boolean)
	 */
	public boolean getNoExams() {
		return _noExams;
	}

	/**
	 * Returns whether the Pilot has been locked out of the Voice server.
	 * @return TRUE if the Pilot has been locked out of the Voice server, otherwise FALSE
	 * @see Pilot#setNoVoice(boolean)
	 */
	public boolean getNoVoice() {
		return _noVoice;
	}
	
	/**
	 * Returns whether the Pilot has been locked out of the Water Cooler.
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
	@Override
	public String getStatusName() {
		return Pilot.STATUS[getStatus()];
	}
	
	/**
	 * Returns whether this Pilot cannot be marked Inactive.
	 * @return TRUE if a permanent account, otherwise FALSE
	 * @see Pilot#setIsPermanent(boolean)
	 */
	public boolean getIsPermanent() {
		return _isPermanent;
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
	 * @return the MapType
	 * @see Pilot#setMapType(MapType)
	 */
	public MapType getMapType() {
		return _mapType;
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
	 * Returns the Pilot's Accomplishments.
	 * @return a Collection of DatedAccomplishmentIDs
	 * @see Pilot#addAccomplishmentID(DatedAccomplishmentID)
	 */
	public Collection<DatedAccomplishmentID> getAccomplishmentIDs() {
		return _accIDs;
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
	 * @return the number of legs flown
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
	 * Returns the number of Online Event flight legs logged by this Pilot.
	 * @return the number of legs flown
	 * @see Pilot#setEventLegs(int)
	 * @see Pilot#getEventHours()
	 */
	public int getEventLegs() {
		return _eventLegs;
	}
	
	/**
	 * Returns the number of Online Event flight hours logged by this Pilot.
	 * @return the number of hours flown
	 * @see Pilot#setEventHours(double)
	 * @see Pilot#getEventLegs()
	 */
	public double getEventHours() {
		return _eventHours;
	}
	
	/**
	 * Returns the number of times this Pilot has signed up for an Online Event.
	 * @return the number of Events signed up for
	 * @see Pilot#setEventSignups(int)
	 */
	public int getEventSignups() {
		return _eventSignups;
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
	 * Returns the total number of flights the user has Dispatched.
	 * @return the number of dispatched flights
	 * @see Pilot#setDispatchFlights(int)
	 */
	public int getDispatchFlights() {
		return _dispatchFlights;
	}

	/**
	 * Returns the number of hours spent providing Dispatch services.
	 * @return the number of hours
	 * @see Pilot#setDispatchHours(double)
	 */
	public double getDispatchHours() {
		return _dispatchHours;
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
	 * Queries whether this Pilot is rated in a particular equipment type.
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
	 * Returns the last modification date of the Signature Image.
	 * @return the last modified date/time, or null if none
	 * @see Pilot#setSignatureModified(Date)
	 */
	public Date getSignatureModified() {
		return _sigModified;
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
	 * Queries whether this Pilot is a member of a particular security role.
	 * @param roleName the name of the role
	 * @return TRUE if the Pilot is a member of this role, otherwise FALSE
	 */
	@Override
	public boolean isInRole(String roleName) {
		return ("*".equals(roleName) || _roles.contains(roleName) || _roles.contains("Admin"));
	}

	/**
	 * Update this Pilot's status.
	 * @param status the new Status code for the Pilot. Use PilotStatus constants if possible
	 * @throws IllegalArgumentException if the new status is not contained within PilotStatus, or is negative
	 * @see Person#setStatus(int)
	 */
	@Override
	public final void setStatus(int status) {
		if (status >= Pilot.STATUS.length)
			throw new IllegalArgumentException("Invalid Pilot Status - " + status);

		super.setStatus(status);
	}
	
	/**
	 * Marks this account as being unable to be marked Inactive.
	 * @param isPerm TRUE if permanent, otherwise FALSE
	 * @see Pilot#getIsPermanent()
	 */
	public void setIsPermanent(boolean isPerm) {
		_isPermanent = isPerm;
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
	 * Updates whether new Water Cooler posts will be scrolled to.
	 * @param newPosts TRUE if new posts are scrolled to, otherwise FALSE
	 * @see Pilot#getShowNewPosts()
	 */
	public void setShowNewPosts(boolean newPosts) {
		_showNewPosts = newPosts;
	}

	/**
	 * Updates whether the Pilot is locked out of the ACARS server.
	 * @param r an ACARS Restriction
	 * @see Pilot#getACARSRestriction()
	 */
	public void setACARSRestriction(Restriction r) {
		_ACARSRestrict = r;
	}
	
	/**
	 * Updates whether the Pilot cannot submit flight reports with Time Compression.
	 * @param noCompress TRUE if Time Compression disabled, otherwise FALSE
	 * @see Pilot#getNoTimeCompression()
	 */
	public void setNoTimeCompression(boolean noCompress) {
		_noTimeCompression = noCompress;
	}

	/**
	 * Updates whether this Pilot is locked out from taking new Examinations.
	 * @param noExams TRUE if the Testing Center is locked out, otherwise FALSE
	 * @see Pilot#getNoExams()
	 */
	public void setNoExams(boolean noExams) {
		_noExams = noExams;
	}

	/**
	 * Updates whether the Pilot is locked out from the Voice server.
	 * @param noVoice TRUE if the Pilot cannot access the voice server, otherwise FALSE
	 * @see Pilot#getNoVoice()
	 */
	public void setNoVoice(boolean noVoice) {
		_noVoice = noVoice;
	}
	
	/**
	 * Updates whether the Pilot is locked out from the Water Cooler.
	 * @param noCooler TRUE if the Pilot cannot access the Water Cooler, otherwise FALSE
	 * @see Pilot#getNoCooler()
	 */
	public void setNoCooler(boolean noCooler) {
		_noCooler = noCooler;
	}

	/**
	 * Updates whether this Pilot will see Water Cooler screen shot threads.
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
	 * @see Pilot#getSignatureExtension()
	 */
	public void setSignatureExtension(String ext) {
		_sigExt = StringUtils.isEmpty(ext) ? null : ext.toLowerCase();
		if (getHasSignature())
			_showDefaultSignature = false;
	}
	
	/**
	 * Sets the last modified date the Pilot's signature image.
	 * @param dt the last modified date/time
	 * @see Pilot#getSignatureModified()
	 * @see Pilot#getHasSignature()
	 */
	public void setSignatureModified(Date dt) {
		_sigModified = dt;
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
	 * @param mt the MapType
	 * @see Pilot#getMapType()
	 */
	public void setMapType(MapType mt) {
		_mapType = mt;
	}
	
	/**
	 * Sets whether a Navigation Bar or side menu should be rendered. 
	 * @param showNavBar TRUE if a navigation bar should be rendered, otherwise FALSE
	 * @see Pilot#getShowNavBar()
	 */
	public void setShowNavBar(boolean showNavBar) {
		_showNavBar = showNavBar;
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
	 * @see Pilot#getHours()
	 */
	public void setHours(double hours) {
		_hours = Math.max(0, hours);
	}

	/**
	 * Update this Pilot's logged flight legs. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>COUNT(PIREPS.HOURS)</B>.
	 * @param legs the number of legs logged by this Pilot
	 * @see Pilot#setHours(double)
	 * @see Pilot#setOnlineHours(double)
	 * @see Pilot#getLegs()
	 */
	public void setLegs(int legs) {
		_legs = Math.max(0, legs);
	}

	/**
	 * Update this Pilot's logged onlne flight legs. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>COUNT(PIREPS.HOURS) WHERE ((PIREPS.ATTR & 0x0D) != 0)</B>.
	 * @param legs the number of online legs logged by this Pilot
	 * @see Pilot#setHours(double)
	 * @see Pilot#setOnlineHours(double)
	 * @see Pilot#getLegs()
	 */
	public void setOnlineLegs(int legs) {
		_onlineLegs = Math.max(0, legs);
	}
	
	/**
	 * Updates the number of flight legs flown as part of an Online Event.
	 * @param legs the number of lgs
	 * @see Pilot#getEventLegs()
	 */
	public void setEventLegs(int legs) {
		_eventLegs = Math.max(0, legs);
	}
	
	/**
	 * Updates the number of times this Pilot has signed up for an Online Event.
	 * @param signups the number of signups
	 * @see Pilot#getEventSignups()
	 */
	public void setEventSignups(int signups) {
		_eventSignups = Math.max(0, signups);
	}
	
	/**
	 * Update this Pilot's logged ACARS flight legs. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>COUNT(PIREPS.HOURS) WHERE ((PIREPS.ATTR & 0x10) != 0)</B>.
	 * @param legs the number of ACARS legs logged by this Pilot
	 * @see Pilot#setHours(double)
	 * @see Pilot#setACARSHours(double)
	 * @see Pilot#getACARSLegs()
	 */
	public void setACARSLegs(int legs) {
		_acarsLegs = Math.max(0, legs);
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
	 * @see Pilot#setHours(double)
	 * @see Pilot#setLegs(int)
	 * @see Pilot#getOnlineHours()
	 */
	public void setOnlineHours(double hours) {
		_onlineHours = Math.max(0, hours);
	}
	
	/**
	 * Updates this Pilot's logged hours as part of an Online Event.
	 * @param hours the event hours logged by this Pilot
	 * @see Pilot#getEventHours()
	 */
	public void setEventHours(double hours) {
		_eventHours = Math.max(0, hours);
	}
	
	/**
	 * Updates this Pilot's logged ACARS hours. This method will typically only be called from a DAO where we are
	 * querying the <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a
	 * <B>SUM(PIREPS.HOURS) WHERE ((PIREPS.ATTRS & 0x10) != 0)</B>.
	 * @param hours the ACARS hours logged by this Pilot
	 * @see Pilot#setHours(double)
	 * @see Pilot#setLegs(int)
	 * @see Pilot#getACARSHours()
	 */
	public void setACARSHours(double hours) {
		_acarsHours = Math.max(0, hours);
	}
	
	/**
	 * Updates the Pilot's total logged hours between all airlines.
	 * @param hours the total hours logged by this Pilot
	 * @see Pilot#getTotalHours()
	 */
	public void setTotalHours(double hours) {
		_totalHours = Math.max(0, hours);
	}
	
	/**
	 * Updates the Pilot's total flight legs between all airlines.
	 * @param legs the total legs logged by the Pilot
	 * @see Pilot#getTotalLegs()
	 */
	public void setTotalLegs(int legs) {
		_totalLegs = Math.max(0, legs);
	}
	
	/**
	 * Updates the number of flights the Pilot has dispactched as a Dispatcher.
	 * @param legs the number of flights dispatched
	 * @see Pilot#getDispatchFlights()
	 */
	public void setDispatchFlights(int legs) {
		_dispatchFlights = Math.max(0, legs);
	}
	
	/**
	 * Updates the number of hours spent providng Dispatch services.
	 * @param hours the number of hours providing service
	 * @see Pilot#getDispatchHours()
	 */
	public void setDispatchHours(double hours) {
		_dispatchHours = Math.max(0, hours);
	}

	/**
	 * Update this pilot's logged miles. This method will typically only be called from a DAO where we are querying the
	 * <b>PILOTS</b> table, and not actually loading all the PIREPs but just getting a <B>SUM(PIREPS.DISTANCE)</B>.
	 * @param miles the number of miles logged by this pilot
	 * @see Pilot#getMiles()
	 */
	public void setMiles(long miles) {
		_miles = Math.max(0, miles);
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
	@Override
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
	 * Adds an Accomplishment to this Pilot.
	 * @param id a DatedAccomplishmentID
	 * @see Pilot#getAccomplishmentIDs()
	 */
	public void addAccomplishmentID(DatedAccomplishmentID id) {
		_accIDs.add(id);
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
	 * @param code the pilot code eg. DVA043
	 * @throws NullPointerException if code is null
	 * @throws IllegalArgumentException if the code does not start with the prefix, or the remainder of the code cannot
	 *             be parsed to a number via DecimalFormat.parse("##000");
	 * @see Pilot#getPilotCode()
	 * @see Pilot#getPilotNumber()
	 */
	public void setPilotCode(CharSequence code) {
		if (code == null)
			return;

		StringBuilder pBuf = new StringBuilder();
		StringBuilder cBuf = new StringBuilder();
		for (int x = 0; x < code.length(); x++) {
			char c = Character.toUpperCase(code.charAt(x));
			if (Character.isDigit(c))
				cBuf.append(c);
			else if (Character.isLetter(c))
				pBuf.append(c);
		}

		// Save the prefix and the code
		_pCodePrefix = pBuf.toString().toUpperCase();
		try {
			_pCodeId = Integer.parseInt(cBuf.toString());
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid Pilot Code - " + code);
		}
	}

	/**
	 * Selects a table row class based upon the Pilot's status.
	 * @return the row CSS class name
	 */
	@Override
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
		p2.setDistanceType(getDistanceType());
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
		p2._hours = _hours;
		p2._lastFlight = _lastFlight;
		p2._legs = _legs;
		p2._mapType = _mapType;
		p2._miles = _miles;
		p2._onlineHours = _onlineHours;
		p2._onlineLegs = _onlineLegs;
		p2._acarsHours = _acarsHours;
		p2._acarsLegs = _acarsLegs;
		p2._eventHours = _eventHours;
		p2._eventLegs = _eventLegs;
		p2._totalHours = _totalHours;
		p2._totalLegs = _totalLegs;
		p2._showSigs = _showSigs;
		p2._showSSThreads = _showSSThreads;
		p2._showNavBar = _showNavBar;
		p2._showNewPosts = _showNewPosts;
		p2._networkIDs.putAll(_networkIDs);
		if (!StringUtils.isEmpty(getPilotCode()))
			p2.setPilotCode(getPilotCode());

		p2._notifyOptions.addAll(_notifyOptions);
		for (Map.Entry<IMAddress, String> me : getIMHandle().entrySet())
			p2.setIMHandle(me.getKey(), me.getValue());

		return p2;
	}

	/**
	 * Shallow-clone a Pilot by copying everything except FlightReport/StatusUpdate beans.
	 * @return a copy of the current Pilot bean
	 * @see Pilot#cloneExceptID()
	 */
	@Override
	public Object clone() {
		Pilot p2 = cloneExceptID();
		p2.setID(getID());
		return p2;
	}
}
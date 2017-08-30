// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;
import java.time.*;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airline;

/**
 * A class for dealing with PIREP data.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class FlightReport extends Flight implements AuthoredBean, CalendarEntry, ViewEntry {

	public static final int DRAFT = 0;
	public static final int SUBMITTED = 1;
	public static final int HOLD = 2;
	public static final int OK = 3;
	public static final int REJECTED = 4;

	/**
	 * Valid PIREP statuses.
	 */
	public static final String STATUS[] = { "Draft", "Submitted", "Hold", "OK", "Rejected" };
	private static final String[] ROW_CLASSES = { "opt2", "opt1", "warn", null, "err" };

	/**
	 * Flight flown without Equipment Type Rating.
	 */
	public static final int ATTR_NOTRATED = 0x01;

	/**
	 * Flight flown on VATSIM network.
	 */
	public static final int ATTR_VATSIM = 0x02;

	/**
	 * Flight flown on IVAO network.
	 */
	public static final int ATTR_IVAO = 0x04;

	/**
	 * Flight flown on FPI network.
	 */
	public static final int ATTR_FPI = 0x08;

	/**
	 * Flight logged using ACARS.
	 */
	public static final int ATTR_ACARS = 0x10;

	/**
	 * Flight flown using unknown route pair.
	 */
	public static final int ATTR_ROUTEWARN = 0x20;

	/**
	 * Flight flown with unusually high or low logged hous.
	 */
	public static final int ATTR_TIMEWARN = 0x40;

	/**
	 * Flight flown as a Check Ride.
	 */
	public static final int ATTR_CHECKRIDE = 0x80;

	/**
	 * Flight flown as a Charter flight.
	 */
	public static final int ATTR_CHARTER = 0x100;

	/**
	 * Flight flown using Historic equipment.
	 */
	public static final int ATTR_HISTORIC = 0x200;

	/**
	 * Flight Academy Training Flight.
	 */
	public static final int ATTR_ACADEMY = 0x400;

	/**
	 * Flight flown with excessive range for aircraft.
	 */
	public static final int ATTR_RANGEWARN = 0x800;
	
	/**
	 * Flight flown with ACARS refueling detected.
	 */
	public static final int ATTR_REFUELWARN = 0x1000;
	
	/**
	 * Flight flown with non-ETOPS-rated aircraft.
	 */
	public static final int ATTR_ETOPSWARN = 0x2000;
	
	/**
	 * Flight flown using a Dispatcher-generated flight plan.
	 */
	public static final int ATTR_DISPATCH = 0x4000;
	
	/**
	 * Flight flown with excessive weights for Aircraft.
	 */
	public static final int ATTR_WEIGHTWARN = 0x8000;
	
	/**
	 * Flight logged using XACARS.
	 */
	public static final int ATTR_XACARS = 0x10000;
	
	/**
	 * Flight flown using a too-short takeoff or landing runway.
	 */
	public static final int ATTR_RWYWARN = 0x20000;
	
	/**
	 * Flight flown using an inappropriate runway surface.
	 */
	public static final int ATTR_RWYSFCWARN = 0x40000;
	
	/**
	 * Flight logged using simFDR.
	 */
	public static final int ATTR_SIMFDR = 0x80000;
	
	/**
	 * Flight logged using PilotEdge.
	 */
	public static final int ATTR_PEDGE = 0x100000;
	
	/**
	 * Flight flown through prohibited airspace.
	 */
	public static final int ATTR_AIRSPACEWARN = 0x200000;
	
	/**
	 * Attribute mask for all warnings.
	 */
	public static final int ATTR_WARN_MASK = 0x26B861;
	
	/**
	 * Attribute mask for VATSIM/IVAO/FPI online flights.
	 */
	public static final int ATTR_ONLINE_MASK = 0x10000E;
	
	/**
	 * Attribute mask for ACARS/XACARS/simFDR flights.
	 */
	public static final int ATTR_FDR_MASK = 0x90010;
	
	/**
	 * Minimum ACARS client version for inclusion in statistics aggregation.
	 */
	public static final int MIN_ACARS_CLIENT = 61;

	private Instant _date;
	private Instant _submittedOn;
	private Instant _disposedOn;
	private int _length;
	private int _status = FlightReport.DRAFT;
	private Simulator _fsVersion = Simulator.UNKNOWN;
	private int _attr;
	private int _pax;
	private double _load;
	private String _remarks;
	private String _comments; // made on disposition
	private Rank _rank; // at time of flight
	private String _route;

	private final Collection<String> _captEQType = new TreeSet<String>();

	// Stores Integers pointing to other database IDs
	private final Map<DatabaseID, Integer> _dbIds = new HashMap<DatabaseID, Integer>();

	/**
	 * Creates a new Flight Report object with a given flight.
	 * @param a the Airline
	 * @param flightNumber the Flight Number
	 * @param leg the Leg Number
	 * @throws NullPointerException if the Airline Code is null
	 * @throws IllegalArgumentException if the Flight Report is zero or negative
	 * @throws IllegalArgumentException if the Leg is less than 1 or greater than 5
	 * @see Flight#setAirline(Airline)
	 * @see Flight#setFlightNumber(int)
	 * @see Flight#setLeg(int)
	 */
	public FlightReport(Airline a, int flightNumber, int leg) {
		super(a, flightNumber, leg);
	}

	/**
	 * Creates a new Flight Report from an existing Flight entry (like an Assignment or ScheduleEntry).
	 * @param f the existing Flight
	 */
	public FlightReport(Flight f) {
		super(f.getAirline(), f.getFlightNumber(), f.getLeg());
		setEquipmentType(f.getEquipmentType());
		setAirportD(f.getAirportD());
		setAirportA(f.getAirportA());
	}

	/**
	 * Returns the Pilot's rank at the time of the Flight.
	 * @return the Pilot's rank
	 * @see FlightReport#setRank(Rank)
	 */
	public Rank getRank() {
		return _rank;
	}

	/**
	 * Returns the length of the fllight <i>in hours multiplied by ten</i>. This is done to avoid rounding errors when
	 * using a floating point number.
	 * @return the length of the flight <i>in hours multiplied by ten</i>
	 * @see FlightReport#setLength(int)
	 * @see ACARSFlightReport#getLength()
	 */
	@Override
	public int getLength() {
		return _length;
	}
	
	@Override
	public Duration getDuration() {
		return Duration.ofSeconds(_length * 360);
	}

	/**
	 * Returns the remarks for this Flight Report.
	 * @return the remarks
	 * @see FlightReport#setRemarks(String)
	 * @see FlightReport#getComments()
	 */
	public String getRemarks() {
		return _remarks;
	}

	/**
	 * Returns the filed Flight Route.
	 * @return the route
	 * @see FlightReport#setRoute(String)
	 */
	public String getRoute() {
		return _route;
	}
	
	/**
	 * Returns the disposition comments for this Flight Report.
	 * @return the disposition comments
	 * @see FlightReport#setComments(String)
	 * @see FlightReport#getRemarks()
	 */
	public String getComments() {
		return _comments;
	}

	/**
	 * Returns if this flight counts towards promotion in a particular equipment type program.
	 * @return the equipment type program name(s), or an empty Collection if this flight does not count
	 */
	public Collection<String> getCaptEQType() {
		return _captEQType;
	}

	/**
	 * Returns the date this flight was flown.
	 * @return the date of this flight; the time component is undefined
	 * @see FlightReport#setDate(Instant)
	 */
	@Override
	public Instant getDate() {
		return _date;
	}

	/**
	 * The date/time this Flight Report was submitted for approval.
	 * @return the submission date/time of this PIREP, null if never submitted.
	 * @see FlightReport#setSubmittedOn(Instant)
	 */
	public Instant getSubmittedOn() {
		return _submittedOn;
	}

	/**
	 * The date/time this Flight Report was disposed on. (approved or rejected)
	 * @return the approval/rejected date/time of this PIREP, null if not disposed of
	 * @see FlightReport#setDisposedOn(Instant)
	 */
	public Instant getDisposedOn() {
		return _disposedOn;
	}

	/**
	 * Sets the database row ID of a relatied database row. This row may be present in the <i>PILOTS </i>,
	 * <i>ASSIGNMENTS </i>, <i>ACARS_PIREPS </i> or <i>EVENTS </i> table. <i>This is typically used by a DAO </i>
	 * @param idType the datbase row ID type
	 * @return the database row ID, or 0 if not found
	 * @throws NullPointerException if idType is null
	 * @see FlightReport#setDatabaseID(DatabaseID, int)
	 */
	public int getDatabaseID(DatabaseID idType) {
		Integer dbID = _dbIds.get(idType);
		return (dbID == null) ? 0 : dbID.intValue();
	}
	
	@Override
	public int getAuthorID() {
		return getDatabaseID(DatabaseID.PILOT);
	}

	/**
	 * Returns the Simulator used for this Flight.
	 * @return the Simulator
	 * @see FlightReport#setSimulator(Simulator)
	 */
	public Simulator getSimulator() {
		return _fsVersion;
	}

	/**
	 * Returns the attributes for this Flight Report.
	 * @return a bit-mask containing this Flight Report's attributes
	 * @see FlightReport#setAttributes(int)
	 * @see FlightReport#setAttribute(int, boolean)
	 */
	public int getAttributes() {
		return _attr;
	}

	/**
	 * Returns the status of this Flight Report.
	 * @return the status of this PIREP
	 * @see FlightReport#getStatusName()
	 * @see FlightReport#setStatus(int)
	 * @see FlightReport#setStatus(String)
	 */
	public int getStatus() {
		return _status;
	}

	/**
	 * Status description of this Flight Report, for JSPs.
	 * @return the status name
	 * @see FlightReport#getStatus()
	 */
	public String getStatusName() {
		return STATUS[getStatus()];
	}
	
	/**
	 * Returns the Online Network used on this Flight.
	 * @return an OnlineNetwork enum, or null
	 * @see FlightReport#setNetwork(OnlineNetwork)
	 */
	public OnlineNetwork getNetwork() {
		if (hasAttribute(ATTR_VATSIM))
			return OnlineNetwork.VATSIM;
		else if (hasAttribute(ATTR_IVAO))
			return OnlineNetwork.IVAO;
		else if (hasAttribute(ATTR_PEDGE))
			return OnlineNetwork.PILOTEDGE;
		else if (hasAttribute(ATTR_FPI))
			return OnlineNetwork.FPI;
		
		return null;
	}
	
	/**
	 * Returns the number of passengers carried on this flight.
	 * @return the number of passengers
	 * @see FlightReport#setPassengers(int)
	 */
	public int getPassengers() {
		return _pax;
	}
	
	/**
	 * Returns the load factor for this flight.
	 * @return the load factor from 0 to 1
	 * @see FlightReport#setLoadFactor(double)
	 */
	public double getLoadFactor() {
		return _load;
	}

	/**
	 * Returns the presence of a particular flight attribute.
	 * @param attrMask the attribute to check
	 * @return TRUE if the attribute is present
	 * @see FlightReport#getAttributes()
	 * @see FlightReport#setAttributes(int)
	 * @see FlightReport#setAttribute(int, boolean)
	 */
	public boolean hasAttribute(int attrMask) {
		return ((getAttributes() & attrMask) != 0);
	}
	
	/**
	 * Updates the Online Network used on this Flight.
	 * @param network an OnlineNetwork enum
	 * @see FlightReport#getNetwork()
	 */
	public void setNetwork(OnlineNetwork network) {
		setAttribute(ATTR_VATSIM, (network == OnlineNetwork.VATSIM));
		setAttribute(ATTR_IVAO, (network == OnlineNetwork.IVAO));
		setAttribute(ATTR_FPI, (network == OnlineNetwork.FPI));
		setAttribute(ATTR_PEDGE, (network == OnlineNetwork.PILOTEDGE));
	}

	/**
	 * Updates the rank of the Pilot filing this report.
	 * @param rank the rank
	 * @see FlightReport#getRank()
	 * @see Person#getRank()
	 */
	public void setRank(Rank rank) {
		_rank = rank;
	}

	/**
	 * Sets the remarks for this Flight Report.
	 * @param remarks the remarks
	 * @see FlightReport#getRemarks()
	 * @see FlightReport#setComments(String)
	 */
	public void setRemarks(String remarks) {
		_remarks = remarks;
	}

	/**
	 * Updates the filed Flight Route.
	 * @param rt the route
	 * @see FlightReport#getRoute()
	 */
	public void setRoute(String rt) {
		_route = rt;
	}
	
	/**
	 * Sets the disposition comments for this Flight Report.
	 * @param comments the disposition comments
	 * @see FlightReport#getComments()
	 * @see FlightReport#setRemarks(String)
	 */
	public void setComments(String comments) {
		_comments = comments;
	}

	/**
	 * Sets if this Flight counts towards promotion in a particular equipment program.
	 * @param eqTypes a Collection of equipment program names
	 * @see FlightReport#setCaptEQType(String)
	 */
	public void setCaptEQType(Collection<String> eqTypes) {
		_captEQType.clear();
		if (eqTypes != null)
			_captEQType.addAll(eqTypes);
	}

	/**
	 * Sets if this Flight counts towards promotion in a particular equipment program.
	 * @param eqType an equipment program names
	 * @see FlightReport#setCaptEQType(Collection)
	 */
	public void setCaptEQType(String eqType) {
		_captEQType.add(eqType);
	}

	/**
	 * Sets the Flight Leg. Overrides the superclass implementation to check for zero values.
	 * @param leg the Flight Leg
	 */
	@Override
	public final void setLeg(int leg) {
		super.setLeg(Math.max(1, leg));
	}

	/**
	 * Sets the length of this Flight, in <i>hours multiplied by 10</i>.
	 * @param length the length of the flight, in <i>hours multiplied by 10</i>.
	 */
	public void setLength(int length) {
		if ((length < 0) || (length > 195))
			_length = 195;
		else
			_length = length;
	}

	/**
	 * Sets the status code of this Flight Report.
	 * @param status the new Status Code for this Flight Report
	 * @throws IllegalArgumentException if the status code is negative or invalid
	 * @see FlightReport#setStatus(String)
	 * @see FlightReport#getStatus()
	 */
	public void setStatus(int status) {
		if ((status < 0) || (status >= FlightReport.STATUS.length))
			throw new IllegalArgumentException("Invalid PIREP status - " + status);

		_status = status;
	}

	/**
	 * Sets the status of this Flight Report.
	 * @param status the new Status for this Flight Report
	 * @throws IllegalArgumentException if the status code is not in Flight.STATUS
	 * @see FlightReport#setStatus(int)
	 * @see FlightReport#getStatus()
	 */
	public void setStatus(String status) {
		for (int x = 0; x < FlightReport.STATUS.length; x++) {
			if (FlightReport.STATUS[x].equals(status)) {
				setStatus(x);
				return;
			}
		}

		throw new IllegalArgumentException("Invalid PIREP status - " + status);
	}

	/**
	 * Sets the attributes for this Flight Report.
	 * @param attrs the new attributes
	 * @see FlightReport#setAttribute(int, boolean)
	 * @see FlightReport#getAttributes()
	 */
	public void setAttributes(int attrs) {
		_attr = attrs;
	}

	/**
	 * Set/Clear a particular attribute for this Flight Report.
	 * @param attrMask the Attribute Mask to set
	 * @param isSet TRUE if attribute should be set, FALSE if attribute should be cleared
	 * @see FlightReport#setAttributes(int)
	 * @see FlightReport#getAttributes()
	 */
	public void setAttribute(int attrMask, boolean isSet) {
		_attr = (isSet) ? (_attr | attrMask) : (_attr & (~attrMask));
	}

	/**
	 * Updates the Simulator used for this flight.
	 * @param sim the Simulator
	 * @see FlightReport#getSimulator()
	 */
	public void setSimulator(Simulator sim) {
		_fsVersion = sim;
	}

	/**
	 * Updates the date that this Flight was flown on.
	 * @param dt when this flight was flown. The time component is undefined.
	 * @see FlightReport#getDate()
	 */
	public void setDate(Instant dt) {
		_date = dt;
	}

	/**
	 * Updates the date/time this Flight Report was submitted on.
	 * @param sd this Flight Report was submitted for approval.
	 * @see FlightReport#getSubmittedOn()
	 */
	public void setSubmittedOn(Instant sd) {
		_submittedOn = sd;
	}

	/**
	 * Updates the date/time this Flight Report was approved or rejected on.
	 * @param dd when this Flight Report was approved or rejected.
	 * @see FlightReport#getDisposedOn()
	 */
	public void setDisposedOn(Instant dd) {
		_disposedOn = dd;
	}
	
	/**
	 * Updates the number of passengers carried on this flight.
	 * @param ps the number of passengers
	 * @see FlightReport#getPassengers()
	 */
	public void setPassengers(int ps) {
		_pax = Math.max(0, ps);
	}
	
	/**
	 * Updates the load factor for this flight.
	 * @param lf the load factor
	 * @see FlightReport#getLoadFactor()
	 */
	public void setLoadFactor(double lf) {
		_load = Math.max(0, Math.min(1, lf));
	}

	/**
	 * Sets the database row ID of a relatied database row. This row may be present in the <i>PILOTS </i>,
	 * <i>ASSIGNMENTS </i>, <i>ACARS_PIREPS </i> or <i>EVENTS </i> table. <i>This is typically used by a DAO.</i>
	 * @param idType the row ID type
	 * @param id the database row ID
	 * @throws IllegalArgumentException if the id is negative
	 * @see FlightReport#getDatabaseID(DatabaseID)
	 */
	public void setDatabaseID(DatabaseID idType, int id) {
		if (id < 0)
			throw new IllegalArgumentException(idType + " Datbase ID cannot be negative");

		_dbIds.put(idType, Integer.valueOf(id));
	}
	
	@Override
	public void setAuthorID(int id) {
		setDatabaseID(DatabaseID.PILOT, id);
	}

	/**
	 * Compare two Flight Reports by comparing their date/time.
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(Object o) {
		FlightReport fr2 = (FlightReport) o;
		int tmpResult = Integer.valueOf(getID()).compareTo(Integer.valueOf(fr2.getID()));
		if ((tmpResult == 0) && (_date != null) && (fr2.getDate() != null))
			tmpResult = _date.compareTo(fr2.getDate());
		return (tmpResult == 0) ? super.compareTo(fr2) : tmpResult;
	}

	/**
	 * Selects a table row class based upon the Flight Report status.
	 * @return the row CSS class name
	 */
	@Override
	public String getRowClassName() {
		if (hasAttribute(ATTR_ACADEMY))
			return "opt4";
		if (hasAttribute(ATTR_CHECKRIDE))
			return "opt3";
		
		return ROW_CLASSES[_status];
	}
}
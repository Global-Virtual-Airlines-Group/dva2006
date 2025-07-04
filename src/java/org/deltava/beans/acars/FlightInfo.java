// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2016, 2017, 2018, 2019, 2021, 2022, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;
import java.time.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.system.OperatingSystem;

import org.deltava.util.*;

/**
 * A bean to store ACARS Flight Information records.
 * @author Luke
 * @version 10.5
 * @since 1.0
 */

public class FlightInfo extends ACARSLogEntry implements FlightData, TimeSpan, ViewEntry {

	private Airline _a;
	private int _flight;
	
	private int _pilotID;
	private int _positionCount;

	private Instant _startTime;
	private Instant _endTime;

	private String _eqType;
	private String _alt;

	private Airport _airportD;
	private Airport _airportA;
	private Airport _airportL;

	private Gate _gateD;
	private Runway _rwyD;
	private TerminalRoute _sid;
	
	private Gate _gateA;
	private Runway _rwyA;
	private TerminalRoute _star;

	private String _route;
	private String _remarks;

	private Simulator _sim;
	private int _simMajor;
	private int _simMinor;
	private OperatingSystem _os = OperatingSystem.WINDOWS;
	private boolean _isSim64Bit;
	private boolean _isACARS64Bit;
	private AutopilotType _ap = AutopilotType.DEFAULT;
	
	private boolean _offline;
	private boolean _scheduleValidated;
	private DispatchType _dispatcher = DispatchType.NONE;
	private boolean _hasPIREP;
	private boolean _archived;
	private boolean _isMP;
	private Recorder _fdr;

	private int _dispatcherID;
	private int _routeID;
	private int _dispatchLogID;
	
	private int _txCode;
	private double _loadFactor;
	private int _pax;
	private int _seats;
	private LoadType _loadType = LoadType.RANDOM;

	private RouteEntry _lastPosition;
	private SortedSet<RouteEntry> _routeData;
	private Collection<NavigationDataBean> _planData;

	/**
	 * Creates a new Flight Information record.
	 * @param id the flight ID
	 * @throws IllegalArgumentException if id or conID are zero or negative
	 */
	public FlightInfo(int id) {
		super();
		if (id != 0)
			setID(id);
	}

	@Override
	public int getAuthorID() {
		return _pilotID;
	}
	
	@Override
	public Airline getAirline() {
		return _a;
	}
	
	@Override
	public int getFlightNumber() {
		return _flight;
	}
	
	@Override
	public int getLeg() {
		return 1;
	}
	
	@Override
	public OnlineNetwork getNetwork() {
		return null;
	}
	
	/**
	 * Returns the flight's dispatcher ID.
	 * @return the database ID of the dispatcher, or zero if none
	 * @see FlightInfo#getAuthorID()
	 */
	public int getDispatcherID() {
		return _dispatcherID;
	}

	/**
	 * Returns the database ID of the Dispatch route used.
	 * @return the route database ID
	 */
	public int getRouteID() {
		return _routeID;
	}
	
	/**
	 * Returns the database ID of the dispatch log entry.
	 * @return the log entry database ID
	 */
	public int getDispatchLogID() {
		return _dispatchLogID;
	}

	@Override
	public Instant getDate() {
		return _startTime;
	}
	
	@Override
	public Instant getStartTime() {
		return _startTime;
	}

	@Override
	public Instant getEndTime() {
		return _endTime;
	}
	
	@Override
	public Duration getDuration() {
		return (_endTime == null) ? Duration.between(_startTime, Instant.now()) : TimeSpan.super.getDuration();
	}

	/**
	 * Returns the flight code for this flight.
	 * @return the flight code (eg. DVA123)
	 * @see FlightInfo#setFlightCode(String)
	 */
	public String getFlightCode() {
		if (_a == null) return null;
		StringBuilder buf = new StringBuilder(_a.getCode());
		buf.append(StringUtils.format(_flight, "#000"));
		return buf.toString();
	}

	@Override
	public String getEquipmentType() {
		return _eqType;
	}
	
	/**
	 * Returns the filed altitude for this flight.
	 * @return the altitude in feet or a flight level
	 * @see FlightInfo#setAltitude(String)
	 */
	public String getAltitude() {
		return _alt;
	}

	/**
	 * Returns the destination Airport for this flight.
	 * @return the destination Airport bean
	 * @see FlightInfo#setAirportA(Airport)
	 * @see FlightInfo#getAirportD()
	 */
	@Override
	public Airport getAirportA() {
		return _airportA;
	}
	
	/**
	 * Returns the arrival gate for this flight.
	 * @return the arrival Gate bean, or null if unknown
	 * @see FlightInfo#setGateA(Gate)
	 * @see FlightInfo#getGateD()
	 */
	public Gate getGateA() {
		return _gateA;
	}

	/**
	 * Returns the arrival Runway for this flight.
	 * @return the arrival Runway, or null if unknown
	 * @see FlightInfo#setRunwayA(Runway)
	 * @see FlightInfo#getRunwayD()
	 */
	public Runway getRunwayA() {
		return _rwyA;
	}

	/**
	 * Returns the origin Airport for this flight.
	 * @return the origin Airport bean
	 * @see FlightInfo#setAirportD(Airport)
	 * @see FlightInfo#getAirportA()
	 */
	@Override
	public Airport getAirportD() {
		return _airportD;
	}

	/**
	 * Returns the departure gate for this flight.
	 * @return the origin Gate bean, or null if unknown
	 * @see FlightInfo#setGateD(Gate)
	 * @see FlightInfo#getGateA()
	 */
	public Gate getGateD() {
		return _gateD;
	}
	
	/**
	 * Returns the departure Runway for this flight.
	 * @return the departure Runway, or null if unknown
	 * @see FlightInfo#setRunwayD(Runway)
	 * @see FlightInfo#getRunwayA()
	 */
	public Runway getRunwayD() {
		return _rwyD;
	}

	/**
	 * Returns the divert Airport for this flight.
	 * @return the alternate Airport bean
	 * @see FlightInfo#setAirportL(Airport)
	 */
	public Airport getAirportL() {
		return _airportL;
	}

	/**
	 * Returns the Departure Route for this flight.
	 * @return the SID TerminalRoute bean
	 * @see FlightInfo#setSID(TerminalRoute)
	 * @see FlightInfo#getSTAR()
	 */
	public TerminalRoute getSID() {
		return _sid;
	}

	/**
	 * Returns the Arrival Route for this flight.
	 * @return the STAR TerminalRoute bean
	 * @see FlightInfo#setSTAR(TerminalRoute)
	 * @see FlightInfo#getSID()
	 */
	public TerminalRoute getSTAR() {
		return _star;
	}

	/**
	 * Returns the filed route for this flight.
	 * @return the route
	 * @see FlightInfo#setRoute(String)
	 */
	public String getRoute() {
		return _route;
	}

	/**
	 * Returns the pilot's remarks for this flight.
	 * @return the remarks
	 * @see FlightInfo#setRemarks(String)
	 */
	public String getRemarks() {
		return _remarks;
	}
	
	/**
	 * Returns the type of Autopilot used in the aircraft.
	 * @return an AutopilotType
	 * @see FlightInfo#setAutopilotType(AutopilotType)
	 */
	public AutopilotType getAutopilotType() {
		return _ap;
	}

	@Override
	public Simulator getSimulator() {
		return _sim;
	}
	
	/**
	 * Returns the major version number of the Simulator used in this flight.
	 * @return the major version, or zero if unknown
	 */
	public int getSimMajor() {
		return _simMajor;
	}
	
	/**
	 * Returns the minor version number of the Simulator used in this flight.
	 * @return the minor version
	 */
	public int getSimMinor() {
		return _simMinor;
	}
	
	/**
	 * Returns the underlying simulator platform.
	 * @return an OperatingSystem
	 */
	public OperatingSystem getPlatform() {
		return _os;
	}
	
	/**
	 * Returns whether the simulator platform is 64-bit.
	 * @return TRUE if 64-bit, otherwise FALSE
	 */
	public boolean getIsSim64Bit() {
		return _isSim64Bit;
	}
	
	/**
	 * Returns whether the ACARS client is 64-bit.
	 * @return TRUE if 64-bit, otherwise FALSE
	 */
	public boolean getIsACARS64Bit() {
		return _isACARS64Bit;
	}

	/**
	 * Returns if this flight was flown disconnected from the ACARS server.
	 * @return TRUE if the flight was flown offline, otherwise FALSE
	 * @see FlightInfo#setOffline(boolean)
	 */
	public boolean getOffline() {
		return _offline;
	}

	/**
	 * Returns if this flight was flown using ACARS multi-player.
	 * @return TRUE if multi-player, otherwise FALSE
	 * @see FlightInfo#setIsMP(boolean)
	 */
	public boolean getIsMP() {
		return _isMP;
	}

	/**
	 * Returns if this flight has an associated Flight Report.
	 * @return TRUE if a Flight Report was filed, otherwise FALSE
	 * @see FlightInfo#setHasPIREP(boolean)
	 */
	public boolean getHasPIREP() {
		return _hasPIREP;
	}

	/**
	 * Returns if this flight's position data is stored in the archive.
	 * @return TRUE if the Position data is in the archive, otherwise FALSE
	 * @see FlightInfo#setArchived(boolean)
	 */
	public boolean getArchived() {
		return _archived;
	}

	/**
	 * Returns the transponder code used on this Flight.
	 * @return the transponder code
	 * @see FlightInfo#setTXCode(int)
	 */
	public int getTXCode() {
		return _txCode;
	}
	
	/**
	 * Returns the load factor for this flight.
	 * @return the load factor, or 0 if not set
	 * @see FlightInfo#setLoadFactor(double)
	 */
	public double getLoadFactor() {
		return _loadFactor;
	}
	
	/**
	 * Returns the number of passengers on the flight.
	 * @return the number of passengers
	 * @see FlightInfo#setPassengers(int)
	 */
	public int getPassengers() {
		return _pax;
	}
	
	/**
	 * Returns the number of available seats on this flight.
	 * @return the number of seats
	 * @see FlightInfo#setSeats(int)
	 */
	public int getSeats() {
		return _seats;
	}
	
	/**
	 * Returns the mechanism used to generate the load factor.
	 * @return a LoadType
	 * @see FlightInfo#setLoadType(LoadType)
	 */
	public LoadType getLoadType() {
		return _loadType;
	}
	
	/**
	 * Returns the number of position records associated with this Flight.
	 * @return the number of positions
	 * @see FlightInfo#setPositionCount(int)
	 */
	public int getPositionCount() {
		return hasRouteData() ? _routeData.size() : _positionCount;
	}
	
	/**
	 * Returns the last logged position for this flight.
	 * @return the latest PositionEntry, or NULL if no route data
	 */
	public RouteEntry getPosition() {
		if (_lastPosition != null)
			return _lastPosition;

		return hasRouteData() ? _routeData.last() : null;
	}

	/**
	 * Returns the actual route data for this flight.
	 * @return a Collection of RouteEntry beans, or null
	 * @see FlightInfo#setRouteData(Collection)
	 * @see FlightInfo#hasRouteData()
	 */
	public Collection<RouteEntry> getRouteData() {
		return _routeData;
	}

	/**
	 * Returns the filed route for this flight.
	 * @return a Collection of NavigationDataBeans, or null
	 * @see FlightInfo#setPlanData(Collection)
	 * @see FlightInfo#hasPlanData()
	 */
	public Collection<NavigationDataBean> getPlanData() {
		return _planData;
	}

	/**
	 * Returns if this bean contains route data.
	 * @return TRUE if route data exists within the bean, otherwise FALSE
	 * @see FlightInfo#getRouteData()
	 * @see FlightInfo#setRouteData(Collection)
	 */
	public boolean hasRouteData() {
		return !CollectionUtils.isEmpty(_routeData);
	}

	/**
	 * Returns if this bean contains flight plan data.
	 * @return TRUE if flight plan data exists within the bean, otherwise FALSE
	 * @see FlightInfo#getPlanData()
	 * @see FlightInfo#setPlanData(Collection)
	 */
	public boolean hasPlanData() {
		return !CollectionUtils.isEmpty(_planData);
	}

	/**
	 * Returns if runway data for this flight has been loaded.
	 * @return TRUE if runway data exists, otherwise FALSE
	 * @see FlightInfo#getRunwayA()
	 * @see FlightInfo#getRunwayD()
	 */
	public boolean hasRunwayData() {
		return (_rwyD != null) || (_rwyA != null);
	}

	/**
	 * Returns if this flight has been validated as being in the schedule.
	 * @return TRUE if the route was validated, otherwise FALSE
	 */
	public boolean isScheduleValidated() {
		return _scheduleValidated;
	}

	/**
	 * Returns the dispatcher type for this Flight.
	 * @return a DispatchType
	 */
	public DispatchType getDispatcher() {
		return _dispatcher;
	}

	@Override
	public Recorder getFDR() {
		return _fdr;
	}

	/**
	 * Updates the Pilot ID for the flight.
	 * @param id the database ID of the pilot flying this flight
	 */
	@Override
	public void setAuthorID(int id) {
		_pilotID = Math.max(0, id);
	}
	
	/**
	 * Updates the Airline for the flight.
	 * @param a the Airline
	 */
	public void setAirline(Airline a) {
		_a = a;
	}
	
	/**
	 * Updates the flight number for the flight.
	 * @param flight the flight number
	 */
	public void setFlight(int flight) {
		_flight = flight;
	}

	/**
	 * Updates the Disaptcher ID for the flight.
	 * @param id the database ID of the dispatcher, or zero if none
	 * @see FlightInfo#getDispatcherID()
	 */
	public void setDispatcherID(int id) {
		_dispatcherID = Math.max(0, id);
	}

	/**
	 * Updates the Dispatch Route ID used in this flight.
	 * @param id the database ID of the route, or zero if none
	 * @see FlightInfo#getRouteID()
	 */
	public void setRouteID(int id) {
		_routeID = Math.max(0, id);
	}
	
	/**
	 * Updates the Dispatch Log ID used in this flight.
	 * @param id the database ID of the dispatch data, or zero if none
	 * @see FlightInfo#getDispatchLogID()
	 */
	public void setDispatchLogID(int id) {
		_dispatchLogID = id;
	}

	/**
	 * Updates whether this flight was flown disconnected from the ACARS server.
	 * @param offline TRUE if the flight was flown offline, otherwise FALSE
	 * @see FlightInfo#getOffline()
	 */
	public void setOffline(boolean offline) {
		_offline = offline;
	}

	/**
	 * Updates whether this flight was flown using ACARS multi-player.
	 * @param isMP TRUE if multi-player, otherwise FALSE
	 * @see FlightInfo#getIsMP()
	 */
	public void setIsMP(boolean isMP) {
		_isMP = isMP;
	}

	/**
	 * Updates whether this flight has an associated Flight Report.
	 * @param hasPIREP TRUE if a Flight Report was filed, otherwise FALSE
	 * @see FlightInfo#getHasPIREP()
	 */
	public void setHasPIREP(boolean hasPIREP) {
		_hasPIREP = hasPIREP;
	}

	/**
	 * Marks this Flight's position data as archived.
	 * @param archived TRUE if the position data is in the archive, otherwise FALSE
	 * @see FlightInfo#getArchived()
	 */
	public void setArchived(boolean archived) {
		_archived = archived;
	}

	/**
	 * Marks this Flight as having a valid flight route.
	 * @param isOK TRUE if the Flight is valid, otherwise FALSE
	 */
	public void setScheduleValidated(boolean isOK) {
		_scheduleValidated = isOK;
	}

	/**
	 * Marks this Flight as being planned by a Dispatcher.
	 * @param dsp the DispatchType
	 */
	public void setDispatcher(DispatchType dsp) {
		_dispatcher = dsp;
	}

	/**
	 * Updates the start date/time for this flight.
	 * @param dt the date/time the flight started
	 * @see FlightInfo#getStartTime()
	 * @see FlightInfo#setEndTime(Instant)
	 */
	public void setStartTime(Instant dt) {
		_startTime = dt;
	}

	/**
	 * Updates the end date/time for this flight.
	 * @param dt the date/time the flight ended
	 * @see FlightInfo#getEndTime()
	 * @see FlightInfo#setStartTime(Instant)
	 */
	public void setEndTime(Instant dt) {
		_endTime = ((dt != null) && dt.isBefore(_startTime)) ? _startTime : dt;
	}
	
	/**
	 * Updates the destination Airport for this flight.
	 * @param a an Airport bean
	 * @see FlightInfo#getAirportA()
	 * @see FlightInfo#setAirportD(Airport)
	 */
	public void setAirportA(Airport a) {
		_airportA = a;
	}
	
	/**
	 * Updates the arrival Gate for this flight.
	 * @param g a Gate bean
	 * @see FlightInfo#getGateA()
	 * @see FlightInfo#setGateD(Gate)
	 */
	public void setGateA(Gate g) {
		_gateA = g;
	}

	/**
	 * Updates the arrival Runway for this flight.
	 * @param r a Runway bean
	 * @see FlightInfo#getRunwayA()
	 * @see FlightInfo#setRunwayD(Runway)
	 */
	public void setRunwayA(Runway r) {
		_rwyA = r;
	}

	/**
	 * Updates the origination Airport for this flight.
	 * @param a an Airport bean
	 * @see FlightInfo#getAirportD()
	 * @see FlightInfo#setAirportA(Airport)
	 */
	public void setAirportD(Airport a) {
		_airportD = a;
	}
	
	/**
	 * Updates the departure Gate for this flight.
	 * @param g a Gate bean
	 * @see FlightInfo#getGateD()
	 * @see FlightInfo#setGateA(Gate)
	 */
	public void setGateD(Gate g) {
		_gateD = g;
	}

	/**
	 * Updates the departure Runway for this flight.
	 * @param r a Runway bean
	 * @see FlightInfo#getRunwayD()
	 * @see FlightInfo#setRunwayA(Runway)
	 */
	public void setRunwayD(Runway r) {
		_rwyD = r;
	}

	/**
	 * Updates the alternate Airport for this flight.
	 * @param a an Airport bean
	 * @see FlightInfo#getAirportL()
	 */
	public void setAirportL(Airport a) {
		_airportL = a;
	}

	/**
	 * Updates the Departure route used on this flight.
	 * @param sid the SID TerminalRoute bean
	 * @see FlightInfo#getSID()
	 * @see FlightInfo#setSTAR(TerminalRoute)
	 */
	public void setSID(TerminalRoute sid) {
		_sid = sid;
	}

	/**
	 * Updates the Arrival Route used on this flight.
	 * @param star the STAR TerminalRoute bean
	 * @see FlightInfo#getSTAR()
	 * @see FlightInfo#setSID(TerminalRoute)
	 */
	public void setSTAR(TerminalRoute star) {
		_star = star;
	}

	/**
	 * Updates the filed altitude for this flight.
	 * @param alt the altitude in feet or as a flight level
	 * @see FlightInfo#getAltitude()
	 */
	public void setAltitude(String alt) {
		_alt = alt;
	}

	/**
	 * Updates the aircraft used on this flight.
	 * @param eq the equipment type
	 * @see FlightInfo#getEquipmentType()
	 */
	public void setEquipmentType(String eq) {
		_eqType = eq;
	}
	
	/**
	 * Updates the load factor for this flight.
	 * @param lf the load factor
	 * @see FlightInfo#getLoadFactor()
	 */
	public void setLoadFactor(double lf) {
		_loadFactor = Math.max(-1, Math.max(1, lf));
	}
	
	/**
	 * Updates the number of passengers on this flight.
	 * @param pax the number of passengers
	 * @see FlightInfo#getPassengers()
	 */
	public void setPassengers(int pax) {
		_pax = Math.max(0, pax);
	}
	
	/**
	 * Updates the number of seats on this flight.
	 * @param seats the number of seats
	 * @see FlightInfo#getSeats()
	 */
	public void setSeats(int seats) {
		_seats = Math.max(0, seats);
	}
	
	/**
	 * Updates the mechanism used to generate the load factor for this flight.
	 * @param lt a LoadType
	 * @see FlightInfo#getLoadType()
	 */
	public void setLoadType(LoadType lt) {
		_loadType = lt;
	}
	
	/**
	 * Updates the autopilot type used in the aircraft.
	 * @param ap an AutoPilotType
	 * @see FlightInfo#getAutopilotType()
	 */
	public void setAutopilotType(AutopilotType ap) {
		_ap = ap;
	}

	/**
	 * Updates the Simulator used in this flight.
	 * @param sim the Simulator
	 * @see FlightInfo#getSimulator()
	 */
	public void setSimulator(Simulator sim) {
		_sim = sim;
		if ((sim == Simulator.XP9) || (sim == Simulator.XP10))
			_fdr = Recorder.XACARS;
		else if ((sim == Simulator.P3Dv4) || (sim == Simulator.XP11) || (sim == Simulator.FS2020))
			_isSim64Bit = true;
	}

	/**
	 * Updates the simulator version used in this flight.
	 * @param major the major version
	 * @param minor the minor version
	 */
	public void setSimulatorVersion(int major, int minor) {
		_simMajor = Math.max(1, major);
		_simMinor = Math.max(0, minor);
	}
	
	/**
	 * Updates the underlying simulator platform.
	 * @param os an OperatingSystem
	 */
	public void setPlatform(OperatingSystem os) {
		_os = os;
	}
	
	/**
	 * Updates whether the simulator is a 64-bit application.
	 * @param is64 TRUE if 64-bit, otherwise FALSE
	 */
	public void setIsSim64Bit(boolean is64) {
		if ((_sim != Simulator.P3Dv4) && (_sim != Simulator.XP11))
			_isSim64Bit = is64;
	}
	
	/**
	 * Updates whether ACARS is a 64-bit application.
	 * @param is64 TRUE if 64-bit, otherwise FALSE
	 */
	public void setIsACARS64Bit(boolean is64) {
		_isACARS64Bit = is64;
	}

	/**
	 * Updates the flight number for this flight.
	 * @param code the flight code
	 * @throws NullPointerException if code is null
	 * @see FlightInfo#getFlightCode()
	 */
	@Deprecated
	public void setFlightCode(String code) {
		Flight f = FlightCodeParser.parse(code);
		_a = f.getAirline();
		_flight = f.getFlightNumber();
	}

	/**
	 * Sets the Flight Data Recorder used on this Flight.
	 * @param r the Recorder
	 */
	public void setFDR(Recorder r) {
		_fdr = r;
	}

	/**
	 * Updates the filed route for this flight.
	 * @param route the filed route
	 * @see FlightInfo#getRoute()
	 */
	public void setRoute(String route) {
		_route = route.replaceAll("[.]+", " ");
	}

	/**
	 * Updates the pilot's remarks for this flight.
	 * @param remarks the pilot's remarks
	 * @see FlightInfo#getRemarks()
	 */
	public void setRemarks(String remarks) {
		_remarks = remarks;
	}
	
	/**
	 * Updates the transponder code used on this flight.
	 * @param tx the transponder code
	 * @see FlightInfo#getTXCode()
	 */
	public void setTXCode(int tx) {
		_txCode = tx;
	}

	/**
	 * Updates the number of position records for this Flight.
	 * @param posCount the number of records
	 * @see FlightInfo#getPositionCount()
	 */
	public void setPositionCount(int posCount) {
		_positionCount = posCount;
	}

	/**
	 * Sets the current position of the flight. This is used to override the progress data.
	 * @param pos the current position
	 */
	public void setPosition(RouteEntry pos) {
		_lastPosition = pos;
	}

	/**
	 * Updates the actual route data for this flight.
	 * @param entries a Collection of RouteEntry beans
	 * @see FlightInfo#getRouteData()
	 * @see FlightInfo#hasRouteData()
	 */
	public void setRouteData(Collection<? extends RouteEntry> entries) {
		_routeData = new TreeSet<RouteEntry>(entries);
	}

	/**
	 * Updates the flight plan data for this flight.
	 * @param entries a Collection of NavigationDatbBeans
	 * @see FlightInfo#getPlanData()
	 * @see FlightInfo#hasPlanData()
	 */
	public void setPlanData(Collection<NavigationDataBean> entries) {
		_planData = entries;
	}

	/**
	 * Displays the CSS class name for this table row.
	 * @return the CSS class name
	 */
	@Override
	public String getRowClassName() {
		return ((_endTime != null) && !_hasPIREP) ? "warn" : null;
	}
}
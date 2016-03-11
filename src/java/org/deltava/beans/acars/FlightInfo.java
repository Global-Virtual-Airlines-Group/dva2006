// Copyright 2005, 2006, 2007, 2008, 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.flight.Recorder;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.util.*;

/**
 * A bean to store ACARS Flight Information records.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class FlightInfo extends ACARSLogEntry implements TimeSpan, RoutePair, ViewEntry {

	private int _pilotID;
	private int _positionCount;

	private Date _startTime;
	private Date _endTime;

	private String _flightCode;
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

	private Simulator _fsVersion;
	private boolean _offline;
	private boolean _scheduleValidated;
	private boolean _dispatchPlan;
	private boolean _hasPIREP;
	private boolean _archived;
	private boolean _isMP;
	private Recorder _fdr;

	private int _dispatcherID;
	private int _routeID;

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

	@Override
	public Date getDate() {
		return _startTime;
	}
	
	@Override
	public Date getStartTime() {
		return _startTime;
	}

	@Override
	public Date getEndTime() {
		return _endTime;
	}

	/**
	 * Returns the flight code for this flight.
	 * @return the flight code (eg. DVA123)
	 * @see FlightInfo#setFlightCode(String)
	 */
	public String getFlightCode() {
		return _flightCode;
	}

	/**
	 * Returns the aircraft type for this flight.
	 * @return the equipment code
	 * @see FlightInfo#setEquipmentType(String)
	 */
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
	 * Returns the distance between the Airports.
	 */
	@Override
	public int getDistance() {
		return GeoUtils.distance(_airportD, _airportA);
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
	 * Returns the version of the Simulator used in this flight.
	 * @return the Simulator
	 * @see FlightInfo#setFSVersion(Simulator)
	 */
	public Simulator getFSVersion() {
		return _fsVersion;
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
	 * Returns if this flight was planned by a Dispatcher.
	 * @return TRUE if planned by a Dispatcher, otherwise FALSE
	 */
	public boolean isDispatchPlan() {
		return _dispatchPlan;
	}

	/**
	 * Returns the Flight Data Recorder used.
	 * @return a Recorder
	 */
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
	 * @param isDP TRUE if planned by a Dispatcher, otherwise FALSE
	 */
	public void setDispatchPlan(boolean isDP) {
		_dispatchPlan = isDP;
	}

	/**
	 * Updates the start date/time for this flight.
	 * @param dt the date/time the flight started
	 * @see FlightInfo#getStartTime()
	 * @see FlightInfo#setEndTime(Date)
	 */
	public void setStartTime(Date dt) {
		_startTime = dt;
	}

	/**
	 * Updates the end date/time for this flight.
	 * @param dt the date/time the flight ended
	 * @see FlightInfo#getEndTime()
	 * @see FlightInfo#setStartTime(Date)
	 */
	public void setEndTime(Date dt) {
		_endTime = ((dt != null) && (dt.before(_startTime))) ? _startTime : dt;
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
	 * Updates the Simulator version used in this flight.
	 * @param sim the Simulator
	 * @see FlightInfo#getFSVersion()
	 */
	public void setFSVersion(Simulator sim) {
		_fsVersion = sim;
		if ((sim == Simulator.XP9) || (sim == Simulator.XP10))
			_fdr = Recorder.XACARS;
	}

	/**
	 * Updates the flight number for this flight.
	 * @param code the flight code
	 * @throws NullPointerException if code is null
	 * @see FlightInfo#getFlightCode()
	 */
	public void setFlightCode(String code) {
		_flightCode = code.toUpperCase();
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
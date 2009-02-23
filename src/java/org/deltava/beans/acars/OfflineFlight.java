// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.beans.ACARSFlightReport;

/**
 * A bean to store data about a submitted offline ACARS Flight Report.
 * @author Luke
 * @version 2.4
 * @since 2.4
 */

public class OfflineFlight {

	private ConnectionEntry _con;
	private FlightInfo _flight;
	private final Collection<RouteEntry> _positions = new ArrayList<RouteEntry>();
	
	private String _sid;
	private String _star;

	private ACARSFlightReport _pirep;
	
	/**
	 * Returns the ACARS connection entry.
	 * @return the ConnectionEntry bean
	 * @see OfflineFlight#setConnection(ConnectionEntry)
	 */
	public ConnectionEntry getConnection() {
		return _con;
	}
	
	/**
	 * Returns the Flight Information.
	 * @return the FlightIno bean
	 * @see OfflineFlight#setFlightReport(ACARSFlightReport)
	 */
	public FlightInfo getInfo() {
		return _flight;
	}
	
	/**
	 * Returns the position entries.
	 * @return a Collection of RouteEntry beans
	 * @see OfflineFlight#addPosition(RouteEntry)
	 */
	public Collection<RouteEntry> getPositions() {
		return _positions;
	}
	
	/**
	 * Returns the Flight Report.
	 * @return an ACARSFlightReport bean
	 * @see OfflineFlight#setFlightReport(ACARSFlightReport)
	 */
	public ACARSFlightReport getFlightReport() {
		return _pirep;
	}
	
	/**
	 * Returns the SID ID. This property exists because FlightInfo can only
	 * store a TerminalRoute bean, not just the ID.
	 * @return the SID ID
	 * @see OfflineFlight#setSID(String)
	 */
	public String getSID() {
		return _sid;
	}
	
	/**
	 * Returns the STAR ID. This property exists because FlightInfo can only
	 * store a TerminalRoute bean, not just the ID.
	 * @return the STAR ID
	 * @see OfflineFlight#setSTAR(String)
	 */
	public String getSTAR() {
		return _star;
	}
	
	/**
	 * Updates the Connection entry.
	 * @param ce a ConnectionEntry bean
	 * @see OfflineFlight#getConnection()
	 */
	public void setConnection(ConnectionEntry ce) {
		_con = ce;
	}
	
	/**
	 * Updates the Flight information.
	 * @param inf a FlightInfo bean
	 * @see OfflineFlight#getInfo()
	 */
	public void setInfo(FlightInfo inf) {
		_flight = inf;
	}
	
	/**
	 * Adds a position record.
	 * @param e a RouteEntry bean
	 * @see OfflineFlight#getPositions()
	 */
	public void addPosition(RouteEntry e) {
		_positions.add(e);
	}
	
	/**
	 * Updates the Flight Report.
	 * @param afr an ACARSFlightReport bean
	 * @see OfflineFlight#getFlightReport()
	 */
	public void setFlightReport(ACARSFlightReport afr) {
		_pirep = afr;
	}
	
	/**
	 * Sets the SID ID for the flight.
	 * @param id the SID ID
	 */
	public void setSID(String id) {
		_sid = id;
	}
	
	/**
	 * Sets the STAR ID for the flight.
	 * @param id the STAR ID
	 */
	public void setSTAR(String id) {
		_star = id;
	}
}
// Copyright 2009, 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

import org.deltava.beans.flight.FDRFlightReport;

import org.deltava.comparators.CalendarEntryComparator;

/**
 * A bean to store data about a submitted offline ACARS Flight Report.
 * @author Luke
 * @version 7.2
 * @since 2.4
 * @param <T> the FlightReport type 
 * @param <P> the RotueEntry type
 */

public class OfflineFlight<T extends FDRFlightReport, P extends RouteEntry> {

	private FlightInfo _flight;
	private final SortedSet<P> _positions = new TreeSet<P>(new CalendarEntryComparator());
	
	private String _sid;
	private String _star;

	private T _pirep;
	
	/**
	 * Returns the Flight Information.
	 * @return the FlightIno bean
	 */
	public FlightInfo getInfo() {
		return _flight;
	}
	
	/**
	 * Returns the position entries.
	 * @return a SortedSet of RouteEntry beans
	 * @see OfflineFlight#addPosition(RouteEntry)
	 */
	public SortedSet<P> getPositions() {
		return _positions;
	}
	
	/**
	 * Returns the Flight Report.
	 * @return an FDRFlightReport bean
	 */
	public T getFlightReport() {
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
	public void addPosition(P e) {
		_positions.add(e);
	}
	
	/**
	 * Updates the Flight Report.
	 * @param fr an FDRFlightReport bean
	 * @see OfflineFlight#getFlightReport()
	 */
	public void setFlightReport(T fr) {
		_pirep = fr;
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
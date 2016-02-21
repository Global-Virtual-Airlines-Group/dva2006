// Copyright 2009, 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

/**
 * An ACARS Connection log entry for Dispatchers.
 * @author Luke
 * @version 6.4
 * @since 2.4
 */

public class DispatchConnectionEntry extends ConnectionEntry {
	
	private final Collection<FlightInfo> _flights = new TreeSet<FlightInfo>();

	/**
	 * Creates a new Dispatch Connection entry.
	 * @param id the Connection ID
	 */
	public DispatchConnectionEntry(long id) {
		super(id);
	}

	/**
	 * Returns if this is a Dispatch connection.
	 * @return TRUE
	 */
	@Override
	public final boolean getDispatch() {
		return true;
	}
	
	/**
	 * Returns whether this connection dispatched any flights.
	 * @return TRUE if flights dispatched, otherwise FALSE
	 */
	public boolean getHasFlights() {
		return (_flights.size() > 0);
	}
	
	/**
	 * Returns Flights dispatched during this connection.
	 * @return a Collection of FlightInfo beans
	 */
	public Collection<FlightInfo> getFlights() {
		return _flights;
	}
	
	/**
	 * Adds dispatched Flights to this Connection entry.
	 * @param info a Collection of FlightInfo beans
	 */
	public void addFlights(Collection<FlightInfo> info) {
		_flights.addAll(info);
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(getDate().toString());
		buf.append('-');
		buf.append(getAuthorID());
		return buf.toString();
	}
}
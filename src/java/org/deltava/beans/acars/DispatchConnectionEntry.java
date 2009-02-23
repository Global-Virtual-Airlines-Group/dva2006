// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import java.util.*;

/**
 * An ACARS Connection log entry for Dispatchers.
 * @author Luke
 * @version 2.4
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
	 * Adds a dispatched Flight to this Connection entry.
	 * @param info a FlightInfo bean
	 */
	public void addFlight(FlightInfo info) {
		_flights.add(info);
	}
}
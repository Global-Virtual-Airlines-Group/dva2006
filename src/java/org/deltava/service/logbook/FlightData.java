// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import org.deltava.beans.acars.RouteEntry;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.schedule.Aircraft;

/**
 * A record to store Flight Data for serialization.
 * @author Luke
 * @version 12.4
 * @param PIREP the FlightReport
 * @param aircraft the Aircraft used 
 * @param positions a Collection of RouteEntry objects
 * @param error an optional error string
 * @since 12.4
 */

record FlightData(FlightReport PIREP, Aircraft aircraft, java.util.SequencedCollection<RouteEntry> positions, String error) { 

	/**
	 * Returns the Flight ID.
	 * @return the ID
	 */
	public int getID() {
		return PIREP.getID();
	}
}
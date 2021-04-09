// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.schedule.RoutePair;

/**
 * A bean to store information about a Flight, designed to provide a common interface for classes that operate on FlightReports
 * and ACARS FlightInfo beans. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public interface FlightData extends FlightNumber, RoutePair {

	/**
	 * Returns the equipment type used.
	 * @return the equipment type
	 */
	public String getEquipmentType();
	
	/**
	 * Returns the OnlineNetwork the flight is operated on.
	 * @return an OnlineNetwork or null if offline
	 */
	public OnlineNetwork getNetwork();
	
	/**
	 * Returns the Simulator used for this flight.
	 * @return a Simulator
	 */
	public Simulator getSimulator();
	
	/**
	 * Returns the Flight Data recorder used for this flight.
	 * @return a Recorder enum or null if none/unknown
	 */
	public Recorder getFDR();
	
	/**
	 * Returns the date of the flight.
	 * @return the date/time
	 */
	public Instant getDate();
}
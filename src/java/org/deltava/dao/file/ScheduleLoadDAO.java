// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.util.Collection;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

/**
 * An interface to describe common methods for Flight Schedule import Data Access Objects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface ScheduleLoadDAO {

	/**
	 * Initalizes the list of airports.
	 * @param airports a Collection of Airport beans
	 * @see ScheduleLoadDAO#setAirlines(Collection)
	 */
	public void setAirports(Collection<Airport> airports);
	
	/**
	 * Initializes the list of airlines.
	 * @param airlines a Collection of Airline beans
	 * @see ScheduleLoadDAO#setAirports(Collection)
	 */
	public void setAirlines(Collection<Airline> airlines);
	
	/**
	 * Returns back the loaded Flight Schedule entries.
	 * @return a Collection of ScheduleEntry beans
	 */
	public Collection<ScheduleEntry> process() throws DAOException;
	
	/**
	 * Returns any error messages from the Schedule load.
	 * @return a Collection of error messages
	 */
	public Collection<String> getErrorMessages();
}
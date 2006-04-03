// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.util.*;
import java.io.InputStream;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;
import org.deltava.util.CollectionUtils;

/**
 * An abstract class to store common methods for Flight Schedule import Data Access Objects.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class ScheduleLoadDAO extends DAO {
	
	protected Map<String, Airline> _airlines;
	protected Map<String, Airport> _airports;
	protected Collection<PartnerAirline> _partners = new ArrayList<PartnerAirline>();
	protected Collection<String> _errors = new ArrayList<String>();

	/**
	 * Initializes the Data Access Object.
	 * @param is the input stream to read
	 */
	protected ScheduleLoadDAO(InputStream is) {
		super(is);
	}
	
	/**
	 * Initializes the list of airlines.
	 * @param airlines a Collection of Airline beans
	 * @see ScheduleLoadDAO#setAirports(Collection)
	 */
	public void setAirlines(Collection<Airline> airlines) {
		_airlines = CollectionUtils.createMap(airlines, "code");
	}
	
	/**
	 * Initalizes the list of airports.
	 * @param airports a Collection of Airport beans
	 * @see ScheduleLoadDAO#setAirlines(Collection)
	 */
	public void setAirports(Collection<Airport> airports) {
		_airports = CollectionUtils.createMap(airports, "IATA");
	}
	
	/**
	 * Initializes the list of partner airlines.
	 * @param airlines a Collection of PartnerAirline beans
	 */
	public void setPartners(Collection<PartnerAirline> airlines) {
		if (airlines != null)
			_partners.addAll(airlines);
	}
	
	/**
	 * Returns back the loaded Flight Schedule entries.
	 * @return a Collection of ScheduleEntry beans
	 */
	public abstract Collection<ScheduleEntry> process() throws DAOException;
	
	/**
	 * Returns any error messages from the Schedule load.
	 * @return a Collection of error messages
	 */
	public Collection<String> getErrorMessages() {
		return _errors;
	}
}
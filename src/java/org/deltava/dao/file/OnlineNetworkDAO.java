// Copyright 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.time.Instant;
import java.io.InputStream;

import org.deltava.beans.schedule.Airport;
import org.deltava.beans.servinfo.NetworkInfo;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * An Online Network information Data Access Object.
 * @author Luke
 * @version 10.1
 * @since 9.0
 */

public abstract class OnlineNetworkDAO extends DAO {
	
	private static final String DATE_FMT = "yyyy-MM-dd'T'HH:mm:ss['Z']";
	
	/**
	 * Creates the Data Access Object.
	 * @param is the InputStream to read
	 */
	protected OnlineNetworkDAO(InputStream is) {
		super(is);
	}

	/**
	 * Retrieves Online Network information.
	 * @return a NetworkInfo bean
	 * @throws DAOException if an I/O error occurs
	 */
	public abstract NetworkInfo getInfo() throws DAOException;
	
	/**
	 * Helper method to parse airport codes even if the Airport does not exist in the database.
	 * @param airportCode the Airport code
	 * @return an Airport
	 */
	protected static Airport getAirport(String airportCode) {
		Airport a = SystemData.getAirport(airportCode);
		return (a == null) ? new Airport(airportCode, airportCode, airportCode) : a;
	}
	
	/**
	 * Parses a Javascript date/time.
	 * @param dt the date/time text
	 * @return an Instant
	 */
	protected static Instant parseDateTime(String dt) {
		int pos = dt.indexOf('.');
		String dt2 = (pos > -1) ? dt.substring(0, pos) : dt;
		return StringUtils.parseInstant(dt2, DATE_FMT);
	}
}
// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.GeospaceLocation;

/**
 * An interface to mark airport beans with an ICAO code. 
 * @author Luke
 * @version 2.7
 * @since 2.7
 */

public interface ICAOAirport extends GeospaceLocation {
	
	/**
	 * Returns the ICAO code for the airport.
	 * @return the ICAO code
	 */
	public String getICAO();
}
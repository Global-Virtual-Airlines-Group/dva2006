// Copyright 2009, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import org.deltava.beans.GeospaceLocation;

/**
 * An interface to mark airport beans with an ICAO code. 
 * @author Luke
 * @version 4.2
 * @since 2.7
 */

public interface ICAOAirport extends GeospaceLocation {
	
	/**
	 * Returns the ICAO code for the airport.
	 * @return the ICAO code
	 */
	public String getICAO();
	
	/**
	 * Returns the magnetic variation at this airport.
	 * @return the variation in degrees
	 */
	public double getMagVar();
}
// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import org.deltava.beans.flight.ILSCategory;

/**
 * A utility class for weather data. 
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class WeatherUtils {

	// singleton
	private WeatherUtils() {
		super();
	}
	
	/**
	 * Returns the ILS Category in use for a particular METAR.
	 * @param m the METAR for the Airport
	 * @return an ILSCategory, or null if ceiling above 2500 feet AGL
	 */
	public static ILSCategory getILS(METAR m) {
		
		// If there's no cloud layer, return nothing
		if (m.getClouds().isEmpty())
			return ILSCategory.NONE;
		
		// Get the bottom cloud layer
		CloudLayer cl = m.getClouds().first();
		
		// Get the visibility
		double viz = m.getVisibility();
		if (m.getVisibilityLessThan())
			viz *= 0.75;
		
		return ILSCategory.categorize(cl.getHeight(), viz);
	}
}
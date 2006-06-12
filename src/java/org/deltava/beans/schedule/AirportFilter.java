// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * An interface for Airport bean filters.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public interface AirportFilter {

	/**
	 * Determines wether the Airport matches the criteria set by this Filter.
	 * @param a the Airport bean to examine
	 * @return TRUE if the airport matches the criteria, otherwise FALSE
	 */
	public boolean accept(Airport a);
}
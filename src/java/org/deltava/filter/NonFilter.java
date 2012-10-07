// Copyright 2005, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import org.deltava.beans.schedule.Airport;

/**
 * An Airport Filter that accepts all airports. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class NonFilter extends AirportFilter {

	/**
	 * Accepts all airports.
	 */
	public boolean accept(Airport a) {
		return (a != null);
	}
}
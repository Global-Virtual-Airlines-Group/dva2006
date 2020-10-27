// Copyright 2005, 2012, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import org.deltava.beans.schedule.Airport;

/**
 * An Airport Filter that accepts all airports. 
 * @author Luke
 * @version 9.1
 * @since 5.0
 */

public class NonFilter implements AirportFilter {

	@Override
	public boolean accept(Airport a) {
		return (a != null);
	}
}
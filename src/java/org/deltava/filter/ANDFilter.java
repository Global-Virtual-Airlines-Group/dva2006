// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import java.util.*;

import org.deltava.beans.schedule.Airport;

/**
 * An Airport filter to support multiple chained filters. 
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class ANDFilter extends MultiFilter {
	
	@Override
	public boolean accept(Airport a) {
		if (a == null) return false;
		boolean hasAll = true;
		for (Iterator<AirportFilter> i = _filters.iterator(); hasAll && i.hasNext(); ) {
			AirportFilter af = i.next();
			hasAll &= af.accept(a);
		}
		
		return hasAll;
	}
}
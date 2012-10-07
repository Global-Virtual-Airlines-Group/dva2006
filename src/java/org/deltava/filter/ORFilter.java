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

public class ORFilter extends MultiFilter {
	
	@Override
	public boolean accept(Airport a) {
		if (a == null) return false;
		for (Iterator<AirportFilter> i = _filters.iterator(); i.hasNext(); ) {
			AirportFilter af = i.next();
			if (af.accept(a))
				return true;
		}
		
		return false;
	}
}
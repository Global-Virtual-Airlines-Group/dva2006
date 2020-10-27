// Copyright 2012, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.filter;

import java.util.*;

/**
 * An abstract AirportFilter to support filter chains.
 * @author Luke
 * @version 9.1
 * @since 5.0
 */

public abstract class MultiFilter implements AirportFilter {
	
	protected final Collection<AirportFilter> _filters = new LinkedHashSet<AirportFilter>();

	/**
	 * Adds a filter to the chain.
	 * @param af an AirportFilter
	 */
	public void add(AirportFilter af) {
		_filters.add(af);
	}
}
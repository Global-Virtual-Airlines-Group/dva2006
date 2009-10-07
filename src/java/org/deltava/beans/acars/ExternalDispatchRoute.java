// Copyright 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.schedule.*;

/**
 * A class to store a Dispatch Route that has an external source. 
 * @author Luke
 * @version 2.6
 * @since 2.6
 */

public class ExternalDispatchRoute extends DispatchRoute implements ExternalFlightRoute {

	private String _source;
	
	/**
	 * Creates a new, empty external Dispatch route.
	 */
	public ExternalDispatchRoute() {
		super();
	}
	
	/**
	 * Creates a new external Dispatch route from a Populated route.
	 * @param pr the PopulatedRoute bean
	 */
	public ExternalDispatchRoute(PopulatedRoute pr) {
		super(pr);
		
		// Copy source
		if (pr instanceof ExternalFlightRoute)
			_source = ((ExternalFlightRoute) pr).getSource();
	}
	
	public String getSource() {
		return _source;
	}
	
	public void setSource(String src) {
		_source = src;
	}
}
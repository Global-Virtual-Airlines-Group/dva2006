// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

/**
 * A class to store ETOPS validation results.
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class ETOPSResult {
	
	private final ETOPS _e;
	private final int _maxDistance;
	private final Collection<String> _msgs = new ArrayList<String>();
	
	/**
	 * Creates an empty results object.
	 * @param e the ETOPS classification
	 */
	ETOPSResult(ETOPS e) {
		super();
		_e = e;
		_maxDistance = 0;
	}

	/**
	 * Creates the results object.
	 * @param e the ETOPS classification
	 * @param distance the maximum distance from any airport in miles
	 * @param msgs the messages
	 */
	ETOPSResult(ETOPS e, int distance, Collection<String> msgs) {
		super();
		_e = e;
		_maxDistance = Math.max(0, distance);
		_msgs.addAll(msgs);
	}

	/**
	 * Returns the ETOPS classification.
	 * @return the classification
	 */
	public ETOPS getResult() {
		return _e;
	}
	
	/**
	 * Returns the maximum distance to a diversion airport.
	 * @return the maximum distance in miles
	 */
	public int getDistance() {
		return _maxDistance;
	}
	
	/**
	 * Returns the ETOPS messages.
	 * @return a Collection of messages
	 */
	public Collection<String> getMessages() {
		return _msgs;	
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(_e.toString());
		buf.append(" - max distance ");
		buf.append(_maxDistance);
		buf.append(" miles");
		for (Iterator<String> i = _msgs.iterator(); i.hasNext(); ) {
			buf.append('\n');
			buf.append(i.next());
		}
			
		return buf.toString();
	}
}
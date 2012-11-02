// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.NavigationDataBean;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.GeoComparator;

/**
 * A class to store ETOPS validation results.
 * @author Luke
 * @version 5.0
 * @since 4.1
 */

public class ETOPSResult {
	
	private final ETOPS _e;
	private final SortedSet<NavigationDataBean> _airports;
	private final GeoLocation _warnPoint;
	private final Collection<String> _msgs = new ArrayList<String>();
	
	/**
	 * Creates an empty results object.
	 * @param e the ETOPS classification
	 */
	ETOPSResult(ETOPS e) {
		super();
		_e = e;
		_warnPoint = new GeoPosition();
		_airports = new TreeSet<NavigationDataBean>(new GeoComparator(_warnPoint));
	}

	/**
	 * Creates the results object.
	 * @param e the ETOPS classification
	 * @param loc the farthest point away from any Airport 
	 * @param msgs the messages
	 */
	ETOPSResult(ETOPS e, NavigationDataBean loc, Collection<String> msgs) {
		super();
		_e = e;
		_msgs.addAll(msgs);
		_warnPoint = (loc == null) ? new GeoPosition() : loc;
		_airports = new TreeSet<NavigationDataBean>(new GeoComparator(_warnPoint));
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
		return (_airports.isEmpty()) ? 0 : _airports.first().getPosition().distanceTo(_warnPoint);
	}
	
	/**
	 * Returns the Airport closest to the maximum distance point.
	 * @return the closest Airport
	 */
	public List<NavigationDataBean> getClosestAirports() {
		return new ArrayList<NavigationDataBean>(_airports);
	}

	/**
	 * Returns the farthest point from a diversion airport.
	 * @return the GeoLocation
	 */
	public GeoLocation getWarningPoint() {
		return _warnPoint;
	}
	
	/**
	 * Returns the ETOPS messages.
	 * @return a Collection of messages
	 */
	public Collection<String> getMessages() {
		return _msgs;	
	}
	
	/**
	 * Adds a diversion airport to the helper.
	 * @param a a NavigationDataBean
	 */
	public void add(NavigationDataBean a) {
		_airports.add(a);
	}
	
	public int hashCode() {
		return toString().hashCode();
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder(_e.toString());
		buf.append(" - max distance ");
		buf.append(getDistance());
		buf.append(" miles");
		for (String msg : _msgs) {
			buf.append('\n');
			buf.append(msg);
		}
			
		return buf.toString();
	}
}
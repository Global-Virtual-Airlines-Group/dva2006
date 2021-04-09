// Copyright 2012, 2013, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.flight;

import java.util.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;

import org.deltava.comparators.GeoComparator;

/**
 * A class to store ETOPS validation results.
 * @author Luke
 * @version 7.0
 * @since 4.1
 */

public class ETOPSResult {
	
	private final ETOPS _e;
	private final SortedSet<AirportLocation> _airports;
	private final NavigationDataBean _warnPoint;
	private final Collection<String> _msgs = new ArrayList<String>();
	
	private class GeoCodeComparator implements Comparator<AirportLocation> {
		private final GeoComparator _cp;
		
		GeoCodeComparator(GeoLocation cp) {
			super();
			_cp = new GeoComparator(cp);
		}

		@Override
		public int compare(AirportLocation nd1, AirportLocation nd2) {
			int tmpResult = _cp.compare(nd1, nd2);
			return (tmpResult == 0) ? nd1.compareTo(nd2) : tmpResult;
		}
	}
	
	/**
	 * Creates an empty results object.
	 * @param e the ETOPS classification
	 */
	ETOPSResult(ETOPS e) {
		super();
		_e = e;
		_warnPoint = null;
		_airports = new TreeSet<AirportLocation>(new GeoCodeComparator(_warnPoint));
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
		_warnPoint = loc;
		_airports = new TreeSet<AirportLocation>(new GeoCodeComparator(_warnPoint));
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
	public List<AirportLocation> getClosestAirports() {
		return new ArrayList<AirportLocation>(_airports);
	}

	/**
	 * Returns the farthest point from a diversion airport.
	 * @return a NavigationDataBean representing the warning point
	 */
	public NavigationDataBean getWarningPoint() {
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
	 * @param a an AirportLocation
	 */
	public void add(AirportLocation a) {
		_airports.add(a);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
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
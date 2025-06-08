// Copyright 2012, 2014, 2015, 2019, 2020, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.*;

import org.deltava.beans.*;

/**
 * A bean to store route frequency data.
 * @author Luke
 * @version 12.0
 * @since 4.1
 */

public abstract class AbstractRoute implements RoutePair, MapEntry {

	private final Airport _aD;
	private final Airport _aA;

	private final String _code;
	protected int _frequency;

	/**
	 * Creates the route.
	 * @param aD the departure Airport
	 * @param aA the arrival Airport
	 * @throws NullPointerException if aD or aA are null
	 */
	protected AbstractRoute(Airport aD, Airport aA) {
		super();
		_aD = aD;
		_aA = aA;

		// Build code
		Collection<String> airports = new TreeSet<String>();
		airports.add(aD.getICAO());
		airports.add(aA.getICAO());
		_code = airports.toString();
	}

	@Override
	public Airport getAirportD() {
		return _aD;
	}

	@Override
	public Airport getAirportA() {
		return _aA;
	}

	@Override
	public double getLatitude() {
		return midPoint().getLatitude();
	}

	@Override
	public double getLongitude() {
		return midPoint().getLongitude();
	}

	/**
	 * Returns the number of flights in this route pair.
	 * @return the number of flights
	 */
	public int getFlights() {
		return _frequency;
	}

	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\"><span class=\"bld\">");
		buf.append(_aD.getName());
		buf.append("</span> (");
		buf.append(_aD.getIATA());
		buf.append(" / ");
		buf.append(_aD.getICAO());
		buf.append(")<br /><span class=\"bld\">");
		buf.append(_aA.getName());
		buf.append("</span> (");
		buf.append(_aA.getIATA());
		buf.append(" / ");
		buf.append(_aA.getICAO());
		buf.append(")<br /><br />Distance: ");
		buf.append(getDistance());
		buf.append(" miles<br />");
		buf.append(_frequency);
		buf.append(" Flights</div>");
		return buf.toString();
	}

	@Override
	public int hashCode() {
		return _code.hashCode();
	}

	@Override
	public String toString() {
		return createKey();
	}
}
// Copyright 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.wx;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

/**
 * A class to store cyclones and anti-cyclones. 
 * @author Luke
 * @version 5.2
 * @since 5.2
 */

public class Cyclone implements GeoLocation {
	
	/**
	 * The cyclone type.
	 */
	public enum Type {
		LOW, HIGH;
	}
	
	private final Type _type;
	private final GeoLocation _loc;
	private int _mb;
	
	/**
	 * Creates the bean.
	 * @param t the cyclone type
	 * @param loc the GeoLocation
	 */
	public Cyclone(Type t, GeoLocation loc) {
		super();
		_type = t;
		_loc = new GeoPosition(loc);
	}

	/**
	 * Returns the type of pressure center.
	 * @return the Type
	 */
	public Type getType() {
		return _type;
	}
	
	/**
	 * Returns the central pressure.
	 * @return the pressure in millibars
	 */
	public int getPressure() {
		return _mb;
	}
	
	@Override
	public double getLatitude() {
		return _loc.getLatitude();
	}

	@Override
	public double getLongitude() {
		return _loc.getLongitude();
	}
	
	/**
	 * Sets the central pessure of the cyclone.
	 * @param mb the pressure in millibars
	 */
	public void setPressure(int mb) {
		_mb = Math.max(850, Math.min(1100, mb));
	}
}
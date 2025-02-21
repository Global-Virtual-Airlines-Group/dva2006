// Copyright 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.DatabaseBean;
import org.deltava.beans.flight.RunwayLengthUsage;

/**
 * A bean to store data about an individual landing.
 * @author Luke
 * @version 11.5
 * @since 11.5
 */

public class TouchdownData extends DatabaseBean implements RunwayLengthUsage {
	
	private int _length;
	private int _distance;
	private int _vSpeed;

	/**
	 * Creates the bean.
	 * @param id the Flight Report database ID 
	 */
	public TouchdownData(int id) {
		super();
		setID(id);
	}
	
	/**
	 * Returns the vertical speed at touchdown.
	 * @return the vertical speed in feet per minute
	 */
	public int getVSpeed() {
		return _vSpeed;
	}

	@Override
	public int getLength() {
		return _length;
	}

	@Override
	public int getDistance() {
		return _distance;
	}

	/**
	 * Updates the runway length.
	 * @param l the length in feet
	 */
	public void setLength(int l) {
		_length = l;
	}
	
	/**
	 * Updates the touchdown distance from the threshold.
	 * @param d the distance in feet
	 */
	public void setDistance(int d) {
		_distance = d;
	}
	
	/**
	 * Updates the vertical speed at landing.
	 * @param vs the vertical speed in feet per minute
	 */
	public void setVSpeed(int vs) {
		_vSpeed = vs;
	}
}
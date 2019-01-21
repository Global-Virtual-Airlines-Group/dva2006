// Copyright 2009, 2016, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.navdata.Runway;

/**
 * A bean to store ACARS takeoff/landing runway data.
 * @author Luke
 * @version 8.5
 * @since 2.6
 */

public class RunwayDistance extends Runway {
	
	private final int _distance;

	/**
	 * Initializes the bean.
	 * @param r the Runway
	 * @param distance the distance from the threshold in feet
	 */
	public RunwayDistance(Runway r, int distance) {
		super(r.getLatitude(), r.getLongitude());
		setCode(r.getCode());
		setName(r.getName());
		setLength(r.getLength());
		setHeading(r.getHeading());
		setFrequency(r.getFrequency());
		setMagVar(r.getMagVar());
		setSurface(r.getSurface());
		setNewCode(r.getNewCode());
		setSimulator(r.getSimulator());
		_distance = distance;
	}

	/**
	 * Returns the distance from the runway threshold that the takeoff/landing took place.
	 * @return the distance in feet
	 */
	public int getDistance() {
		return _distance;
	}
}
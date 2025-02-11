// Copyright 2009, 2016, 2019, 2021, 2022, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.flight.RunwayLengthUsage;
import org.deltava.beans.navdata.Runway;

/**
 * A bean to store ACARS takeoff/landing runway data.
 * @author Luke
 * @version 11.1
 * @since 2.6
 */

public class RunwayDistance extends Runway implements RunwayLengthUsage {
	
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
		setWidth(r.getWidth());
		setHeading(r.getHeading());
		setFrequency(r.getFrequency());
		setMagVar(r.getMagVar());
		setSurface(r.getSurface());
		setAlternateCode(r.getAlternateCode(), r.isAltNew());
		setSimulator(r.getSimulator());
		setThresholdLength(r.getThresholdLength());
		_distance = distance;
	}

	@Override
	public int getDistance() {
		return _distance;
	}
	
	/**
	 * Compares two RunwayDistance beans by comparing Runway ID and distance.
	 * @param rd2 the RunwayDistance bean
	 * @return TRUE if the runway and distance match, otherwise FALSE
	 */
	public boolean equals(RunwayDistance rd2) {
		return super.equals(rd2) && (_distance == rd2._distance);
	}
}
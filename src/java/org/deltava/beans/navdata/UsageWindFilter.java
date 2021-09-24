// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.deltava.beans.wx.METAR;
import org.deltava.util.GeoUtils;

/**
 * A UsageFilter to filter runways based on maximum tailwind component and popularity.
 * @author Luke
 * @version 10.2
 * @since 10.2
 */

public class UsageWindFilter extends UsagePercentFilter {

	private final int _maxTailwind;
	private int _windSpeed;
	private int _windHeading;

	/**
	 * Creates the filter.
	 * @param minPct
	 * @param maxTailwind
	 */
	public UsageWindFilter(int minPct, int maxTailwind) {
		super(minPct);
		_maxTailwind = maxTailwind;
	}

	/**
	 * Updates the Airport wind data. 
	 * @param hdg the wind heading in degrees
	 * @param spd the wind speed in knots
	 */
	public void setWinds(int hdg, int spd) {
		_windHeading = hdg;
		_windSpeed = spd;
	}
	
	/**
	 * Updates the Airport wind data from a METAR.
	 * @param m the METAR bean 
	 */
	public void setWinds(METAR m) {
		if (m != null)
			setWinds(m.getWindDirection(), (m.getWindSpeed() + m.getWindGust()) / 2);
		else
			setWinds(0, 0);
	}
	
	private double calculateHeadWind(Runway r) {
		double wd = GeoUtils.delta(_windHeading, r.getHeading());
		return StrictMath.cos(StrictMath.toRadians(wd)) * _windSpeed;
	}

	@Override
	public List<RunwayUsage> filter(Collection<RunwayUsage> data) {
		return super.filter(data.stream().filter(r -> (calculateHeadWind(r) >= _maxTailwind)).collect(Collectors.toList()));
	}
}
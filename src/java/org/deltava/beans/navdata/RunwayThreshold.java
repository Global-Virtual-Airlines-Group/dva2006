// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

import org.deltava.beans.*;

import org.deltava.util.StringUtils;

/**
 * A bean to store ACARS takeoff/landing runway data.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class RunwayThreshold implements MarkerMapEntry, IconMapEntry {
	
	private final GeoLocation _loc;
	private final String _name;
	private final int _size;
	
	/**
	 * Initializes the bean.
	 * @param loc the GeoLocation of the threshold
	 * @param name the Runway name
	 * @param size the threshold displacement in feet
	 */
	RunwayThreshold(GeoLocation loc, String name, int size) {
		_loc = loc;
		_name = name;
		_size = size;
	}
	
	@Override
	public double getLatitude() {
		return _loc.getLatitude();
	}
	
	@Override
	public double getLongitude() {
		return _loc.getLongitude();
	}
	
	@Override
	public String getIconColor() {
		return GREY;
	}
	
	@Override
	public int getPaletteCode() {
		return 5;
	}
	
	@Override
	public int getIconCode() {
		return 6;
	}
	
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox navdata\"><span class=\"bld\">Runway ");
		buf.append(_name);
		buf.append("</span><br /><br /><span class=\"ita\">Distplaced Threshold: ");
		buf.append(_size);
		buf.append(" feet<br /><br />Latitude: ");
		buf.append(StringUtils.format(_loc, true, GeoLocation.LATITUDE));
		buf.append("<br />Longitude: ");
		buf.append(StringUtils.format(_loc, true, GeoLocation.LONGITUDE));
		buf.append("<br /></div>");
		return buf.toString();
	}
}
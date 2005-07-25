// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.Pilot;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;

import org.deltava.util.StringUtils;

/**
 * A bean to store pilot locations.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotLocation implements GeoLocation, MapEntry {
	
	private Pilot _usr;
	private GeoLocation _position;

	// TODO JavaDoc
	public PilotLocation(Pilot usr, GeoLocation loc) {
		super();
		_usr = usr;
		_position = loc;
	}
	
	public double getLatitude() {
		return _position.getLatitude();
	}
	
	public double getLongitude() {
		return _position.getLongitude();
	}
	
	public Pilot getUser() {
		return _usr;
	}
	
	public String getIconColor() {
		return MapEntry.BLUE;
	}
	
	public String getInfoBox() {
		StringBuffer buf = new StringBuffer("<span class=\"small\"><b>");
		buf.append(_usr.getName());
		buf.append("</b> (");
		buf.append(_usr.getPilotCode());
		buf.append(")<br />");
		buf.append(_usr.getRank());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br />Location: ");
		buf.append(_usr.getLocation());
		buf.append("<br /><br />Position: ");
		buf.append(StringUtils.format(_position, true, GeoLocation.ALL));
		buf.append("<br />Joined on: ");
		buf.append(StringUtils.format(_usr.getCreatedOn(), "EEEE MMMM dd, yyyy"));
		buf.append("</span>");
		return buf.toString();
	}
}
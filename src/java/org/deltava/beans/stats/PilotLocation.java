// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2014, 2015, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.*;
import org.deltava.beans.system.IPBlock;

import org.deltava.util.StringUtils;

/**
 * A bean to store pilot locations for displaying on a Google Map.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class PilotLocation implements MarkerMapEntry {
	
	private final Pilot _usr;
	private final GeoLocation _position;
	private IPBlock _addrInfo;
	private boolean _allowDelete;

	/**
	 * Creates a new Pilot location object.
	 * @param usr the Pilot bean
	 * @param loc the Pilot's location
	 */
	public PilotLocation(Pilot usr, GeoLocation loc) {
		super();
		_usr = usr;
		_position = loc;
		if (loc instanceof IPBlock ipb) _addrInfo = ipb;
	}
	
	/**
	 * Returns the Pilot's latitude.
	 * @return the latitude in degrees
	 * @see PilotLocation#getLongitude()
	 */
	@Override
	public final double getLatitude() {
		return _position.getLatitude();
	}
	
	/**
	 * Returns the Pilot's longitude.
	 * @return the longitude in degrees
	 * @see PilotLocation#getLatitude()
	 */
	@Override
	public final double getLongitude() {
		return _position.getLongitude();
	}
	
	/**
	 * Returns the Pilot.
	 * @return the Pilot bean
	 */
	public Pilot getUser() {
		return _usr;
	}
	
	/**
	 * Sets whether the infobox should have a link to allow map entry deletion.
	 * @param allowDelete TRUE if the deletion link should be added, otherwise FALSE
	 */
	public void setAllowDelete(boolean allowDelete) {
		_allowDelete = allowDelete;
	}
	
	/**
	 * Display the Google Map icon color.
	 * @return BLUE
	 */
	@Override
	public String getIconColor() {
		return MapEntry.BLUE;
	}
	
	/**
	 * Returns the Google Map info box text.
	 * @return the info box text
	 */
	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox pilotBoard\"><span class=\"bld\">");
		buf.append(_usr.getName());
		buf.append("</span> (");
		buf.append(_usr.getPilotCode());
		buf.append(")<br />");
		buf.append(_usr.getRank());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br />");
		
		// Add motto if not empty
		if (!StringUtils.isEmpty(_usr.getMotto())) {
			buf.append("<span class=\"ita\">\"");
			buf.append(_usr.getMotto());
			buf.append("\"</span><br />");
		}

		// Add Location		
		buf.append("<br />Location: ");
		if (_addrInfo != null) {
			buf.append(_addrInfo.getLocation());
			buf.append(" (");
			buf.append(_addrInfo.toString());
			buf.append(')');
		} else
			buf.append(_usr.getLocation());
		
		// Add position
		if (_position != null) {
			buf.append("<br />Position: ");
			buf.append(StringUtils.format(_position, true, GeoLocation.ALL));
		}
		
		buf.append("<br />Joined on: ");
		buf.append(StringUtils.format(_usr.getCreatedOn(), "EEEE MMMM dd, yyyy"));
		if (_usr.getLastLogin() != null) {
			buf.append("<br />Last login: ");
			buf.append(StringUtils.format(_usr.getLastLogin(), "EEEE MMMM dd, yyyy"));
		}
		
		// Add deletion link
		if (_allowDelete) {
			buf.append("<br /><br /><a href=\"javascript:void golgotha.pilotMap.deleteMarker(");
			buf.append(_usr.getID());
			buf.append(")\" class=\"small sec bld\">DELETE MARKER</a>");
		}
		
		buf.append("</div>");
		return buf.toString();
	}
}
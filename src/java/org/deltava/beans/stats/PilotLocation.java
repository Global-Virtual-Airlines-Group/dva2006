// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.stats;

import org.deltava.beans.Pilot;
import org.deltava.beans.GeoLocation;
import org.deltava.beans.MapEntry;

import org.deltava.util.StringUtils;

/**
 * A bean to store pilot locations for displaying on a Google Map.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PilotLocation implements MapEntry {
	
	private Pilot _usr;
	private GeoLocation _position;
	private int _minZoom;
	
	public String _info;

	/**
	 * Creates a new Pilot location object.
	 * @param usr the Pilot bean
	 * @param loc the Pilot's location
	 */
	public PilotLocation(Pilot usr, GeoLocation loc) {
		super();
		_usr = usr;
		_position = loc;
	}
	
	/**
	 * Returns the Pilot's latitude.
	 * @return the latitude in degrees
	 * @see PilotLocation#getLongitude()
	 */
	public final double getLatitude() {
		return _position.getLatitude();
	}
	
	/**
	 * Returns the Pilot's longitude.
	 * @return the longitude in degrees
	 * @see PilotLocation#getLatitude()
	 */
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
	 * Reutrns the minimum map zoom level to display the Pilot at.
	 * @return the minimum zoom level
	 */
	public int getMinZoom() {
		return _minZoom;
	}
	
	/**
	 * Updates the minimum map zoom level to display the Pilot at.
	 * @param zoom the minimum zoom level
	 */
	public void setMinZoom(int zoom) {
		_minZoom = (zoom < 1) ? 1 : zoom;
	}
	
	/**
	 * Display the Google Map icon color.
	 * @return BLUE
	 */
	public String getIconColor() {
		return MapEntry.BLUE;
	}
	
	/**
	 * Returns the Google Map info box text.
	 * @return the info box text
	 */
	public String getInfoBox() {
		if (_info != null)
			return _info;
		
		StringBuilder buf = new StringBuilder("<span class=\"mapInfoBox\"><b>");
		buf.append(_usr.getName());
		buf.append("</b> (");
		buf.append(_usr.getPilotCode());
		buf.append(")<br />");
		buf.append(_usr.getRank());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br />");
		
		// Add motto if not empty
		if (!StringUtils.isEmpty(_usr.getMotto())) {
			buf.append('\"');
			buf.append(_usr.getMotto());
			buf.append("\"<br />");
		}

		// Add Location		
		buf.append("<br />Location: ");
		buf.append(_usr.getLocation());
		buf.append("<br />Position: ");
		buf.append(StringUtils.format(_position, true, GeoLocation.ALL));
		buf.append("<br />Joined on: ");
		buf.append(StringUtils.format(_usr.getCreatedOn(), "EEEE MMMM dd, yyyy"));
		buf.append("</span>");
		_info = buf.toString();
		return _info;
	}
}
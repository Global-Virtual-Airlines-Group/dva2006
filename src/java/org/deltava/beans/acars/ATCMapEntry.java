// Copyright 2011, 2014 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.*;

/**
 * A Map entry bean to store an ATC location.
 * @author Luke
 * @version 6.0
 * @since 4.0
 */

public class ATCMapEntry extends GroundMapEntry {

	/**
	 * Creates the bean.
	 * @param usr the Pilot
	 * @param loc the location
	 */
	public ATCMapEntry(Pilot usr, GeoLocation loc) {
		super(usr, loc);
	}
	
	@Override
	public EntryType getType() {
		return EntryType.ATC;
	}
	
	@Override
	public String getIconColor() {
		return BROWN;
	}

	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox acarsATC\"><span class=\"pri bld\">");
		buf.append(_usr.getName());
		buf.append("</span><br />");
		buf .append(_usr.getRank());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br /><br />");
		buf.append("ACARS Radar Build ");
		buf.append(_clientBuild);
		if (_betaBuild > 0) {
			buf.append(" (Beta ");
			buf.append(_betaBuild);
			buf.append(')');
		}
		
		buf.append("</div>");
		return buf.toString();
	}
}
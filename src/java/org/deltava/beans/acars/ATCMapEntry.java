// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.*;

/**
 * A Map entry bean to store an ATC location.
 * @author Luke
 * @version 4.0
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
	
	public final boolean isDispatch() {
		return false;
	}

	@Override
	public String getIconColor() {
		return BROWN;
	}

	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox\"><span class=\"pri bld\">");
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
// Copyright 2008, 2011, 2014, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.*;
import org.deltava.beans.schedule.Airport;

import org.deltava.util.StringUtils;

/**
 * An ACARS Map entry bean to store a Dispatcher location and range.
 * @author Luke
 * @version 7.0
 * @since 2.2
 */

public class DispatchMapEntry extends GroundMapEntry {
	
	/**
	 * Initializes the map entry.
	 * @param usr the Dispatcher user
	 * @param loc the service location
	 */
	public DispatchMapEntry(Pilot usr, GeoLocation loc) {
		super(usr, loc);
	}
	
	@Override
	public EntryType getType() {
		return EntryType.DISPATCH;
	}

	@Override
	public String getIconColor() {
		return _busy ? PURPLE : GREEN;
	}

	@Override
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox acarsDispatch\"><span class=\"pri bld\">");
		buf.append(_usr.getName());
		buf.append("</span><br />");
		buf .append(_usr.getRank());
		buf.append(", ");
		buf.append(_usr.getEquipmentType());
		buf.append("<br /><br />");
		buf.append("ACARS Dispatch Build ");
		buf.append(_clientBuild);
		if (_betaBuild > 0) {
			buf.append(" (Beta ");
			buf.append(_betaBuild);
			buf.append(')');
		}
		if (_busy) 
			buf.append("<br /><span class=\"error bld\">BUSY - Not providing Dispatch Service</span>");
		
		// Display limited range
		int range = getRange();
		if ((range > 0) && (range != Integer.MAX_VALUE)) {
			buf.append("<br /><br />");
			buf.append("<span class=\"sec bld\">Dispatch services restricted to Pilots within<br />");
			buf.append(range);
			buf.append(" miles of ");
			GeoLocation loc = getLocation();
			if (loc instanceof Airport)
				buf.append(loc);
			else
				buf.append(StringUtils.format(getLocation(), true, GeoLocation.ALL));
			
			buf.append("</span>");
		}
			
		buf.append("</div>");
		return buf.toString();
	}
}
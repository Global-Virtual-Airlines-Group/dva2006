// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.acars;

import org.deltava.beans.*;
import org.deltava.util.StringUtils;

/**
 * A bean to store a Dispatcher location and range.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class DispatchMapEntry extends ACARSMapEntry {
	
	private int _range;

	/**
	 * Initializes the map entry.
	 * @param usr the Dispatcher user
	 * @param loc the service location
	 */
	public DispatchMapEntry(Pilot usr, GeoLocation loc) {
		super(loc);
		_usr = usr;
	}

	public final boolean isDispatch() {
		return true;
	}

	public String getIconColor() {
		return _busy ? PURPLE : GREEN;
	}

	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<div class=\"mapInfoBox\"><span class=\"pri bld\">");
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
		if ((_range > 0) && (_range != Integer.MAX_VALUE)) {
			buf.append("<br /><br />");
			buf.append("<span class=\"sec bld\">Dispatch services restricted to Pilots within<br />");
			buf.append(_range);
			buf.append(" miles of ");
			buf.append(StringUtils.format(getLocation(), true, GeoLocation.ALL));
			buf.append("</span>");
		}
			
		buf.append("</div>");
		return buf.toString();
	}
	
	/**
	 * Returns the dispatch service range.
	 * @return the range in miles
	 */
	public int getRange() {
		return _range;
	}

	/**
	 * Updates the dispatch service range.
	 * @param range the range radius in miles
	 */
	public void setRange(int range) {
		_range = range;
	}
}
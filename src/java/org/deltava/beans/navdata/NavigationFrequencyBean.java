// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.navdata;

/**
 * A NavigationDataBean to represent a navigation aid with a tunable radio frequency.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public abstract class NavigationFrequencyBean extends NavigationDataBean {

	private String _freq;

	/**
	 * Initializes the bean.
	 * @param type the type
	 * @param lat the latitude
	 * @param lon the longitude
	 */
	public NavigationFrequencyBean(int type, double lat, double lon) {
		super(type, lat, lon);
	}

	/**
	 * Returns the navaid's frequency.
	 * @return the frequency
	 */
	public String getFrequency() {
		return _freq;
	}

	/**
	 * Updates the navaid's frequency.
	 * @param freq the frequency
	 */
	public void setFrequency(String freq) {
		_freq = freq;
	}

	/**
	 * Returns the default Google Maps infobox text.
	 * @return an HTML String
	 */
	public String getInfoBox() {
		StringBuilder buf = new StringBuilder("<span class=\"mapInfoBox\">");
		buf.append(getHTMLTitle());
		if (_freq != null) {
			buf.append("Frequency: ");
			buf.append(_freq);
			buf.append("<br />");
		}
		
		buf.append(getHTMLPosition());
		buf.append("</span>");
		return buf.toString();
	}
}
// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

import java.util.Date;

/**
 * A bean to store data about an externally hosted Chart. These typically have a
 * reflector URL that redirects the user to the "real" URL for the chart.
 * @author Luke
 * @version 4.0
 * @since 4.0
 */

public class ExternalChart extends Chart {

	private Date _lastMod;
	private String _url;
	
	/**
	 * Creates the Chart bean.
	 * @param name the chart name
	 * @param a the Airport
	 */
	public ExternalChart(String name, Airport a) {
		super(name, a);
	}

	/**
	 * Returns the URL to the chart.
	 * @return the URL
	 */
	public String getURL() {
		return _url;
	}
	
	/**
	 * Returns the last modification date of the chart.
	 * @return the modification date/time
	 */
	public Date getLastModified() {
		return _lastMod;
	}
	
	/**
	 * Updates the last modification date of the chart.
	 * @param dt the modification date/time
	 */
	public void setLastModified(Date dt) {
		_lastMod = dt;
	}
	
	/**
	 * Updates the URL to the chart.
	 * @param url the URL
	 */
	public void setURL(String url) {
		_url = url;
	}
}
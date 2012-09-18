// Copyright 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.schedule;

/**
 * A bean to store data about an externally hosted Chart. These typically have a
 * reflector URL that redirects the user to the "real" URL for the chart.
 * @author Luke
 * @version 5.0
 * @since 4.0
 */

public class ExternalChart extends Chart {

	private String _source;
	private String _url;
	private String _externalID;
	
	/**
	 * Creates the Chart bean.
	 * @param name the chart name
	 * @param a the Airport
	 */
	public ExternalChart(String name, Airport a) {
		super(name, a);
	}
	
	/**
	 * Returns whther this is an external chart.
	 * @return TRUE
	 */
	@Override
	public final boolean getIsExternal() {
		return true;
	}

	/**
	 * Returns the URL to the chart.
	 * @return the URL
	 */
	public String getURL() {
		return _url;
	}
	
	/**
	 * Returns the source of the chart.
	 * @return the source
	 */
	public String getSource() {
		return _source;
	}
	
	/**
	 * Returns the external provider's ID for the chart.
	 * @return id the provider chart ID
	 */
	public String getExternalID() {
		return _externalID;
	}
	
	/**
	 * Updates the URL to the chart.
	 * @param url the URL
	 */
	public void setURL(String url) {
		_url = url;
	}

	/**
	 * Updates the source of the chart.
	 * @param src the source
	 */
	public void setSource(String src) {
		_source = src;
	}
	
	/**
	 * Updates the external provider's ID for the chart.
	 * @param id the provider chart ID
	 */
	public void setExternalID(String id) {
		_externalID = id;
	}
}
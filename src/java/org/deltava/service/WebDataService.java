// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.service;

import java.sql.Connection;

/**
 * A class to support Web Services that require access to a JDBC data source.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class WebDataService extends WebService {

	protected Connection _con;

	/**
	 * Sets the JDBC connection for this Web Service.
	 * @param c the JDBC connection to use
	 */
	public final void setConnection(Connection c) {
		_con = c;
	}
	
	/**
	 * Helper method to return the number of entries to display.
	 * @param sctxt the Service Context
	 * @param defaultValue the default number of entries
	 * @return the value of the count parameter, or defaultVlue
	 */
	protected int getCount(ServiceContext sctxt, int defaultValue) {
		try {
			return Integer.parseInt(sctxt.getRequest().getParameter("count"));
		} catch (NumberFormatException nfe) {
			return defaultValue;
		}
	}
}
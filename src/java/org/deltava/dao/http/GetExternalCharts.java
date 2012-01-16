// Copyright 2011, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.net.*;
import java.util.Date;
import java.io.IOException;

import org.deltava.beans.schedule.ExternalChart;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to handle external chart redirects.
 * @author Luke
 * @version 4.1
 * @since 4.0
 */

public class GetExternalCharts extends DAO {
	
	/**
	 * Validates and determines the proper size for an external chart.
	 * @param c an ExternalChart
	 * @throws DAOException if an error occurs
	 */
	public void populate(ExternalChart c) throws DAOException {
		if ((c.getSize() > 0) || c.isLoaded())
			return;
		
		try {
			URL u = new URL(c.getURL());
			HttpURLConnection urlCon = (HttpURLConnection) u.openConnection();
			urlCon.setRequestMethod("HEAD");
			urlCon.setInstanceFollowRedirects(true);
			
			// Fetch the data
			int rspCode = urlCon.getResponseCode();
			if (rspCode != HttpURLConnection.HTTP_OK)
				throw new HTTPDAOException("Unknown Response Code", rspCode);

			// Get size and modification date
			c.setLastModified(new Date());
			int length = urlCon.getContentLength();
			if (length > 0)
				c.setSize(length);
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
	
	/**
	 * Loads an external chart.
	 * @param c an ExternalChart
	 * @throws DAOException if an error occurs
	 */
	public void load(ExternalChart c) throws DAOException {
		if (StringUtils.isEmpty(c.getURL()) || c.isLoaded())
			return;
		
		try {
			init(c.getURL());
			c.load(getIn());
			c.setLastModified(new Date());
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
}
// Copyright 2022, 2023, 2024, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static jakarta.servlet.http.HttpServletResponse.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.*;
import java.net.URLEncoder;

import org.deltava.beans.simbrief.*;

import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to fetch SimBrief packages.
 * @author Luke
 * @version 12.5
 * @since 10.3
 */

public class GetSimBrief extends DAO {
	
	/**
	 * SimBrief error data encapsulation class.
	 */
	public class SimBriefException extends HTTPDAOException {
		private final String _errorData;

		SimBriefException(String url, String errorData) {
			super(url, SC_BAD_REQUEST);
			_errorData = errorData;
			setLogStackDump(false);
		}
		
		@Override
		public String getMessage() {
			return _errorData;
		}
	}
	
	/**
	 * Loads a SimBrief briefing package.
	 * @param id the briefing ID
	 * @return the raw XML string
	 * @throws DAOException if an I/O error occurs
	 * @throws SimBriefException if a SimBrief error occurs
	 */
	public BriefingPackage load(String id) throws DAOException {
		String url = String.format("https://www.simbrief.com/ofp/flightplans/xml/%s.xml", id);
		try {
			init(url);
			
			// If we're an error, throw a status code exception
			int statusCode = getResponseCode();
			if (statusCode == SC_BAD_REQUEST)
				throw new SimBriefException(url, downloadStream());
			if (statusCode >= SC_BAD_REQUEST)
				throw new HTTPDAOException(url, statusCode);
			
			// Parse the data
			BriefingPackage sbdata = SimBriefParser.parse(downloadStream());
			sbdata.setSimBriefID(id);
			sbdata.setURL(url);
			return sbdata;
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
	
	/**
	 * Refreshes an existing SimBrief briefing package.
	 * @param userID the SimBrief user ID that generated the package
	 * @param staticID the static ID
	 * @return the raw XML string
	 * @throws DAOException if an I/O error occurs
	 * @throws SimBriefException if a SimBrief error occurs
	 */
	public String refresh(String userID, String staticID) throws DAOException {
		
		StringBuilder buf = new StringBuilder("https://www.simbrief.com/api/xml.fetcher.php?username=");
		buf.append(URLEncoder.encode(userID, UTF_8));
		if (!StringUtils.isEmpty(staticID))
			buf.append("&static_id=").append(staticID);
		
		try {
			setReturnErrorStream(true);
			init(buf.toString());
			
			// If we're an error, throw a status code exception, except a 400 in which case we parse the error
			int statusCode = getResponseCode();
			if (statusCode == SC_BAD_REQUEST)
				throw new SimBriefException(buf.toString(), downloadStream());
			if (statusCode > SC_BAD_REQUEST)
				throw new HTTPDAOException(buf.toString(), statusCode);
			
			return downloadStream();
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
}
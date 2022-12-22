// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

import org.deltava.beans.simbrief.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to fetch SimBrief packages.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class GetSimBrief extends DAO {
	
	/*
	 * Downloads the content in the URL.
	 */
	private String download() throws IOException {
		try (InputStream in = getIn(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[16384];
			int bytesRead = in.read(buffer);
			while (bytesRead > 0) {
				out.write(buffer, 0, bytesRead);
				bytesRead = in.read(buffer);
			}
			
			return new String(out.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Loads a SimBrief briefing package.
	 * @param id the briefing ID
	 * @return the raw XML string
	 * @throws DAOException if an I/O error occurs
	 */
	public BriefingPackage load(String id) throws DAOException {
		String url = String.format("https://www.simbrief.com/ofp/flightplans/xml/%s.xml", id);
		try {
			setCompression(Compression.GZIP);
			init(url);
			
			// If we're an error, throw a status code exception
			int statusCode = getResponseCode();
			if (statusCode >= SC_BAD_REQUEST)
				throw new HTTPDAOException(url, statusCode);
			
			// Parse the data
			BriefingPackage sbdata = SimBriefParser.parse(download());
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
	 */
	public String refresh(String userID, String staticID) throws DAOException {
		String url = String.format("https://www.simbrief.com/api/xml.fetcher.php?userid=%s&static_id=%s", userID, staticID);
		try {
			setCompression(Compression.GZIP);
			init(url);
			
			// If we're an error, throw a status code exception
			int statusCode = getResponseCode();
			if (statusCode >= SC_BAD_REQUEST)
				throw new HTTPDAOException(url, statusCode);
			
			return download();
		} catch (IOException ie) {
			throw new DAOException(ie);
		} finally {
			reset();
		}
	}
}
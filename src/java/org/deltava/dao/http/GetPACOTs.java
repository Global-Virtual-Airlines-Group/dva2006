// Copyright 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to loac Pacific Track data.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class GetPACOTs extends DAO implements TrackDAO {
	
	private String _url;

	/**
	 * Initializes the Data Access Object.
	 * @param url the URL to fetch from
	 */
	public GetPACOTs(String url) {
		super();
		_url = url;
	}

	/**
	 * Retrieves the PACOT information.
	 * @return a String with the formatted PACOT data
	 * @throws DAOException if an I/O error occurs
	 */
	public String getTrackInfo() throws DAOException {
		StringBuilder buf = new StringBuilder();
		try {
			LineNumberReader br = new LineNumberReader(new InputStreamReader(getStream(_url)));
			
			// Read through the URL results
			String data = br.readLine();
			while (data != null) {
				buf.append(data);
				data = br.readLine();
			}
			
			br.close();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
		
		// Split out the data
		String data = buf.toString();
		buf = new StringBuilder();
		int pos = data.indexOf("<input type=\"checkbox\"");
		while (pos != -1) {
			pos = data.indexOf("value=", pos);
			if (pos != -1) {
				int endPos = data.indexOf('"', pos + 7);
				if (endPos != -1) {
					buf.append(data.subSequence(pos + 7, endPos));
					buf.append("<br /><hr />");
					buf.append(System.getProperty("line.separator"));
				}
			}
			
			// Find next occurrance
			pos = data.indexOf("<input type=\"checkbox\"", pos);
		}
		
		// Return the data
		return buf.toString();
	}
}
// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import java.net.URLConnection;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to get North Atlantic Track data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */
public class GetNATs extends DAO implements TrackDAO {

	/**
	 * Initializes the DAO with a particular HTTP connection.
	 * @param c the HTTP connection
	 */
	public GetNATs(URLConnection c) {
		super(c);
	}

	/**
	 * Retrieves the NAT information.
	 * @return a String with the formatted NAT data
	 * @throws DAOException if an I/O error occurs
	 */
	public String getTrackInfo() throws DAOException {
		try {
			BufferedReader br = getReader();
			StringBuilder buf = new StringBuilder();

			// Read through the URL results
			boolean isWriting = false;
			String data = br.readLine();
			while (data != null) {
				data = data.trim();
				
				// Check for start/end of NAT segment
				if (!isWriting && data.contains("NAT-")) {
					buf.append(data);
					buf.append("<br />");
					buf.append(System.getProperty("line.separator"));
					isWriting = true;
				} else if (isWriting && (data.startsWith("END OF PART"))) {
					buf.append(data);
					buf.append("<br /><hr />");
					buf.append(System.getProperty("line.separator"));
					isWriting = false;
				} else if (isWriting) {
					buf.append(data);
					buf.append("<br />");
					buf.append(System.getProperty("line.separator"));
				}

				// Read next line
				data = br.readLine();
			}

			// Return the data
			br.close();
			return buf.toString();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}
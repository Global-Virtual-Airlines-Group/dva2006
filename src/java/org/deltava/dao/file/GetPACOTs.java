// Copyright 2006, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to loac Pacific Track data.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class GetPACOTs extends DAO implements TrackDAO {

	/**
	 * Initializes the DAO with a particular stream.
	 * @param is the stream
	 */
	public GetPACOTs(InputStream is) {
		super(is);
	}

	/**
	 * Retrieves the PACOT information.
	 * @return a String with the formatted PACOT data
	 * @throws DAOException if an I/O error occurs
	 */
	public String getTrackInfo() throws DAOException {
		
		StringBuilder buf = new StringBuilder();
		try {
			BufferedReader br = getReader();
			
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
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
public class GetNATs extends DAO {

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
			StringWriter out = new StringWriter();

			//	Start/end flags
			boolean isSOF = false;
			boolean isEOF = false;

			// Read through the URL results
			String data = br.readLine();
			while (data != null) {
				data = data.trim();
				isEOF = isEOF || (isSOF && (data.startsWith("<br>")));

				// If we're after the SOF but before the EOF, append the data
				if (isSOF && !(isEOF)) {
					out.write(data);
					out.write("<br>");
					out.write(System.getProperty("line.separator"));
				}

				// Read next line
				isSOF = isSOF || (data.indexOf("<pre>") != -1);
				data = br.readLine();
			}

			// Return the data
			return out.toString();
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}
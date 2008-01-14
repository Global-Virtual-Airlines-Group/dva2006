// Copyright 2005, 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.DAOException;

import org.deltava.util.StringUtils;

/**
 * A Data Access Object to get North Atlantic Track data.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

public class GetNATs extends DAO implements TrackDAO {
	
	private String _notam;

	/**
	 * Initializes the DAO with a particular stream.
	 * @param is the stream
	 */
	public GetNATs(InputStream is) {
		super(is);
	}
	
	/**
	 * Initializes the DAO with a pre-generated NOTAM. <i>This is typically used for data migration</i>
	 * @param notam the NOTAM text
	 */
	public GetNATs(String notam) {
		super(null);
		_notam = notam;
	}
	
	/**
	 * Retrieves the NAT information.
	 * @return a String with the formatted NAT data
	 * @throws DAOException if an I/O error occurs
	 */
	public String getTrackInfo() throws DAOException {
		if (_notam != null)
			return _notam;
		
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
			_notam = buf.toString();
			return _notam;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}

	/**
	 * Returns the Waypoints for each North Atlantic Track.
	 * @return a Map of {@link OceanicWaypoints} beans, keyed by track code
	 * @throws DAOException if an I/O error occurs
	 */
	public Map<String, Collection<String>> getWaypoints() throws DAOException {
		if (_notam == null)
			getTrackInfo();
		
		// Parse the NOTAM data
		try {
			Map<String, Collection<String>> results = new TreeMap<String, Collection<String>>();
			BufferedReader br = new BufferedReader(new StringReader(_notam));
			while (br.ready()) {
				String data = br.readLine();
				br.mark(512);
				
				// Check if the track code is the first character
				if ((data != null) && (data.length() > 2) && (data.charAt(1) == ' ')) {
					String el = br.ready() ? br.readLine() : null;
					String wl = br.ready() ? br.readLine() : null;
					
					// Validate the next two lines - if they're good then parse the track
					if ((el != null) && (el.startsWith("EAST LVLS") && (wl != null) && (wl.startsWith("WEST LVLS")))) {
						String code = data.substring(0, 1);
						int end = data.indexOf('<');
						if (end == -1)
							end = data.length();
						
						Collection<String> wps = new LinkedHashSet<String>(StringUtils.split(data.substring(2, end), " "));
						results.put(code, wps);
					}
				} else if (data == null)
					break;
				
				br.reset();
			}
			
			br.close();
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}
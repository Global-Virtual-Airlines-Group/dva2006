// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;

/**
 * A Data Access Object to read from the Linux /proc filesystem.
 * @author luke
 * @version 1.0
 * @since 1.0
 */

public class GetProcData extends DAO {

	/**
	 * Initializes the Data Access Object.
	 */
	public GetProcData() {
		super((InputStream) null);
	}

	/**
	 * Retrieves system uptime from /proc/uptime.
	 * @return the machine's uptime in milliseconds
	 * @throws DAOException if an I/O error occurs
	 */
	public long getUptime() throws DAOException {
		try {
			InputStream is = new FileInputStream("/proc/uptime");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String data = br.readLine();
			if (data == null)
				throw new IOException("Empty /proc/uptime");
			
			// Get the first entry
			is.close();
			if (data.indexOf(' ') != -1)
				data = data.substring(0, data.indexOf(' '));
			try {
				return (long) Math.floor(Double.parseDouble(data));
			} catch (NumberFormatException nfe) {
				throw new IOException("Unparseable uptime - " + data);
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	/**
	 * Returns the system load average from /proc/loadavg.
	 * @return a List of doubles, with processes waiting in the last 1, 5 and 15 minutes
	 * @throws DAOException if an I/O error occurs
	 */
	public List<Double> getLoad() throws DAOException {
		try {
			InputStream is = new FileInputStream("/proc/loadavg");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String data = br.readLine();
			if (data == null)
				throw new IOException("Empty /proc/loadavg");
			
			// Split the entries
			is.close();
			List<Double> results = new ArrayList<Double>(3);
			StringTokenizer tkns = new StringTokenizer(data, " ");
			if (tkns.countTokens() < 3)
				throw new IOException("Unparseable load average - " + data);
			
			// Parse the entries
			for (int x = 0; x < 3; x++) {
				try {
					results.add(new Double(Double.parseDouble(tkns.nextToken())));
				} catch (NumberFormatException nfe) {
					results.add(new Double(0.0));
				}
			}
			
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}
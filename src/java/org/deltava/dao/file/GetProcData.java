// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to read from the Linux /proc filesystem.
 * @author Luke
 * @version 2.2
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
	public int getUptime() throws DAOException {
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
				return (int) Math.floor(Double.parseDouble(data));
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

	/**
	 * Returns system memory data from /proc/meminfo.
	 * @return a Map of Integers, keyed by value
	 * @throws DAOException if an I/O error occurs
	 */
	public Map<String, Integer> getMemory() throws DAOException {
		try {
			Map<String, Integer> results = new LinkedHashMap<String, Integer>();
			InputStream is = new FileInputStream("/proc/meminfo");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			// Parse the file
			String data = br.readLine();
			while (data != null) {
				int pos = data.indexOf(':');
				if ((pos != -1) && data.endsWith(" kB")) {
					String key = data.substring(0, pos);
					String rawValue = data.substring(pos + 1, data.lastIndexOf(' '));
					int value = StringUtils.parse(rawValue.substring(rawValue.lastIndexOf(' ') + 1), 0);
					results.put(key, Integer.valueOf(value));
				}
				
				data = br.readLine();
			}
			
			// Close and return
			is.close();
			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}
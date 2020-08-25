// Copyright 2007, 2008, 2012, 2016, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.file;

import java.io.*;
import java.util.*;

import org.deltava.dao.DAOException;
import org.deltava.util.StringUtils;

/**
 * A Data Access Object to read from the Linux /proc filesystem.
 * @author Luke
 * @version 9.1
 * @since 1.0
 */

public class GetProcData extends DAO {
	
	private final File _proc = new File("/proc");

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
		try (InputStream is = new FileInputStream(new File(_proc, "uptime")); BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			String data = br.readLine();
			if (data == null)
				throw new IOException("Empty /proc/uptime");

			// Get the first entry
			if (data.indexOf('.') > -1)
				data = data.substring(0, data.indexOf('.'));
			if (data.indexOf(' ') > -1)
				data = data.substring(0, data.indexOf(' '));

			try {
				return Long.parseLong(data);
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
		try (InputStream is = new FileInputStream(new File(_proc, "loadavg")); BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			List<Double> results = new ArrayList<Double>(3);
			String data = br.readLine();
			if (data == null)
				throw new IOException("Empty /proc/loadavg");

			// Split the entries
			StringTokenizer tkns = new StringTokenizer(data, " ");
			if (tkns.countTokens() < 3)
				throw new IOException("Unparseable load average - " + data);

			// Parse the entries
			for (int x = 0; x < 3; x++) {
				try {
					results.add(Double.valueOf(Double.parseDouble(tkns.nextToken())));
				} catch (NumberFormatException nfe) {
					results.add(Double.valueOf(0));
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
		try (InputStream is = new FileInputStream(new File(_proc, "meminfo")); BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			Map<String, Integer> results = new LinkedHashMap<String, Integer>();
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

			return results;
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}
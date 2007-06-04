// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.servinfo;

import java.io.*;
import java.net.*;

import org.apache.log4j.Logger;

import org.deltava.beans.servinfo.*;

import org.deltava.dao.file.FileURLConnection;

import org.deltava.util.system.SystemData;

/**
 * A Connection Factory for local/remote ServInfo data feeds.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

class ConnectionFactory {
	
	private static final Logger log = Logger.getLogger(ConnectionFactory.class);

	/**
	 * Helper method to open a connection to a particular URL.
	 */
	static URLConnection getURL(String dataURL) {
		if (dataURL == null)
			return null;

		if (dataURL.startsWith("http://")) {
			try {
				URL url = new URL(dataURL);
				URLConnection con = url.openConnection();
				con.setConnectTimeout(1500);
				con.setReadTimeout(2500);
				return con;
			} catch (IOException ie) {
				log.error("Error getting HTTP connection " + ie.getMessage(), ie);
				return null;
			}
		}

		// Build a file URL
		try {
			return new FileURLConnection(dataURL);
		} catch (IOException ie) {
			log.error("Error getting FILE connection " + ie.getMessage(), ie);
			return null;
		}
	}
	
	/**
	 * Helper method to determine if a file exists on the filesystem.
	 */
	private static boolean isLocal(String fName) {
		if (fName == null)
			return false;
		
		// Check that the file exists
		File f = new File(fName);
		return f.exists();
	}
	
	/**
	 * Returns a Connection to the network Status file, either local or over HTTP
	 * @param networkName the network name
	 * @return a URLConnection, or null if none can be established
	 */
	static URLConnection getStatus(String networkName) {
		String localStatus = SystemData.get("online." + networkName.toLowerCase() + ".local.status");
		if (isLocal(localStatus))
			return getURL(localStatus);
		
		// Get the URL to the online version
		return getURL(SystemData.get("online." + networkName.toLowerCase() + ".status_url"));
	}
	
	/**
	 * Returns a Connection to the network Traffic file, either local or over HTTP
	 * @param status the network status object
	 * @return a URLConnection, or null if none can be established
	 */
	static NetworkDataURL getInfo(NetworkStatus status) {
		NetworkDataURL local = status.getLocal();
		if (isLocal(local.getURL()))
			return local;
			
		return status.getDataURL(false);
	}
}
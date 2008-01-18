// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.servinfo;

import java.io.*;

import org.apache.log4j.Logger;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;

import org.deltava.beans.servinfo.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.util.system.SystemData;

/**
 * A Connection Factory for local/remote ServInfo data feeds.
 * @author Luke
 * @version 2.1
 * @since 1.0
 */

class ConnectionFactory {
	
	private static final Logger log = Logger.getLogger(ConnectionFactory.class);

	/**
	 * Helper method to determine if a file exists on the filesystem.
	 */
	private static boolean isLocal(String fName) {
		if (fName == null)
			return false;
		
		File f = new File(fName);
		if (!f.exists())
			log.warn("Cannot find local file " + fName);
		
		return f.exists();
	}
	
	/**
	 * Returns a Stream to the network Status file, either local or over HTTP.
	 * @param networkName the network name
	 * @return an InputStream to the content
	 * @throws IOException if an I/O error occurs
	 */
	static InputStream getStatus(String networkName) throws IOException {
		String localStatus = SystemData.get("online." + networkName.toLowerCase() + ".local.status");
		if (isLocal(localStatus))
			return new FileInputStream(localStatus);
		
		// Get the URL to the online version
		HttpClient hc = new HttpClient();
		hc.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_0);
		hc.getParams().setParameter("http.useragent",  VersionInfo.USERAGENT);
		hc.getParams().setParameter("http.tcp.nodelay", Boolean.TRUE);
		hc.getParams().setParameter("http.socket.timeout", new Integer(2500));
		hc.getParams().setParameter("http.connection.timeout", new Integer(1500));
		hc.getParams().setParameter("http.protocol.allow-circular-redirects", Boolean.FALSE);
		
		// Open the connection
		GetMethod gm = new GetMethod(SystemData.get("online." + networkName.toLowerCase() + ".status_url"));
		gm.setFollowRedirects(false);
		
		// Get the content
		int resultCode = hc.executeMethod(gm);
		if (resultCode != 200)
			throw new IOException(gm.getStatusText());
		
		return new ByteArrayInputStream(gm.getResponseBody(65536));
	}
	
	/**
	 * Returns a stream to the network Traffic file, either local or over HTTP.
	 * @param status the network status object
	 * @return a NetworkDataURL
	 * @throws IOException if an I/O error occurs
	 */
	static InputStream getInfo(NetworkStatus status) throws IOException {
		NetworkDataURL local = status.getLocal();
		if (isLocal(local.getURL()))
			return new FileInputStream(local.getURL());
			
		// Get the URL to the online version
		NetworkDataURL remote = status.getDataURL(true);
		HttpClient hc = new HttpClient();
		hc.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_0);
		hc.getParams().setParameter("http.useragent",  VersionInfo.USERAGENT);
		hc.getParams().setParameter("http.tcp.nodelay", Boolean.TRUE);
		hc.getParams().setParameter("http.socket.timeout", new Integer(2500));
		hc.getParams().setParameter("http.connection.timeout", new Integer(1500));
		hc.getParams().setParameter("http.protocol.allow-circular-redirects", Boolean.FALSE);
		
		// Open the connection
		GetMethod gm = new GetMethod(remote.getURL());
		gm.setFollowRedirects(false);
		
		// Get the content
		int resultCode = hc.executeMethod(gm);
		if (resultCode != 200)
			throw new IOException(gm.getStatusText());
		
		remote.logUsage(true);
		return new ByteArrayInputStream(gm.getResponseBody(524288));
	}
}
// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.servinfo;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.servinfo.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.GetServInfo;

import org.deltava.util.ThreadUtils;
import org.deltava.util.http.HttpTimeoutHandler;

/**
 * A worker thread to asynchronously load ServInfo data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServInfoLoader implements Runnable {
	
	private static final Logger log = Logger.getLogger(ServInfoLoader.class);
	private static final Map<String, LoaderThread> LOADERS = new LinkedHashMap<String, LoaderThread>();

	private String _url;
	private String _network;
	private NetworkInfo _info;
	
	private static class LoaderThread {
		
		private long _startTime;
		private Thread _loader;
		
		LoaderThread(Thread t) {
			super();
			_loader = t;
			_startTime = System.currentTimeMillis();
		}
		
		public Thread getThread() {
			return _loader;
		}
		
		public long getRunTime() {
			return System.currentTimeMillis() - _startTime;
		}
	}
	
	/**
	 * Determines if a particular network's data is being loaded.
	 * @param network the network name
	 * @return TRUE if the network data is being loaded, otherwise FALSE
	 * @throws NullPointerException if network is null
	 */
	public static synchronized final boolean isLoading(String network) {
		LoaderThread loader = LOADERS.get(network.toUpperCase());
		boolean isLoading = ThreadUtils.isAlive(loader.getThread());
		
		// Check the threads and kill the ones that are misbehaving
		for (Iterator<String> i = LOADERS.keySet().iterator(); i.hasNext(); ) {
			String networkName = i.next();
			LoaderThread lt = LOADERS.get(network);
			Thread t = lt.getThread();
			if (ThreadUtils.isAlive(t) && (lt.getRunTime() > 25000)) {
				log.warn("Killing ServInfo loader thread for " + networkName);
				t.interrupt();
				ThreadUtils.kill(t, 1500);
			} else if (!ThreadUtils.isAlive(lt.getThread()))
				i.remove();
		}
		
		return isLoading;
	}
	
	/**
	 * Adds a network information loader thread. The loader thread will be started automatically if it is
	 * not already running.
	 * @param network the network name
	 * @param loaderThread the loader thread
	 * @throws NullPointerException if network or loaderThread are null
	 */
	public static final synchronized void addLoader(String network, Thread loaderThread) {
		if (LOADERS.containsKey(network.toUpperCase()))
			throw new IllegalArgumentException(network + " data already being loaded!");
		
		LOADERS.put(network.toUpperCase(), new LoaderThread(loaderThread));
		if (!loaderThread.isAlive())
			loaderThread.start();
	}
	
	/**
	 * Initializes the worker.
	 * @param url the network status URL
	 * @param networkName the network name 
	 */
	public ServInfoLoader(String url, String networkName) {
		super();
		_url = url;
		_network = networkName;
	}
	
	/**
	 * Helper method to open a connection to a particular URL.
	 */
	private HttpURLConnection getURL(String dataURL) {
		try {
			URL url = new URL(null, dataURL, new HttpTimeoutHandler(1750));
			return (HttpURLConnection) url.openConnection();
		} catch (IOException ie) {
			log.error("Error getting HTTP connection " + ie.getMessage(), ie);
			return null;
		}
	}
	
	/**
	 * Returns the retrieved network traffic information. 
	 * @return the NetworkInfo bean
	 */
	public NetworkInfo getInfo() {
		return _info;
	}

	/**
	 * Starts the thread.
	 * @see Runnable#run()
	 */
	public void run() {

		// Get the URL connection
		HttpURLConnection con = getURL(_url);
		if (con == null)
			return;

		// Get the network URLs
		NetworkStatus status = null;
		try {
			GetServInfo sdao = new GetServInfo(con);
			sdao.setUseCache(true);
			status = sdao.getStatus(_network);
			if (status.getCached())
				log.info("Using cached " + _network + " network status");
		} catch (DAOException de) {
			log.error("Error loading " + _network.toUpperCase() + " status - " + de.getMessage(), de.getLogStackDump() ? de : null);
		} finally {
			con.disconnect();
		}
		
		// If we received nothing, abort
		if (status == null)
			return;

		// Get network status
		NetworkDataURL nd = status.getDataURL(false);
		con = getURL(nd.getURL());
		if (con == null)
			return;
		
		// Get the network info
		try {
			GetServInfo idao = new GetServInfo(con);
			idao.setUseCache(true);
			idao.setBufferSize(30720);
			_info = idao.getInfo(_network);
			if (_info.getCached())
				log.info("Using cached " + _network + " connection data");
			else
				nd.logUsage(true);
		} catch (DAOException de) {
			nd.logUsage(false);
			Throwable re = de.getCause();
			if (re instanceof SocketTimeoutException) {
				log.warn("HTTP Timeout connecting to " + con.getURL().toString());
			} else if (re instanceof ConnectException) {
				log.warn("Connection to " + con.getURL().toString() + " refused");
			} else if (re instanceof FileNotFoundException) {
				log.error("File not found " + re.getMessage());
			} else {
				log.error("Error loading " + _network.toUpperCase() + " info - " + de.getMessage(), de);
			}
		} finally {
			con.disconnect();
		}
		
		// Log status info
		log.info("ServInfo load complete - " + nd);
	}
}
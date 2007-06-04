// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.servinfo;

import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.servinfo.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.*;

import org.deltava.util.ThreadUtils;

/**
 * A worker thread to asynchronously load ServInfo data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ServInfoLoader implements Runnable {

	private static final Logger log = Logger.getLogger(ServInfoLoader.class);
	private static final Map<String, LoaderThread> LOADERS = new LinkedHashMap<String, LoaderThread>();

	private String _network;
	private NetworkInfo _info;

	/**
	 * Determines if a particular network's data is being loaded.
	 * @param network the network name
	 * @return TRUE if the network data is being loaded, otherwise FALSE
	 * @throws NullPointerException if network is null
	 */
	public static synchronized final boolean isLoading(String network) {
		LoaderThread loader = LOADERS.get(network.toUpperCase());
		boolean isLoading = (loader != null) && ThreadUtils.isAlive(loader.getThread());

		// Check the threads and kill the ones that are misbehaving
		for (Iterator<String> i = LOADERS.keySet().iterator(); i.hasNext();) {
			String networkName = i.next();
			LoaderThread lt = LOADERS.get(networkName);
			Thread t = lt.getThread();
			if (ThreadUtils.isAlive(t) && (lt.getRunTime() > 25000)) {
				log.warn("Killing ServInfo loader thread for " + networkName);
				t.interrupt();
				ThreadUtils.kill(t, 1500);
				i.remove();
			} else if (!ThreadUtils.isAlive(t))
				i.remove();
		}

		return isLoading;
	}

	/**
	 * Adds a network information loader thread. The loader thread will be started automatically if it is not already
	 * running.
	 * @param network the network name
	 * @param loaderThread the loader thread
	 * @throws NullPointerException if network or loaderThread are null
	 */
	public static final synchronized void addLoader(String network, Thread loaderThread) {
		if (isLoading(network))
			throw new IllegalArgumentException(network + " data already being loaded!");

		// Get the current thread stack
		LoaderThread lt = new LoaderThread(loaderThread);
		lt.setStack(Thread.currentThread().getStackTrace());
		LOADERS.put(network.toUpperCase(), lt);
		if (!loaderThread.isAlive())
			loaderThread.start();
	}
	
	/**
	 * Returns all running ServInfo loader threads.
	 * @return a Map of Threads, keyed by network name
	 */
	public static final synchronized Map<String, Thread> getLoaders() {
		Map<String, Thread> results = new LinkedHashMap<String, Thread>();
		for (Iterator<String> i = LOADERS.keySet().iterator(); i.hasNext(); ) {
			String networkName = i.next();
			LoaderThread lt = LOADERS.get(networkName);
			results.put(networkName, lt.getThread());
		}
		
		return results;
	}

	/**
	 * Initializes the worker.
	 * @param networkName the network name
	 */
	public ServInfoLoader(String networkName) {
		super();
		_network = networkName;
	}

	/**
	 * Helper method to disconnect HTTP connections only.
	 */
	private void disconnect(URLConnection con) {
		if (con instanceof HttpURLConnection)
			((HttpURLConnection) con).disconnect();
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

		// Get a connection to the network info
		URLConnection con = ConnectionFactory.getStatus(_network);
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
			log.error("Error loading " + _network.toUpperCase() + " status - " + de.getMessage(),
					de.getLogStackDump() ? de : null);
		} finally {
			disconnect(con);
		}

		// If we received nothing, abort
		if (status == null)
			return;

		// Get network status
		NetworkDataURL nd = ConnectionFactory.getInfo(status);
		con = ConnectionFactory.getURL(nd.getURL());
		if (con == null)
			return;

		// Get the network info
		try {
			GetServInfo idao = new GetServInfo(con);
			idao.setUseCache(nd != status.getLocal());
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
			disconnect(con);
		}

		// Log status info
		log.info("ServInfo load complete - " + nd);
	}
}
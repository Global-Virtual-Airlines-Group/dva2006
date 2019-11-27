// Copyright 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ipc;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.dao.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;
import org.gvagroup.jdbc.*;

/**
 * A daemon to listen for inter-process events.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class IPCDaemon implements Runnable {

	private static final Logger log = Logger.getLogger(IPCDaemon.class);
	
	/**
	 * Returns the thread name.
	 * @return the tread name
	 */
	@Override
	public String toString() {
		return SystemData.get("airline.code") + " IPC Daemon";
	}

	/**
	 * Executes the thread.
	 */
	@Override
	public void run() {
		log.info("Starting");
		ConnectionPool cPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);

		while (!Thread.currentThread().isInterrupted()) {
			try {
				EventDispatcher.waitForEvent();
				Collection<SystemEvent> events = EventDispatcher.getEvents();
				Connection con = null;
				try {
					con = cPool.getConnection();
					for (SystemEvent event : events) {
						switch (event.getCode()) {
							case AIRLINE_RELOAD:
								log.warn(SystemData.get("airline.code") + " Reloading Airlines");
								GetAirline aldao = new GetAirline(con);
								SystemData.add("airlines", aldao.getAll());
								break;
								
							case TZ_RELOAD:
								log.warn(SystemData.get("airline.code") + " Reloading Time Zones");
								GetTimeZone tzdao = new GetTimeZone(con);
								tzdao.initAll();
								break;
								
							case AIRPORT_RELOAD:
								log.warn(SystemData.get("airline.code") + " Reloading Airports");
								GetAirport apdao = new GetAirport(con);
								SystemData.add("airports", apdao.getAll());
								break;
								
							case CACHE_FLUSH:
								IDEvent ie = (IDEvent) event;
								CacheManager.invalidate(ie.getID(), false);
								log.warn(SystemData.get("airline.code") + " Flushing cache " + ie.getID());
								break;
								
							default:
								break;
						}
					}
				} catch (ConnectionPoolException | DAOException cpde) {
					log.error(cpde.getMessage(), cpde);
				} finally {
					cPool.release(con);
				}
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		
		log.info("Stopping");
		EventDispatcher.unregister();
	}
}
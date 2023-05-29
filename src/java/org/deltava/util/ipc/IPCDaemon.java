// Copyright 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ipc;

import java.util.*;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.stats.*;

import org.deltava.dao.*;

import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;
import org.gvagroup.jdbc.*;

/**
 * A daemon to listen for inter-process events.
 * @author Luke
 * @version 11.0
 * @since 1.0
 */

public class IPCDaemon implements Runnable {

	private static final Logger log = LogManager.getLogger(IPCDaemon.class);
	
	@Override
	public String toString() {
		return SystemData.get("airline.code") + " IPC Daemon";
	}

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
								
							case AIRPORT_RENAME:
								ie = (IDEvent) event;
								if (ie.getData() == null) break;
								log.warn(SystemData.get("airline.code") + " renaming Airport " + ie.getData() + " to " + ie.getID());
								
								// Update accomplishments
								try {
									con.setAutoCommit(false);
									Collection<Accomplishment> accs = new LinkedHashSet<Accomplishment>();
									GetAccomplishment acdao = new GetAccomplishment(con);
									accs.addAll(acdao.getByUnit(AccomplishUnit.AIRPORTS));
									accs.addAll(acdao.getByUnit(AccomplishUnit.AIRPORTD));
									accs.addAll(acdao.getByUnit(AccomplishUnit.AIRPORTA));
									accs.removeIf(acc -> !acc.renameChoice(ie.getID(), ie.getData()));
								
									if (!accs.isEmpty()) {
										SetAccomplishment acwdao = new SetAccomplishment(con);
										for (Accomplishment acc : accs) {
											log.warn(SystemData.get("airline.code") + " updating Accomplishment " + acc.getName());
											acwdao.write(acc);
										}
										
										con.commit();
									}
								} catch (Exception e) {
									throw new DAOException(e);
								}
								
								break;
								
							case AIRCRAFT_RENAME:
								ie = (IDEvent) event;
								if (ie.getData() == null) break;
								log.warn(SystemData.get("airline.code") + " renaming Aircraft " + ie.getData() + " to " + ie.getID());
								
								// Update accomplishments
								try {
									con.setAutoCommit(false);
									GetAccomplishment acdao = new GetAccomplishment(con);
									Collection<Accomplishment> accs = acdao.getByUnit(AccomplishUnit.AIRCRAFT);
									accs.removeIf(acc -> !acc.renameChoice(ie.getID(), ie.getData()));
									
									if (!accs.isEmpty()) {
										SetAccomplishment acwdao = new SetAccomplishment(con);
										for (Accomplishment acc : accs) {
											log.warn(SystemData.get("airline.code") + " updating Accomplishment " + acc.getName());
											acwdao.write(acc);
										}
										
										con.commit();		
									}
								} catch (Exception e) {
									throw new DAOException(e);
								}
								
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
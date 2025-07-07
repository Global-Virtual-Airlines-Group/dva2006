// Copyright 2007, 2008, 2009, 2010, 2011, 2012, 2015, 2019, 2023, 2024, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.ipc;

import java.util.*;
import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.flight.FlightReport;
import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.discord.Bot;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;
import org.gvagroup.pool.*;

/**
 * A daemon to listen for inter-process events.
 * @author Luke
 * @version 12.1
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
		JDBCPool cPool = (JDBCPool) SystemData.getObject(SystemData.JDBC_POOL);
		String aCode = SystemData.get("airline.code");

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
								log.warn("{} Reloading Airlines", aCode);
								GetAirline aldao = new GetAirline(con);
								SystemData.add("airlines", aldao.getAll());
								break;
								
							case TZ_RELOAD:
								log.warn("{} Reloading Time Zones", aCode);
								GetTimeZone tzdao = new GetTimeZone(con);
								tzdao.initAll();
								break;
								
							case AIRPORT_RELOAD:
								log.warn("{} Reloading Airports", aCode);
								GetAirport apdao = new GetAirport(con);
								SystemData.add("airports", apdao.getAll());
								break;
								
							case CACHE_FLUSH:
								IDEvent ie = (IDEvent) event;
								CacheManager.invalidate(ie.getID(), false);
								log.warn("{} Flushing cache {}", aCode, ie.getID());
								break;
								
							case FLIGHT_REPORT:
								ie = (IDEvent) event;
								boolean hasDiscord = SystemData.getBoolean("discord.bot");
								if (!aCode.equals(ie.getData()) || !hasDiscord) {
									log.info("{} ignoring Flight Report {} (for {})", aCode, ie.getID(), ie.getData());
									break;
								}
								
								// Get the flgiht Report and the pilot
								GetFlightReports frdao = new GetFlightReports(con);
								FlightReport fr = frdao.get(StringUtils.parse(ie.getID(), 0), SystemData.get("airline.db"));
								if (fr == null) {
									log.warn("{} cannot find Flight Report {}", aCode, ie.getID());
									break;
								}
								
								GetPilot pdao = new GetPilot(con);
								Pilot p = pdao.get(fr.getAuthorID());
								log.info("{} sending Flight Report {} notification to Discord", aCode, fr.getShortCode());
								Bot.sendFlightReport(fr, p);
								break;
								
							case AIRPORT_RENAME:
								ie = (IDEvent) event;
								if (ie.getData() == null) break;
								log.warn("{} renaming Airport {} to {}", aCode, ie.getData(), ie.getID());
								
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
											log.warn("{} updating Accomplishment {}", aCode, acc.getName());
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
								log.warn("{} renaming Aircraft {} to {}", SystemData.get("airline.code"), ie.getData(), ie.getID());
								
								// Update accomplishments
								try {
									con.setAutoCommit(false);
									GetAccomplishment acdao = new GetAccomplishment(con);
									Collection<Accomplishment> accs = acdao.getByUnit(AccomplishUnit.AIRCRAFT);
									accs.removeIf(acc -> !acc.renameChoice(ie.getID(), ie.getData()));
									
									if (!accs.isEmpty()) {
										SetAccomplishment acwdao = new SetAccomplishment(con);
										for (Accomplishment acc : accs) {
											log.warn("{} updating Accomplishment {}", aCode, acc.getName());
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
					log.atError().withThrowable(cpde).log(cpde.getMessage());
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
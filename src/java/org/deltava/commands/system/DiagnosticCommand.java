// Copyright 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.io.Serializable;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.servlet.ServletScoreboard;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.dao.wsdl.*;
import org.deltava.jdbc.*;

import org.deltava.taskman.TaskScheduler;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.acars.*;
import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to display diagnostic infomration.
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class DiagnosticCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(DiagnosticCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@SuppressWarnings("unchecked")
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get system uptime and load average if running on Linux
		if ("Linux".equals(System.getProperty("os.name"))) {
			try {
				Calendar cld = Calendar.getInstance();
				GetProcData procdao = new GetProcData();
				int osRunTime = procdao.getUptime();
				cld.add(Calendar.SECOND, osRunTime * -1);
				ctx.setAttribute("osStart", cld.getTime(), REQUEST);
				ctx.setAttribute("osExecTime", new Integer(osRunTime), REQUEST);
				ctx.setAttribute("loadAvg", procdao.getLoad(), REQUEST);
				ctx.setAttribute("osMemInfo", procdao.getMemory(), REQUEST);
			} catch (DAOException de) {
				log.error(de.getMessage());
			}
		}

		// Get the Task Scheduler data
		TaskScheduler tSched = (TaskScheduler) SystemData.getObject(SystemData.TASK_POOL);
		if (tSched != null)
			ctx.setAttribute("taskInfo", tSched.getTaskInfo(), REQUEST);
		
		// Get servlet scoreboard
		ctx.setAttribute("scoreBoard", ServletScoreboard.getScoreboard(), REQUEST);

		// Get ACARS server data
		if (SystemData.getBoolean("acars.enabled") && SharedData.getApplications().contains("ACARS")) {
			// Get the ACARS Connection pool data and save in the request
			ACARSAdminInfo acarsPool = (ACARSAdminInfo) SharedData.get(SharedData.ACARS_POOL);
			ctx.setAttribute("acarsPool", IPCUtils.deserialize(acarsPool.getPoolInfo(true)), REQUEST);

			// Get the acars worker info data and save in the request
			ACARSWorkerInfo acarsInfo = (ACARSWorkerInfo) SharedData.get(SharedData.ACARS_DAEMON);
			ctx.setAttribute("workers", acarsInfo.getWorkers(), REQUEST);

			// Save the ACARS statistics in the request
			try {
				Connection con = ctx.getConnection();
				GetACARSBandwidth bwdao = new GetACARSBandwidth(con);
				ctx.setAttribute("acarsBW", bwdao.getLatest(), REQUEST);
			} catch (DAOException de) {
				log.error("Error loading ACARS bandwidth - " + de.getMessage(), de);
			} finally {
				ctx.release();
			}
		}
		
		// Get JDBC Connection Pool data
		Collection<String> appNames = SharedData.getApplications();
		Map<String, ConnectionPool> pools = new HashMap<String, ConnectionPool>();
		for (Iterator<String> i = appNames.iterator(); i.hasNext(); ) {
			String appName = i.next();
			Serializable rawPool = (Serializable) SharedData.get(SharedData.JDBC_POOL + appName);
			ConnectionPool jdbcPool = (ConnectionPool) IPCUtils.reserialize(rawPool);
			pools.put(appName, jdbcPool);
		}
		
		// Save connection pool data
		if (!pools.isEmpty()) {
			ctx.setAttribute("appNames", appNames, REQUEST);
			ctx.setAttribute("jdbcPools", pools, REQUEST);
		}
		
		// Get DAO cache properties
		Collection<CachingDAO> daoCaches = new ArrayList<CachingDAO>();
		daoCaches.add(new GetCoolerChannels(null));
		daoCaches.add(new GetCoolerThreads(null));
		daoCaches.add(new GetEquipmentType(null));
		daoCaches.add(new GetExamQuestions(null));
		daoCaches.add(new GetMessageTemplate(null));
		daoCaches.add(new GetImage(null));
		daoCaches.add(new GetNavData(null));
		daoCaches.add(new GetNavAirway(null));
		daoCaches.add(new GetNavRoute(null));
		daoCaches.add(new GetFlightReportRecognition(null));
		daoCaches.add(new GetStatistics(null));
		daoCaches.add(new GetSystemData(null));
		daoCaches.add(new GetUserData(null));
		daoCaches.add(new GetPilot(null));
		daoCaches.add(new GetLibrary(null));
		daoCaches.add(new GetACARSRunways(null));
		daoCaches.add(new GetACARSLog(null));
		daoCaches.add(new GetIPLocation(null));
		daoCaches.add(new GetFAWeather());
		daoCaches.add(new GetNOAAWeather());
		daoCaches.add(new org.deltava.dao.ipc.GetACARSPool());
		ctx.setAttribute("daoCaches", daoCaches, REQUEST);
		
		// Get Virtual Machine properties
		Runtime rt = Runtime.getRuntime();
		ctx.setAttribute("cpuCount", Integer.valueOf(rt.availableProcessors()), REQUEST);
		ctx.setAttribute("totalMemory", new Long(rt.totalMemory()), REQUEST);
		ctx.setAttribute("maxMemory", new Long(rt.maxMemory()), REQUEST);
		ctx.setAttribute("freeMemory", new Long(rt.freeMemory()), REQUEST);
		ctx.setAttribute("pctMemory", new Double(100 - (Math.round(rt.freeMemory() * 100.0 / rt.totalMemory()))), REQUEST);

		// Get time zone info
		TimeZone tz = TimeZone.getDefault();
		ctx.setAttribute("timeZone", tz, REQUEST);
		ctx.setAttribute("tzName", tz.getDisplayName(tz.inDaylightTime(new Date()), TimeZone.LONG), REQUEST);

		// Get current time
		ctx.setAttribute("systemTime", new Long(System.currentTimeMillis()), REQUEST);
		
		// Calculate DAO usage count
		ctx.setAttribute("daoUsageCount", new Long(org.deltava.dao.DAO.getQueryCount()), REQUEST);

		// Get System properties
		ctx.setAttribute("sys", System.getProperties(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/diagnostics.jsp");
		result.setSuccess(true);
	}
}
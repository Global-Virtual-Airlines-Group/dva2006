// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.io.Serializable;
import java.sql.Connection;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.TextStyle;

import org.apache.log4j.Logger;

import org.deltava.beans.servlet.ServletScoreboard;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;

import org.deltava.taskman.TaskScheduler;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.ipc.PoolWorkerInfo;
import org.gvagroup.jdbc.*;
import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to display diagnostic infomration.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class DiagnosticCommand extends AbstractCommand {
	
	private static final Logger log = Logger.getLogger(DiagnosticCommand.class);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Get system uptime and load average if running on Linux
		if ("Linux".equals(System.getProperty("os.name"))) {
			try {
				GetProcData procdao = new GetProcData();
				int osRunTime = procdao.getUptime();
				ctx.setAttribute("osStart", Instant.now().minusSeconds(osRunTime), REQUEST);
				ctx.setAttribute("osExecTime", Integer.valueOf(osRunTime), REQUEST);
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
		ServletScoreboard.updateActiveThreads();
		ctx.setAttribute("scoreBoard", ServletScoreboard.getScoreboard(), REQUEST);

		// Get ACARS server data
		if (SystemData.getBoolean("acars.enabled") && SharedData.getApplications().contains("ACARS")) {
			// Get the ACARS Connection pool data and save in the request
			ACARSAdminInfo<?> acarsPool = (ACARSAdminInfo<?>) SharedData.get(SharedData.ACARS_POOL);
			ctx.setAttribute("acarsSelectCount", Integer.valueOf(acarsPool.getSelectCount()), REQUEST);
			ctx.setAttribute("acarsPool", IPCUtils.deserialize(acarsPool.getPoolInfo(true)), REQUEST);

			// Get the acars worker info data and save in the request
			PoolWorkerInfo acarsInfo = (PoolWorkerInfo) SharedData.get(SharedData.ACARS_DAEMON);
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
			Serializable rawPool = SharedData.get(SharedData.JDBC_POOL + appName);
			ConnectionPool jdbcPool = (ConnectionPool) IPCUtils.reserialize(rawPool);
			pools.put(appName, jdbcPool);
		}
		
		// Save connection pool data
		if (!pools.isEmpty()) {
			ctx.setAttribute("appNames", appNames, REQUEST);
			ctx.setAttribute("jdbcPools", pools, REQUEST);
		}
		
		// Get Virtual Machine properties
		Runtime rt = Runtime.getRuntime();
		ctx.setAttribute("cpuCount", Integer.valueOf(rt.availableProcessors()), REQUEST);
		ctx.setAttribute("totalMemory", Long.valueOf(rt.totalMemory()), REQUEST);
		ctx.setAttribute("maxMemory", Long.valueOf(rt.maxMemory()), REQUEST);
		ctx.setAttribute("freeMemory", Long.valueOf(rt.freeMemory()), REQUEST);
		ctx.setAttribute("pctMemory", new Double(100 - (Math.round(rt.freeMemory() * 100.0 / rt.totalMemory()))), REQUEST);

		// Get time zone info
		ZoneId tz = ZoneId.systemDefault();
		ctx.setAttribute("timeZone", tz, REQUEST);
		ctx.setAttribute("tzName", tz.getDisplayName(TextStyle.FULL, Locale.US), REQUEST);

		// Get current time
		ctx.setAttribute("systemTime", Instant.now(), REQUEST);
		
		// Calculate DAO usage count
		ctx.setAttribute("daoUsageCount", Long.valueOf(org.deltava.dao.DAO.getQueryCount()), REQUEST);

		// Get System properties
		ctx.setAttribute("sys", System.getProperties(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/diagnostics.jsp");
		result.setSuccess(true);
	}
}
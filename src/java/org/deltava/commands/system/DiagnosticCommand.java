// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2014, 2015, 2016, 2017, 2019, 2020, 2021, 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.util.stream.Collectors;
import java.io.Serializable;
import java.sql.Connection;
import java.time.*;
import java.time.format.TextStyle;

import org.apache.logging.log4j.*;

import org.deltava.beans.acars.CommandStats;
import org.deltava.beans.stats.HTTPCompressionInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.discord.Bot;
import org.deltava.taskman.TaskScheduler;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.pool.*;
import org.gvagroup.acars.ACARSAdminInfo;
import org.gvagroup.ipc.PoolWorkerInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to display diagnostic infomration.
 * @author Luke
 * @version 11.3
 * @since 1.0
 */

public class DiagnosticCommand extends AbstractCommand {
	
	private static final Logger log = LogManager.getLogger(DiagnosticCommand.class);

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
				long osRunTime = procdao.getUptime();
				ctx.setAttribute("osStart", Instant.now().minusSeconds(osRunTime), REQUEST);
				ctx.setAttribute("osExecTime", Long.valueOf(osRunTime), REQUEST);
				ctx.setAttribute("loadAvg", procdao.getLoad(), REQUEST);
				ctx.setAttribute("osMemInfo", procdao.getMemory(), REQUEST);
			} catch (DAOException de) {
				log.atError().withThrowable(de).log(de.getMessage());
			}
		}

		// Get the Task Scheduler data
		TaskScheduler tSched = (TaskScheduler) SystemData.getObject(SystemData.TASK_POOL);
		if (tSched != null)
			ctx.setAttribute("taskInfo", tSched.getTaskInfo(), REQUEST);
		
		// Get ACARS server data
		if (SystemData.getBoolean("acars.enabled") && SharedData.getApplications().contains("ACARS")) {
			// Get the ACARS Connection pool data and save in the request
			ACARSAdminInfo<?> acarsPool = (ACARSAdminInfo<?>) SharedData.get(SharedData.ACARS_POOL);
			ctx.setAttribute("acarsSelectCount", Integer.valueOf(acarsPool.getSelectCount()), REQUEST);
			ctx.setAttribute("acarsPool", IPCUtils.deserialize(acarsPool.getPoolInfo(true)), REQUEST);

			// Get the acars worker info data and save in the request
			PoolWorkerInfo acarsInfo = (PoolWorkerInfo) SharedData.get(SharedData.ACARS_DAEMON);
			ctx.setAttribute("workers", acarsInfo.getWorkers(), REQUEST);
			
			// Get ACARS command stats
			Map<?, ?> cmdStats = (Map<?, ?>) IPCUtils.reserialize(SharedData.get(SharedData.ACARS_CMDSTATS));
			List<CommandStats> cmdStatsInfo = cmdStats.values().stream().map(CommandStats.class::cast).filter(st -> (st.getCount() > 0)).collect(Collectors.toList());
			cmdStatsInfo.sort(null); Collections.reverse(cmdStatsInfo);
			ctx.setAttribute("acarsCmdStats", cmdStatsInfo, REQUEST);

			// Save the ACARS statistics in the request
			try {
				Connection con = ctx.getConnection();
				GetACARSBandwidth bwdao = new GetACARSBandwidth(con);
				ctx.setAttribute("acarsBW", bwdao.getLatest(), REQUEST);
			} catch (DAOException de) {
				log.atError().withThrowable(de).log("Error loading ACARS bandwidth - {}", de.getMessage());
			} finally {
				ctx.release();
			}
		}
		
		// Get JDBC Connection Pool data
		Collection<String> appNames = SharedData.getApplications();
		Map<String, ConnectionPool<?>> pools = new TreeMap<String, ConnectionPool<?>>();
		for (String appName : appNames) {
			Serializable rawDBPool = SharedData.get(SharedData.JDBC_POOL + appName);
			Serializable rawJedisPool = SharedData.get(SharedData.JEDIS_POOL + appName);
			ConnectionPool<?> jdbcPool = (ConnectionPool<?>) IPCUtils.reserialize(rawDBPool);
			ConnectionPool<?> jedisPool = (ConnectionPool<?>) IPCUtils.reserialize(rawJedisPool);
			pools.put("JDBC$" + appName, jdbcPool);
			pools.put("JEDIS$" + appName, jedisPool);
		}
		
		// Save connection pool data
		ctx.setAttribute("pools", pools, REQUEST);
		
		// Get Virtual Machine properties
		Runtime rt = Runtime.getRuntime();
		ctx.setAttribute("cpuCount", Integer.valueOf(rt.availableProcessors()), REQUEST);
		ctx.setAttribute("totalMemory", Long.valueOf(rt.totalMemory()), REQUEST);
		ctx.setAttribute("maxMemory", Long.valueOf(rt.maxMemory()), REQUEST);
		ctx.setAttribute("freeMemory", Long.valueOf(rt.freeMemory()), REQUEST);
		ctx.setAttribute("pctMemory", Double.valueOf(100 - (Math.round(rt.freeMemory() * 100.0 / rt.totalMemory()))), REQUEST);

		// Get time zone info
		ZoneId tz = ZoneId.systemDefault();
		ctx.setAttribute("timeZone", tz, REQUEST);
		ctx.setAttribute("tzName", tz.getDisplayName(TextStyle.FULL, Locale.US), REQUEST);

		// Get current time
		ctx.setAttribute("systemTime", Instant.now(), REQUEST);
		
		// Calculate DAO usage count and redis statistics
		ctx.setAttribute("daoUsageCount", Long.valueOf(org.deltava.dao.DAO.getQueryCount()), REQUEST);
		ctx.setAttribute("httpCompression", HTTPCompressionInfo.getInfo(), REQUEST);
		ctx.setAttribute("vkStatus", JedisUtils.getStatus(), REQUEST);
		
		// Get Discord connection
		ctx.setAttribute("discordOK", Boolean.valueOf(Bot.isInitialized()), REQUEST);

		// Get System properties
		ctx.setAttribute("sys", System.getProperties(), REQUEST);
		
		// Load build data
		ctx.setAttribute("buildDataCore", BuildUtils.getBuildInfo("golgotha_build.properties"), REQUEST);
		ctx.setAttribute("buildDataJSP", BuildUtils.getBuildInfo("golgotha_jsp_build.properties"), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/diagnostics.jsp");
		result.setSuccess(true);
	}
}
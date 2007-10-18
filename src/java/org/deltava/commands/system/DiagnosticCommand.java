// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.io.Serializable;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.servinfo.*;
import org.deltava.beans.servlet.ServletScoreboard;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.jdbc.*;

import org.deltava.taskman.TaskScheduler;
import org.deltava.taglib.googlemap.InsertGoogleAPITag;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;
import org.deltava.util.servinfo.ServInfoLoader;

import org.gvagroup.acars.*;
import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to display diagnostic infomration.
 * @author Luke
 * @version 1.0
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
			ctx.setAttribute("acarsBans", acarsPool.getBanInfo(), REQUEST);

			// Get the acars worker info data and save in the request
			ACARSWorkerInfo acarsInfo = (ACARSWorkerInfo) SharedData.get(SharedData.ACARS_DAEMON);
			ctx.setAttribute("workers", acarsInfo.getWorkers(), REQUEST);

			// Save the ACARS statistics in the request
			try {
				Connection con = ctx.getConnection();
				GetACARSLog addao = new GetACARSLog(con);
				ctx.setAttribute("acarsCmdStats", addao.getCommandStats(), REQUEST);
			} catch (DAOException de) {
				log.error("Error loading ACARS command statistics - " + de.getMessage(), de);
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
		
		// Get ServInfo statistics
		List networks = (List) SystemData.getObject("online.networks");
		if (!CollectionUtils.isEmpty(networks)) {
			Collection<NetworkStatus> netInfo = new TreeSet<NetworkStatus>();
			for (Iterator i = networks.iterator(); i.hasNext(); ) {
				String networkName = (String) i.next();
				NetworkStatus status = GetServInfo.getCachedStatus(networkName);
				if (status != null)
					netInfo.add(status);
			}
			
			ctx.setAttribute("servInfoStatus", netInfo, REQUEST);
			
			// Dump the Servinfo thread map after cleaning up
			ServInfoLoader.isLoading("?");
			Map loaders = ServInfoLoader.getLoaders();
			ctx.setAttribute("servInfoLoaders", loaders, REQUEST);
			ctx.setAttribute("servInfoLoaderNets", loaders.keySet(), REQUEST);
		}
		
		// Get DAO cache properties
		Collection<CachingDAO> daoCaches = new ArrayList<CachingDAO>();
		daoCaches.add(new GetCoolerChannels(null));
		daoCaches.add(new GetExamProfiles(null));
		daoCaches.add(new GetMessageTemplate(null));
		daoCaches.add(new GetNavData(null));
		daoCaches.add(new GetNavRoute(null));
		daoCaches.add(new GetStatistics(null));
		daoCaches.add(new GetSystemData(null));
		daoCaches.add(new GetUserData(null));
		daoCaches.add(new GetPilot(null));
		daoCaches.add(new GetACARSLog(null));
		ctx.setAttribute("daoCaches", daoCaches, REQUEST);
		
		// Get Virtual Machine properties
		Runtime rt = Runtime.getRuntime();
		ctx.setAttribute("cpuCount", new Integer(rt.availableProcessors()), REQUEST);
		ctx.setAttribute("totalMemory", new Long(rt.totalMemory()), REQUEST);
		ctx.setAttribute("maxMemory", new Long(rt.maxMemory()), REQUEST);
		ctx.setAttribute("freeMemory", new Long(rt.freeMemory()), REQUEST);
		ctx.setAttribute("pctMemory", new Double(100 - (Math.round(rt.freeMemory() * 100.0 / rt.totalMemory()))), REQUEST);

		// Get time zone info
		TimeZone tz = TimeZone.getDefault();
		ctx.setAttribute("timeZone", tz, REQUEST);
		ctx.setAttribute("tzName", tz.getDisplayName(tz.inDaylightTime(new Date()), TimeZone.LONG), REQUEST);

		// Get Servlet context properties
		Date startDate = (Date) _ctx.getAttribute("startedOn");
		ctx.setAttribute("serverInfo", _ctx.getServerInfo(), REQUEST);
		ctx.setAttribute("serverStart", startDate, REQUEST);
		ctx.setAttribute("servletContextName", _ctx.getServletContextName(), REQUEST);
		ctx.setAttribute("majorServletAPI", new Integer(_ctx.getMajorVersion()), REQUEST);
		ctx.setAttribute("minorServletAPI", new Integer(_ctx.getMinorVersion()), REQUEST);
		
		// Calculate DAO usage count
		ctx.setAttribute("execTime", new Long((System.currentTimeMillis() - startDate.getTime()) / 1000), REQUEST);
		ctx.setAttribute("daoUsageCount", new Long(org.deltava.dao.DAO.getQueryCount()), REQUEST);

		// Get the Google Maps API usage count
		ctx.setAttribute("mapsAPIUsage", _ctx.getAttribute(InsertGoogleAPITag.USAGE_ATTR_NAME), REQUEST);

		// Get System properties
		ctx.setAttribute("sys", System.getProperties(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/diagnostics.jsp");
		result.setSuccess(true);
	}
}
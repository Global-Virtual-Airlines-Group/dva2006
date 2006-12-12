// Copyright 2005, 2006 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.servinfo.*;
import org.deltava.beans.servlet.ServletScoreboard;

import org.deltava.commands.*;

import org.deltava.dao.DAO;
import org.deltava.dao.file.GetServInfo;

import org.deltava.jdbc.ConnectionPool;
import org.deltava.taskman.TaskScheduler;

import org.deltava.taglib.googlemap.InsertGoogleAPITag;

import org.deltava.util.CollectionUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display diagnostic infomration.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class DiagnosticCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Connection Pool data
		ConnectionPool cPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
		ctx.setAttribute("jdbcPoolInfo", (cPool == null) ? null : cPool.getPoolInfo(), REQUEST);

		// Get the Task Scheduler data
		TaskScheduler tSched = (TaskScheduler) SystemData.getObject(SystemData.TASK_POOL);
		if (tSched != null)
			ctx.setAttribute("taskInfo", tSched.getTaskInfo(), REQUEST);
		
		// Get servlet scoreboard
		ctx.setAttribute("scoreBoard", ServletScoreboard.getScoreboard(), REQUEST);

		// Get ACARS server data
		if (SystemData.getBoolean("acars.enabled")) {
			// Get the ACARS Connection pool data and save in the request
			ACARSAdminInfo acarsPool = (ACARSAdminInfo) SystemData.getObject(SystemData.ACARS_POOL);
			ctx.setAttribute("acarsPool", acarsPool.getPoolInfo(), REQUEST);
			ctx.setAttribute("acarsBans", acarsPool.getBanInfo(), REQUEST);

			// Get the acars worker info data and save in the request
			ACARSWorkerInfo acarsInfo = (ACARSWorkerInfo) SystemData.getObject(SystemData.ACARS_DAEMON);
			ctx.setAttribute("workers", acarsInfo.getWorkers(), REQUEST);

			// Save the ACARS statistics in the request
			ctx.setAttribute("acarsStats", ServerStats.getInstance(), REQUEST);
			ctx.setAttribute("acarsCmdStats", CommandStats.getInfo(), REQUEST);
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
		}
		
		// Run the GC
		System.gc();

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
		ctx.setAttribute("daoUsageCount", new Long(DAO.getQueryCount()), REQUEST);

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
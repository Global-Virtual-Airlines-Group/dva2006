// Copyright 2007, 2008, 2009, 2012, 2013, 2015, 2016, 2018, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to import Terminal Routes in PSS format.
 * @author Luke
 * @version 9.0
 * @since 2.0
 */

public class TerminalRouteImportCommand extends NavDataImportCommand {

	private static final Logger log = Logger.getLogger(TerminalRouteImportCommand.class);
	
	private static final String[] UPLOAD_NAMES = {"psssid.dat", "pssstar.dat"};

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Command result
		CommandResult result = ctx.getResult();
		
		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Navigation Data");
		
		// Load the data
		CycleInfo inf = getCurrrentCycle(ctx);
		ctx.setAttribute("currentNavCycle", inf, REQUEST);

		// If we're doing a GET, then redirect to the JSP
		FileUpload navData = ctx.getFile("navData");
		if (navData == null) {
			result.setURL("/jsp/navdata/tRouteImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Strip out .gz extension
		String name = navData.getName();
		if (name.endsWith(".gz") || name.endsWith(".bz2"))
			name = name.substring(0, name.lastIndexOf('.'));
		
		// Get the navaid type
		int routeType = StringUtils.arrayIndexOf(UPLOAD_NAMES, name);
		if (routeType == -1)
			throw notFoundException("Unknown Data File - " + navData.getName());

		List<String> errors = new ArrayList<String>();
		Map<String, Long> timings = new LinkedHashMap<String, Long>();
		TerminalRoute.Type rt = TerminalRoute.Type.values()[routeType];
		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		int entryCount = 0; LineNumberReader br = null;
		try (InputStream is = navData.getInputStream(); LineNumberReader br2 = new LineNumberReader(new InputStreamReader(is))) {
			br = br2;
			
			// Iterate through the file
			TerminalRoute tr = null; 
			Collection<String> IDs = new HashSet<String>(); Collection<String> trIDs = new HashSet<String>();
			Collection<TerminalRoute> results = new ArrayList<TerminalRoute>();
			TaskTimer tt = new TaskTimer();
			String txtData = br.readLine(); String lastAirport = null; int seq = 0;
			while (txtData != null) {
				if (txtData.startsWith("[")) {
					IDs.clear();
					String id = txtData.substring(1, txtData.indexOf(']')).replace(" ", "");
					List<String> idParts = StringUtils.split(id, "/");
					Airport a = SystemData.getAirport(idParts.get(0));
					if (a != null) {
						if (!a.getICAO().equals(lastAirport)) {
							lastAirport = a.getICAO();
							seq = 0;
						} else
							seq++;
							
						tr = new TerminalRoute(a, idParts.get(1), rt);
						tr.setRunway(idParts.get(2));
						tr.setSequence(seq);
						tr.setCanPurge(true);
						if (idParts.size() > 3)
							tr.setTransition(idParts.get(3));
						
						// Check that we don't have a duplicate
						String routeID = tr.toString(); 
						if (trIDs.add(routeID))
							results.add(tr);
						else {
							log.warn("Duplicate " + tr.getType().name() + " - " + routeID);
							tr = null;
						}
					} else
						tr = null;
				} else if ((tr != null) && (txtData.length() > 5)) {
					List<String> wptParts = StringUtils.split(txtData.replace(" ", ""), ",");
					String wpt = wptParts.get(3);
					if (!StringUtils.isEmpty(wpt) && !IDs.contains(wpt)) {
						IDs.add(wpt);
						NavigationDataBean nd = NavigationDataBean.create(Navaid.INT, StringUtils.parse(wptParts.get(4), 0.0),
								StringUtils.parse(wptParts.get(5), 0.0));
						nd.setCode(wpt);
						tr.addWaypoint(nd);
						
						// Use code #1 if wptParts.get(1) == ALL or Runway name
						if (tr.getTransition() == null) {
							String tx = wptParts.get(1);
							if ("ALL".equals(tx) || tr.getRunway().equals(tx))
								tr.setTransition(wpt);
							else
								tr.setTransition(tx);
						}
					}
				}
				
				txtData = br.readLine();
			}
			
			timings.put("Load", Long.valueOf(tt.stop()));
			
			// Get a connection
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the write DAO and purge the table
			SetNavData dao = new SetNavData(con);
			if (doPurge) {
				tt.start();
				int purgeCount = dao.purgeTerminalRoutes(rt);
				timings.put("Purge", Long.valueOf(tt.stop()));
				ctx.setAttribute("purgeCount", Integer.valueOf(purgeCount), REQUEST);	
			}

			// Write the entries
			tt.start();
			for (TerminalRoute trt : results) {
				if (trt.getTransition() == null) {
					errors.add(trt.getName() + " (" + trt.getICAO() + ") has no transition");
					continue;
				}
				
				dao.writeRoute(trt);
				entryCount++;
			}
			
			// Update the waypoint types
			timings.put("Write", Long.valueOf(tt.stop()));
			dao.setQueryTimeout(75);
			int regionCount = dao.updateTRWaypoints();
			timings.put("UpdateRegions", Long.valueOf(tt.stop()));
			ctx.setAttribute("regionCount", Integer.valueOf(regionCount), REQUEST);
			
			// Commit
			ctx.commitTX();
		} catch (Exception e) {
			if (br != null)
				log.error("Import error at line " + br.getLineNumber());
			
			ctx.rollbackTX();
			throw new CommandException(e);
		} finally { 
			ctx.release();
		}
		
		// Purge the caches
		CacheManager.invalidate("NavSIDSTAR");
		CacheManager.invalidate("NavRoute");
		
		// Set status attributes
		ctx.setAttribute("timings", timings, REQUEST);
		ctx.setAttribute("entryCount", Integer.valueOf(entryCount), REQUEST);
		ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
		ctx.setAttribute("doPurge", Boolean.valueOf(doPurge), REQUEST);
		ctx.setAttribute("terminalRoute", Boolean.TRUE, REQUEST);
		
		// Save error messages
		if (!errors.isEmpty())
			ctx.setAttribute("errors", errors, REQUEST);
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/navdata/navDataUpdate.jsp");
		result.setSuccess(true);
	}
}
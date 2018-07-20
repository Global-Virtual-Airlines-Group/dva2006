// Copyright 2007, 2008, 2009, 2012, 2013, 2015, 2016, 2018 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;
import org.deltava.util.cache.CacheManager;

/**
 * A Web Site Command to import airway data in PSS format.
 * @author Luke
 * @version 8.3
 * @since 2.0
 */

public class AirwayImportCommand extends NavDataImportCommand {

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
			result.setURL("/jsp/navdata/airwayImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		List<String> errors = new ArrayList<String>();
		Collection<Airway> results = new ArrayList<Airway>();
		int entryCount = 0; CycleInfo newCycle = null;
		Map<String, Long> timings = new LinkedHashMap<String, Long>();
		TaskTimer tt = new TaskTimer();
		try (InputStream is = navData.getInputStream()) {
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
			
			// Iterate through the file
			Airway a = null; int lastSeq = -1; String lastCode = "";
			String txtData = br.readLine(); 
			while (txtData != null) {
				boolean isComment = txtData.startsWith(";");
				if (isComment && (newCycle == null)) {
					int pos = txtData.indexOf("AIRAC Cycle : ");
					if (pos != -1) {
						newCycle = new CycleInfo(txtData.substring(pos+14, pos+18));
						if ((inf != null) && (newCycle.compareTo(inf) == -1))
							throw new IllegalStateException("Navigation Data Cycle " + newCycle + " is older than loaded cycle " + inf);
					}
				} else if (!isComment && (txtData.length() > 5)) {
					txtData = txtData.replace(" ", "");
					List<String> codes = StringUtils.split(txtData, ",");
					String code = codes.get(1);
					if ((!code.equals(lastCode)) || ("0".equals(codes.get(6)))) {
						if (!code.equals(lastCode))
							lastSeq = -1;
						
						lastCode = code;
						char type = Character.toUpperCase(codes.get(9).charAt(0));
						a = new Airway(code, ++lastSeq);
						a.setHighLevel((type == 'H') || (type == 'B'));
						a.setLowLevel((type == 'L') || (type == 'B'));
						results.add(a);
					}
					
					// Add a waypoint
					try {
						NavigationDataBean nd = NavigationDataBean.create(Navaid.INT, Double.parseDouble(codes.get(4)), Double.parseDouble(codes.get(5)));
						nd.setCode(codes.get(3));
						if (a != null)
							a.addWaypoint(nd);
					} catch (NumberFormatException nfe) {
						errors.add("Error at line " + br.getLineNumber() + ": " + nfe.getMessage());
						errors.add(txtData);
					}
				}
				
				txtData = br.readLine();
			}
		} catch (IOException | IllegalStateException ie) {
			throw new CommandException(ie);
		} finally {
			timings.put("Load", Long.valueOf(tt.stop()));
		}
			
		// Get a connection
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the write DAO and purge the table
			SetNavData dao = new SetNavData(con);
			boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
			if (doPurge) {
				tt.start();
				int purgeCount = dao.purgeAirways();
				timings.put("Purge", Long.valueOf(tt.stop()));
				ctx.setAttribute("purgeCount", Integer.valueOf(purgeCount), REQUEST);
				ctx.setAttribute("doPurge", Boolean.TRUE, REQUEST);
			}
			
			// Write the airways
			tt.start();
			for (Airway aw : results) {
				dao.write(aw);
				entryCount++;
			}
			
			// Update the waypoint types and commit
			timings.put("Store", Long.valueOf(tt.stop()));
			dao.setQueryTimeout(75);
			tt.start();
			int regionCount = dao.updateAirwayWaypoints();
			timings.put("UpdateRegion", Long.valueOf(tt.stop()));
			ctx.setAttribute("regionCount", Integer.valueOf(regionCount), REQUEST);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Purge the caches
		CacheManager.invalidate("NavAirway");
		CacheManager.invalidate("NavRoute");
		
		// Set status attributes
		ctx.setAttribute("timings", timings, REQUEST);
		ctx.setAttribute("entryCount", Integer.valueOf(entryCount), REQUEST);
		ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
		ctx.setAttribute("airway", Boolean.TRUE, REQUEST);
		ctx.setAttribute("navCycleID", newCycle, REQUEST);
		
		// Save error messages
		if (!errors.isEmpty())
			ctx.setAttribute("errors", errors, REQUEST);
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/navdata/navDataUpdate.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to import Terminal Routes in PSS format.
 * @author Luke
 * @version 2.1
 * @since 2.0
 */

public class TerminalRouteImportCommand extends AbstractCommand {
	
	private static final String[] UPLOAD_NAMES = {"psssid.dat", "pssstar.dat"};

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the Command result
		CommandResult result = ctx.getResult();
		
		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Navigation Data");

		// If we're doing a GET, then redirect to the JSP
		FileUpload navData = ctx.getFile("navData");
		if (navData == null) {
			result.setURL("/jsp/navdata/tRouteImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Strip out .gz extension
		String name = navData.getName();
		if (name.endsWith(".gz"))
			name = name.substring(0, name.lastIndexOf('.'));
		
		// Get the navaid type
		int routeType = StringUtils.arrayIndexOf(UPLOAD_NAMES, name);
		if (routeType == -1)
			throw notFoundException("Unknown Data File - " + navData.getName());

		List<String> errors = new ArrayList<String>();
		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		int entryCount = 0;
		try {
			// Get the file
			InputStream is = navData.getInputStream();
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
			
			// Iterate through the file
			TerminalRoute tr = null;
			Collection<String> IDs = new HashSet<String>();
			Collection<TerminalRoute> results = new ArrayList<TerminalRoute>();
			while (br.ready()) {
				String txtData = br.readLine();
				if (txtData.startsWith("[")) {
					IDs.clear();
					String id = txtData.substring(1, txtData.indexOf(']')).replace(" ", "");
					List<String> idParts = StringUtils.split(id, "/");
					Airport a = SystemData.getAirport(idParts.get(0));
					if (a != null) {
						tr = new TerminalRoute(a, idParts.get(1), routeType);
						tr.setRunway(idParts.get(2));
						tr.setCanPurge(true);
						if (routeType == TerminalRoute.SID)
							tr.setTransition(idParts.get(3));
						
						results.add(tr);
					} else
						tr = null;
				} else if ((tr != null) && (txtData.length() > 5)) {
					List<String> wptParts = StringUtils.split(txtData.replace(" ", ""), ",");
					String wpt = wptParts.get(3);
					if (!StringUtils.isEmpty(wpt) && !IDs.contains(wpt)) {
						IDs.add(wpt);
						NavigationDataBean nd = NavigationDataBean.create(NavigationDataBean.INT, StringUtils.parse(wptParts.get(4), 0.0),
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
			}
			
			// Close the stream
			is.close();
			
			// Get a connection
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the write DAO and purge the table
			SetNavData dao = new SetNavData(con);
			if (doPurge)
				dao.purge("SID_STAR", true);

			// Write the entries
			for (Iterator<TerminalRoute> i = results.iterator(); i.hasNext(); ) {
				tr = i.next();
				if (tr.getTransition() != null) {
					dao.writeRoute(tr);
					entryCount++;
				} else
					errors.add(tr.getName() + " (" + tr.getICAO() + ") has no transition");
			}
			
			// Update the waypoint types
			dao.updateTRWaypoints();
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} catch (IOException ie) {
			ctx.rollbackTX();
			throw new CommandException(ie);
		} finally { 
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("entryCount", new Integer(entryCount), REQUEST);
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
// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.navdata.TerminalRoute;
import org.deltava.beans.schedule.Airport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to import Terminal Routes in PSS format.
 * @author Luke
 * @version 2.0
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
			result.setURL("/jsp/schedule/tRouteImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the navaid type
		int routeType = StringUtils.arrayIndexOf(UPLOAD_NAMES, navData.getName().toLowerCase());
		if (routeType == -1)
			throw notFoundException("Unknown Data File - " + navData.getName());

		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		int entryCount = 0;
		try {
			// Get the file
			InputStream is = new ByteArrayInputStream(navData.getBuffer());
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));

			// Iterate through the file
			TerminalRoute tr = null;
			Collection<TerminalRoute> results = new ArrayList<TerminalRoute>();
			while (br.ready()) {
				String txtData = br.readLine();
				if (txtData.startsWith("[")) {
					String id = txtData.substring(1, txtData.indexOf(']')).replace(" ", "");
					List<String> idParts = StringUtils.split(id, "/");
					Airport a = SystemData.getAirport(idParts.get(0));
					if (a != null) {
						tr = new TerminalRoute(a, idParts.get(1), routeType);
						tr.setRunway(idParts.get(2));
						results.add(tr);
					}
				} else if ((tr != null) && (txtData.length() > 5)) {
					List<String> wptParts = StringUtils.split(txtData.replace(" ", ""), ",");
					tr.addWaypoint(wptParts.get(3));
					if (tr.getTransition() == null)
						tr.setTransition(wptParts.get(3));
				}
			}
			
			// Close the stream
			is.close();
			
			// Get the connection
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Get the write DAO and purge the table
			SetNavData dao = new SetNavData(con);
			if (doPurge)
				dao.purge("SID_STAR");

			// Write the entries
			for (Iterator<TerminalRoute> i = results.iterator(); i.hasNext(); ) {
				tr = i.next();
				dao.writeRoute(tr);
				entryCount++;
			}
			
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
		
		// Forward to the JSP
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/schedule/navDataUpdate.jsp");
		result.setSuccess(true);
	}
}
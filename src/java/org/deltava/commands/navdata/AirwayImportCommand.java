// Copyright 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.navdata.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to import airway data in PSS format.
 * @author Luke
 * @version 2.6
 * @since 2.0
 */

public class AirwayImportCommand extends AbstractCommand {

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
			result.setURL("/jsp/navdata/airwayImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		List<String> errors = new ArrayList<String>();
		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		int entryCount = 0;
		try {
			// Get the file
			InputStream is = navData.getInputStream();
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
			
			// Iterate through the file
			Airway a = null; int lastSeq = -1; String lastCode = "";
			Collection<Airway> results = new ArrayList<Airway>();
			while (br.ready()) {
				String txtData = br.readLine().replace(" ", "");
				if ((!txtData.startsWith(";")) && (txtData.length() > 5)) {
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
						NavigationDataBean nd = NavigationDataBean.create(NavigationDataBean.INT, Double.parseDouble(codes.get(4)),
								Double.parseDouble(codes.get(5)));
						nd.setCode(codes.get(3));
						a.addWaypoint(nd);
					} catch (NumberFormatException nfe) {
						errors.add("Error at line " + br.getLineNumber() + ": " + nfe.getMessage());
						errors.add(txtData);
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
			if (doPurge) {
				int purgeCount = dao.purgeAirways();
				ctx.setAttribute("purgeCount", new Integer(purgeCount), REQUEST);
			}
			
			// Write the airways
			for (Iterator<Airway> i = results.iterator(); i.hasNext(); ) {
				a = i.next();
				dao.write(a);
				entryCount++;
			}
			
			// Update the waypoint types
			int regionCount = dao.updateAirwayWaypoints();
			ctx.setAttribute("regionCount", new Integer(regionCount), REQUEST);
			
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
		
		// Purge the cache
		new GetNavRoute(null).clear();
		
		// Set status attributes
		ctx.setAttribute("entryCount", Integer.valueOf(entryCount), REQUEST);
		ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
		ctx.setAttribute("doPurge", Boolean.valueOf(doPurge), REQUEST);
		ctx.setAttribute("airway", Boolean.TRUE, REQUEST);
		
		// Save error messages
		if (!errors.isEmpty())
			ctx.setAttribute("errors", errors, REQUEST);
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/navdata/navDataUpdate.jsp");
		result.setSuccess(true);
	}
}
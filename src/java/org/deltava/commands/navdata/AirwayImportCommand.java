// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.navdata;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.navdata.Airway;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to import airway data in PSS format.
 * @author Luke
 * @version 2.0
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
			result.setURL("/jsp/schedule/airwayImport.jsp");
			result.setSuccess(true);
			return;
		}
		
		boolean doPurge = Boolean.valueOf(ctx.getParameter("doPurge")).booleanValue();
		int entryCount = 0;
		try {
			// Get the file
			InputStream is = new ByteArrayInputStream(navData.getBuffer());
			LineNumberReader br = new LineNumberReader(new InputStreamReader(is));
			
			// Iterate through the file
			Map<String, Airway> results = new TreeMap<String, Airway>();
			while (br.ready()) {
				String txtData = br.readLine().replace(" ", "");
				if ((!txtData.startsWith(";")) && (txtData.length() > 5)) {
					List<String> codes = StringUtils.split(txtData, ",");
					String code = codes.get(1);
					Airway a = results.get(code);
					if (a == null) {
						a = new Airway(code);
						results.put(code, a);
					}
					
					// Add a waypoint
					a.addWaypoint(codes.get(3));
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
				dao.purge("AIRWAYS");
			
			// Write the airways
			for (Iterator<Airway> i = results.values().iterator(); i.hasNext(); ) {
				Airway a = i.next();
				dao.write(a);
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
		ctx.setAttribute("airway", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		result.setType(CommandResult.REQREDIRECT);
		result.setURL("/jsp/schedule/navDataUpdate.jsp");
		result.setSuccess(true);
	}
}
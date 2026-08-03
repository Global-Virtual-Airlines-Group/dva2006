// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;

import org.deltava.dao.*;
import org.deltava.dao.file.GetJSONSchedule;

/**
 * A Web Site Command to merge Aviation Stack data.   
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class AVStackMergeCommand extends AbstractCommand {
	
	/**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check for effective date
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/avStackMerge.jsp");
		FileUpload fu = ctx.getFile("jsonData", 4_096_000);
		if (fu == null) {
			result.setSuccess(true);
			return;
		}
		
		// Load the file
		try {
			Connection con = ctx.getConnection();
			GetAirline adao = new GetAirline(con);
			GetAircraft acdao = new GetAircraft(con);

			// Get the DAO
			GetJSONSchedule jsdao = new GetJSONSchedule(ScheduleSource.AVSTACK, fu.getInputStream());
			jsdao.setAircraft(acdao.getAircraftTypes());
			jsdao.setAirlines(adao.getActive().values());
			
			// Load the data
			Collection<RawScheduleEntry> newEntries = jsdao.process();
			ctx.setAttribute("status", jsdao.getStatus(), REQUEST);
			
			// Get the next line number
			GetRawScheduleInfo rsdao = new GetRawScheduleInfo(con);
			int ln = rsdao.getNextLine(ScheduleSource.AVSTACK);
			
			// Write new entries
			ctx.startTX();
			SetSchedule swdao = new SetSchedule(con);
			for (RawScheduleEntry rse : newEntries) {
				rse.setLineNumber(ln++);
				swdao.writeRaw(rse, false);
			}
				
			ctx.commitTX();
			
			// Write status attributes
			ctx.setAttribute("isMerge", Boolean.TRUE, REQUEST);
			ctx.setAttribute("entryCount", Integer.valueOf(newEntries.size()), REQUEST);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}
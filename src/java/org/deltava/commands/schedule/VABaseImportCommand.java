// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.Collection;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.file.GetVABaseSchedule;

import org.deltava.security.command.ScheduleAccessControl;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to upload a VABase-format flight schedule.
 * @author Luke
 * @version 8.0
 * @since 8.0
 */

public class VABaseImportCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Flight Schedule data");
		
		// If we are not uploading a CSV file, then redirect to the JSP
		FileUpload csvData = ctx.getFile("csvData");
		if (csvData == null) {
			CommandResult result = ctx.getResult();
			result.setURL("/jsp/schedule/vaBaseImport.jsp");
			result.setSuccess(true);
			return;
		}

		boolean isTailCodes = Boolean.valueOf(ctx.getParameter("isTailCodes")).booleanValue();
		try {
			Connection con = ctx.getConnection();
			GetVABaseSchedule idao = new GetVABaseSchedule(csvData.getInputStream());
			
			// Load aircraft codes
			GetAircraft acdao = new GetAircraft(con);
			idao.setAircraft(acdao.getAircraftTypes());
			idao.setAirlines(SystemData.getAirlines().values());
			
			SetSchedule swdao = new SetSchedule(con);
			ctx.startTX();
			
			if (isTailCodes) {
				Collection<TailCode> codes = idao.getTailCodes();
				for (TailCode tc : codes)
					swdao.write(tc);
			} else {
				Collection<ScheduleEntry> entries = idao.process();
				swdao.purgeRaw();
				for (ScheduleEntry se : entries)
					swdao.writeRaw((RawScheduleEntry) se);
			}
			
			// Check if anything has been imported
			GetRawSchedule rsdao = new GetRawSchedule(con);
			ctx.setAttribute("hasRawSchedule", Boolean.valueOf(rsdao.hasEntries(Instant.now())), REQUEST);
			
			// Set status attributes
			ctx.setAttribute("msgs", idao.getErrorMessages(), REQUEST);
			ctx.setAttribute("eqTypes", idao.getInvalidEQ(), REQUEST);
			ctx.setAttribute("airlines", idao.getInvalidAirlines(), REQUEST);
			ctx.setAttribute("airports", idao.getInvalidAirports(), REQUEST);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attributes
		ctx.setAttribute("isTailCode", Boolean.valueOf(isTailCodes), REQUEST);
		ctx.setAttribute("isImport", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/vaBaseStatus.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}
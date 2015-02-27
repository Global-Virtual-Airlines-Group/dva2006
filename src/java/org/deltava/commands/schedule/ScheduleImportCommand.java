// Copyright 2005, 2006, 2007, 2010, 2012, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.dao.file.ScheduleLoadDAO;
import org.deltava.dao.file.innovata.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to import Flight Schedule data.
 * @author Luke
 * @version 6.0
 * @since 1.0
 */

public class ScheduleImportCommand extends AbstractCommand {

	protected static final Logger log = Logger.getLogger(ScheduleImportCommand.class);

	private static final int NATIVE = 0;
	protected static final int INNOVATA = 1;
	protected static final String[] SCHED_TYPES = { "Native", "Innovata LLC" };

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Save schedule types
		ctx.setAttribute("schedTypes", ComboUtils.fromArray(SCHED_TYPES), REQUEST);

		// Get the command results
		CommandResult result = ctx.getResult();

		// Check our access level
		ScheduleAccessControl access = new ScheduleAccessControl(ctx);
		access.validate();
		if (!access.getCanImport())
			throw securityException("Cannot import Flight Schedule data");

		// If we are not uploading a CSV file, then redirect to the JSP
		FileUpload csvData = ctx.getFile("csvData");
		if (csvData == null) {
			result.setURL("/jsp/schedule/flightImport.jsp");
			result.setSuccess(true);
			return;
		}

		// Get the schedule DAO and do the initial load
		ScheduleLoadDAO dao = null;
		Collection<ScheduleEntry> entries = null;
		try {
			int scheduleType = StringUtils.arrayIndexOf(SCHED_TYPES, ctx.getParameter("schedType"));

			switch (scheduleType) {
				case NATIVE:
					dao = new org.deltava.dao.file.GetSchedule(csvData.getInputStream());
					break;

				case INNOVATA:
					GetFullSchedule ivdao = new GetFullSchedule(csvData.getInputStream());
					ivdao.load();
					dao = ivdao;
					break;

				default:
					throw new CommandException("Unknown Schedule type - " + ctx.getParameter("schedType"));
			}

			// Initialize the DAO
			GetAircraft acdao = new GetAircraft(ctx.getConnection());
			dao.setAircraft(acdao.getAircraftTypes());
			entries = dao.process();
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save the data in the session
		ctx.setAttribute("entries", entries, SESSION);
		ctx.setAttribute("schedType", ctx.getParameter("schedType"), SESSION);
		ctx.setAttribute("errors", dao.getErrorMessages(), SESSION);

		// Forward to the JSP
		result.setURL("/jsp/schedule/flightSave.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}
// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.FileUpload;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;

import org.deltava.dao.GetAircraft;
import org.deltava.dao.DAOException;
import org.deltava.dao.file.GetPartnerAirlines;
import org.deltava.dao.file.ScheduleLoadDAO;
import org.deltava.dao.file.innovata.*;

import org.deltava.security.command.ScheduleAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to import Flight Schedule data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ScheduleImportCommand extends AbstractCommand {

	protected static final Logger log = Logger.getLogger(ScheduleImportCommand.class);

	private static final int NATIVE = 0;
	protected static final int INNOVATA = 1;
	protected static final String[] SCHED_TYPES = { "Native", "Innovata LLC" };

	private static Collection<PartnerAirline> _codeShareInfo;

	/**
	 * Initializes this command.
	 * @param cmdName the name of the command
	 * @throws CommandException if the command name is null
	 * @throws IllegalStateException if the command has already been initialized
	 */
	public void init(String id, String cmdName) throws CommandException {
		super.init(id, cmdName);

		// Load the airlines
		if (_codeShareInfo == null) {
			try {
				GetPartnerAirlines pdao = new GetPartnerAirlines(ConfigLoader.getStream("/etc/codeshares.txt"));
				_codeShareInfo = pdao.getPartners();
				_codeShareInfo.add(PartnerAirline.IGNORE);
			} catch (Exception e) {
				log.warn("Cannot load Partner Airline data - " + e.getMessage());
			}
		}
	}
	
	/**
	 * Loads partner airlines applicable to a particular import file.
	 * @param fileName the import file name
	 * @return a Collection of PartnerAirline beans
	 */
	protected Collection<PartnerAirline> getPartners(String fileName) {
		if (_codeShareInfo == null)
			return Collections.emptySet();
		
		// Parse the collection
		Collection<PartnerAirline> results = new ArrayList<PartnerAirline>();
		for (Iterator<PartnerAirline> i = _codeShareInfo.iterator(); i.hasNext(); ) {
			PartnerAirline pa = i.next();
			if (pa.includesFile(fileName))
				results.add(pa);
		}
		
		return results;
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
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
					GetSchedule ivdao = new GetSchedule(csvData.getInputStream());
					ivdao.load();
					dao = ivdao;
					break;

				default:
					throw new CommandException("Unknown Schedule type - " + ctx.getParameter("schedType"));
			}

			// Initialize the DAO
			Connection con = ctx.getConnection();
			GetAircraft acdao = new GetAircraft(con);
			dao.setAircraft(acdao.getAircraftTypes());
			dao.setPartners(getPartners(csvData.getName()));

			// Load the data
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
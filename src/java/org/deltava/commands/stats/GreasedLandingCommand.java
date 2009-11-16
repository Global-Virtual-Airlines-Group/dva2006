// Copyright 2005, 2006, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.flight.FlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Site Command to display the smoothest landings.
 * @author Luke
 * @version 2.7
 * @since 1.0
 */

public class GreasedLandingCommand extends AbstractViewCommand {

	private static final List<?> DATE_FILTER = ComboUtils.fromArray(new String[] { "All Landings", "30 Days", "60 Days",
			"90 Days" }, new String[] { "0", "30", "60", "90" });

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Load the view context
		ViewContext vc = initView(ctx, 25);

		// Check equipment type and how many days back to search
		String eqType = ctx.getParameter("eqType");
		int daysBack = StringUtils.parse(ctx.getParameter("days"), 30);
		try {
			Connection con = ctx.getConnection();

			// Get the DAO
			GetFlightReportRecognition dao = new GetFlightReportRecognition(con);
			
			// Save equipment choices
			List<Object> eqTypes = new ArrayList<Object>();
			eqTypes.add(ComboUtils.fromString("All Aircraft", ""));
			eqTypes.add(ComboUtils.fromString("Staff Members", "staff"));
			eqTypes.addAll(dao.getACARSEquipmentTypes(25));
			ctx.setAttribute("eqTypes", eqTypes, REQUEST);
			
			// Get the results
			dao.setQueryMax(vc.getCount());
			dao.setDayFilter(daysBack);
			if (StringUtils.isEmpty(eqType))
				vc.setResults(dao.getGreasedLandings());
			else if ("staff".equals(eqType))
				vc.setResults(dao.getStaffReports());
			else
				vc.setResults(dao.getGreasedLandings(eqType));
			
			// Get the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<?> i = vc.getResults().iterator(); i.hasNext(); ) {
				FlightReport fr = (FlightReport) i.next();
				IDs.add(new Integer(fr.getDatabaseID(FlightReport.DBID_PILOT)));
			}
			
			// Load the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save combobox choices
		ctx.setAttribute("dateFilter", DATE_FILTER, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/roster/greasedLandings.jsp");
		result.setSuccess(true);
	}
}
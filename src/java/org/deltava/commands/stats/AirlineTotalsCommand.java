// Copyright 2005, 2006, 2007, 2009, 2010, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.stats.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A web site command to display Airline Total statistics.
 * @author Luke
 * @version 6.3
 * @since 1.0
 */

public class AirlineTotalsCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		AirlineTotals totals = null;
		Collection<TableInfo> tableStatus = new ArrayList<TableInfo>();
		try {
			Connection con = ctx.getConnection();

			// Get the Statistics DAO
			GetStatistics dao = new GetStatistics(con);
			totals = dao.getAirlineTotals();

			// Get the Table Status for our database and common
			GetTableStatus tsdao = new GetTableStatus(con);
			tableStatus.addAll(tsdao.getStatus("common"));
			tableStatus.addAll(tsdao.getStatus("exams"));
			tableStatus.addAll(tsdao.getStatus("events"));
			tableStatus.addAll(tsdao.getStatus("acars"));
			tableStatus.addAll(tsdao.getStatus("xacars"));
			tableStatus.addAll(tsdao.getStatus("geoip"));
			tableStatus.addAll(tsdao.getStatus("postfix"));
			tableStatus.addAll(tsdao.getStatus("teamspeak"));
			tableStatus.addAll(tsdao.getStatus("online"));
			tableStatus.addAll(tsdao.getStatus(SystemData.get("airline.db").toLowerCase()));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Calculate database size
		long dbSize = 0;
		long dbRows = 0;
		for (TableInfo info : tableStatus) {
			dbSize += info.getSize();
			dbSize += info.getIndexSize();
			dbRows += info.getRows();
		}

		// Save database size
		totals.setDBRows(dbRows);
		totals.setDBSize(dbSize);

		// Save the results in the request
		ctx.setAttribute("totals", totals, REQUEST);
		ctx.setAttribute("tableStatus", tableStatus, REQUEST);
		ctx.setAttribute("effectiveDate", new Date(totals.getEffectiveDate()), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/airlineTotals.jsp");
		result.setSuccess(true);
	}
}
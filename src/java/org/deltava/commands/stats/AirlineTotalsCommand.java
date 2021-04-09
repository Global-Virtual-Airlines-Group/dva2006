// Copyright 2005, 2006, 2007, 2009, 2010, 2011, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.stats.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A web site command to display Airline Total statistics.
 * @author Luke
 * @version 10.0
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
			tableStatus.addAll(tsdao.getStatus("online"));
			tableStatus.addAll(tsdao.getStatus(ctx.getDB().toLowerCase()));
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Calculate database size
		totals.setDBRows(tableStatus.stream().mapToLong(TableInfo::getRows).sum());
		totals.setDBSize(tableStatus.stream().mapToLong(t -> t.getSize() + t.getIndexSize()).sum());

		// Save the results in the request
		ctx.setAttribute("totals", totals, REQUEST);
		ctx.setAttribute("tableStatus", tableStatus, REQUEST);
		ctx.setAttribute("effectiveDate", totals.getEffectiveDate(), REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/airlineTotals.jsp");
		result.setSuccess(true);
	}
}
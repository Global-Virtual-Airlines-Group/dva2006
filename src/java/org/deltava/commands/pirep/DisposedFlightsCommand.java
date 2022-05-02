// Copyright 2012, 2016, 2018, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to view recently disposed Flight Reports.
 * @author Luke
 * @version 10.2
 * @since 5.1
 */

public class DisposedFlightsCommand extends AbstractViewCommand {
	
	private static final String[] SORT_CODES = {"PR.DATE DESC, PR.SUBMITTED DESC", "P.LASTNAME, P.FIRSTNAME, PR.SUBMITTED DESC", "PR.EQTYPE, PR.DATE DESC, PR.SUBMITTED DESC"};
	private static final String[] SORT_NAMES = {"Submission Date", "Pilot Name", "Equipment Type"};
	private static final List<ComboAlias> SORT_OPTS = ComboUtils.fromArray(SORT_NAMES, SORT_CODES);

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
        // Get/set start/count parameters
        ViewContext<FlightReport> vc = initView(ctx, FlightReport.class);
        FlightStatus st = EnumUtils.parse(FlightStatus.class, ctx.getParameter("status"), FlightStatus.REJECTED);
        if (StringUtils.arrayIndexOf(SORT_CODES, vc.getSortType()) == -1)
        	vc.setSortType(SORT_CODES[0]);
		
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetFlightReports dao = new GetFlightReports(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Get the PIREPs and load the promotion type
			vc.setResults(dao.getByStatus(Collections.singleton(st), vc.getSortType()));
			dao.getCaptEQType(vc.getResults());
			
			// Get the Pilot IDs
			Collection<Integer> IDs = new HashSet<Integer>();
			for (FlightReport fr : vc.getResults()) {
				IDs.add(Integer.valueOf(fr.getDatabaseID(DatabaseID.PILOT)));
				IDs.add(Integer.valueOf(fr.getDatabaseID(DatabaseID.DISPOSAL)));
			}
			
			// Load the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save sort options
		ctx.setAttribute("sortTypes", SORT_OPTS, REQUEST);
		ctx.setAttribute("status", st, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/pirepDisposed.jsp");
		result.setSuccess(true);
	}
}
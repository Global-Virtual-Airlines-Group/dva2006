// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.FlightReport;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.*;

/**
 * A Web Site Command to display a Pilot's flight reports.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class LogBookCommand extends AbstractViewCommand {
	
    // List of query columns we can order by
    private static final String[] SORT_CODE = {"DATE DESC, PR.ID DESC", "EQTYPE", "DISTANCE DESC", "FLIGHT_TIME DESC"};
    private static final String[] SORT_NAMES = {"Flight Date", "Equipment", "Distance", "Flight Time"};
    private static final List SORT_OPTIONS = ComboUtils.fromArray(SORT_NAMES, SORT_CODE);

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
       
        // Get/set start/count parameters and pilot ID
        ViewContext vc = initView(ctx);
        if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
  		   	vc.setSortType(SORT_CODE[0]);
        
        // Determine if we display comments or not
        boolean showComments = "log".equals(ctx.getCmdParameter(Command.OPERATION, "log"));
        ctx.setAttribute("comments", Boolean.valueOf(showComments), REQUEST);
        
        // Set sort options
        ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
        
        try {
            Connection con = ctx.getConnection();
            
            // Get the pilot profile
            GetPilot dao = new GetPilot(con);
            ctx.setAttribute("pilot", dao.get(ctx.getID()), REQUEST);

            // Get the DAO and set the parameters
            GetFlightReports dao2 = new GetFlightReports(con);
            dao2.setQueryStart(vc.getStart());
            dao2.setQueryMax(vc.getCount());
            
            // Get the PIREP beans and load the promotion eligibility
            Collection<FlightReport> pireps = dao2.getByPilot(ctx.getID(), vc.getSortType());
            dao2.getCaptEQType(pireps);
            vc.setResults(pireps);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
      
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/pilot/logBook.jsp");
        result.setSuccess(true);
    }
}
// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.sql.Connection;
import java.util.Collection;
import java.util.stream.Collectors;

import org.deltava.beans.flight.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display Flight Reports grouped by aircraft SDK.  
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class FlightSDKCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
        // Get/set start/count parameters
		CommandResult result = ctx.getResult();
        ViewContext<FlightReport> vc = initView(ctx, FlightReport.class);
        String sdk = ctx.getParameter("sdk");
        
        try {
        	Connection con = ctx.getConnection();
        	
        	// Get SDK list
        	GetACARSLog aldao = new GetACARSLog(con);
        	ctx.setAttribute("SDKs", aldao.getSDKs(), REQUEST);
        	
        	// Load the flights
        	GetFlightReportACARS frdao = new GetFlightReportACARS(con);
        	vc.setResults(frdao.getBySDK(sdk));
        	
        	// Load the pilots
        	GetPilot pdao = new GetPilot(con);
        	Collection<Integer> pilotIDs = vc.getResults().stream().map(FlightReport::getAuthorID).collect(Collectors.toSet());
        	ctx.setAttribute("pilots", pdao.getByID(pilotIDs, "PILOTS"), REQUEST);
        } catch (DAOException de) {
        	throw new CommandException(de);
        } finally {
        	ctx.release();
        }
        
        // Forward to the JSP
        result.setURL("/jsp/pilot/pirepSDK.jsp");
        result.setSuccess(true);
	}
}
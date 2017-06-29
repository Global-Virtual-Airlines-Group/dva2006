// Copyright 2005, 2007, 2008, 2009, 2010, 2012, 2013, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ChartAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display Approach Charts.
 * @author Luke
 * @version 7.5
 * @since 1.0
 */

public class ChartsCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
    public void execute(CommandContext ctx) throws CommandException {
    	
        // Get airport code to display and save it into the request
        String aCode = ctx.getParameter("id");
        if ((aCode == null) || (aCode.length() < 3))
        	aCode = ctx.getUser().getHomeAirport();
        Airport a = SystemData.getAirport(aCode);
        if (a == null)
        	a = SystemData.getAirport("ATL");
        ctx.setAttribute("airport", a, REQUEST);
        
        // Get our access level
        ChartAccessControl access = new ChartAccessControl(ctx);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);

        try {
        	Connection con = ctx.getConnection();
        	
        	// Load the charts and airports
            GetChart dao = new GetChart(con);
            ctx.setAttribute("charts", dao.getCharts(a), REQUEST);
            ctx.setAttribute("airports", dao.getAirports(), REQUEST);
            
            // If this is a US airport, get the chart cycle data
            if (a.getCountry().getCode().equals("US")) {
            	GetMetadata mddao = new GetMetadata(con);
            	String chartCycleID = mddao.get("charts.cycle.faa");
            	if (chartCycleID != null) {
            		GetNavCycle ncdao = new GetNavCycle(con);
            		ctx.setAttribute("currentCycle", ncdao.getCycle(chartCycleID), REQUEST);
            	}
            }
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Save charts and types
        List<Chart.Type> typeNames = Arrays.asList(Chart.Type.values());
        ctx.setAttribute("chartTypes", typeNames.subList(1, typeNames.size()), REQUEST);
        ctx.setAttribute("selectedTypes", ctx.getParameters("chartType"), REQUEST);
        
        // Redirect to the home page
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/charts.jsp");
        result.setSuccess(true);
    }
}
// Copyright 2005, 2007, 2008, 2009, 2010, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ChartAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web site Command to display Approach Charts.
 * @author Luke
 * @version 4.2
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

        // Get charts for the airport
        try {
            GetChart dao = new GetChart(ctx.getConnection());
            ctx.setAttribute("charts", dao.getCharts(a), REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Calculate chart types
        List<Chart.Type> typeNames = Arrays.asList(Chart.Type.values());
        List<ComboAlias> types = ComboUtils.fromArray(Chart.Type.values());

        // Save charts and types
        ctx.setAttribute("typeCodes", typeNames.subList(1, typeNames.size()), REQUEST);
        ctx.setAttribute("chartTypes", types.subList(1, types.size()), REQUEST);
        ctx.setAttribute("selectedTypes", ctx.getParameters("chartType"), REQUEST);
        
        // Redirect to the home page
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/charts.jsp");
        result.setSuccess(true);
    }
}
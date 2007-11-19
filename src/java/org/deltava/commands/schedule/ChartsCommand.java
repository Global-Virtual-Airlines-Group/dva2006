// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

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
 * @version 2.0
 * @since 1.0
 */

public class ChartsCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        
        // Get airport code to display and save it into the request
        String aCode = ctx.getParameter("airport");
        Airport a = SystemData.getAirport((aCode == null) ? "ATL" : aCode);
        ctx.setAttribute("airport", a, REQUEST);
        
        // Get our access level
        ChartAccessControl access = new ChartAccessControl(ctx);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        Collection<Chart> results = null;
        try {
            Connection con = ctx.getConnection();
            
            // Get available airport codes and charts
            GetChart dao = new GetChart(con); 
            ctx.setAttribute("airports", dao.getAirports(), REQUEST);
            results = dao.getCharts(a);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }

        // Filter charts
        Collection<String> chartTypes = ctx.getParameters("chartType");
        if (!CollectionUtils.isEmpty(chartTypes)) {
        	for (Iterator<Chart> i = results.iterator(); i.hasNext(); ) {
        		Chart c = i.next();
        		if (!chartTypes.contains(String.valueOf(c.getType())))
        			i.remove();
        	}
        }
        
        // Build list of chart types
        Collection<ComboAlias> choices = new ArrayList<ComboAlias>();
        for (int x = 1; x < Chart.TYPES.length; x++)
        	choices.add(ComboUtils.fromString(Chart.TYPENAMES[x], String.valueOf(x)));
        
        // Save charts and types
        ctx.setAttribute("chartTypes", choices, REQUEST);
        ctx.setAttribute("charts", results, REQUEST);
        
        // Redirect to the home page
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/charts.jsp");
        result.setSuccess(true);
    }
}
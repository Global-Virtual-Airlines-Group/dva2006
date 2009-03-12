// Copyright 2005, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.2
 * @since 1.0
 */

public class ChartsCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
    	
        // Get airport code to display and save it into the request
        String aCode = ctx.getParameter("id");
        if ((aCode == null) || (aCode.length() < 3))
        	aCode = ctx.getUser().getHomeAirport();
        Airport a = SystemData.getAirport(aCode);
        ctx.setAttribute("airport", a, REQUEST);
        
        // Get our access level
        ChartAccessControl access = new ChartAccessControl(ctx);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);

        // Get charts for the airport
        ViewContext vc = initView(ctx);
        try {
            GetChart dao = new GetChart(ctx.getConnection());
            vc.setResults(dao.getCharts(a));
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Calculate chart types
        List<String> typeNames = Arrays.asList(Chart.TYPES);
        List<ComboAlias> types = ComboUtils.fromArray(Chart.TYPENAMES, Chart.TYPES);

        // Save charts and types
        ctx.setAttribute("chartTypeNames", typeNames.subList(1, typeNames.size()), REQUEST);
        ctx.setAttribute("chartTypes", types.subList(1, types.size()), REQUEST);
        ctx.setAttribute("selectedTypes", ctx.getParameters("chartType"), REQUEST);
        ctx.setAttribute("emptyList", Collections.emptyList(), REQUEST);
        
        // Redirect to the home page
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/schedule/charts.jsp");
        result.setSuccess(true);
    }
}
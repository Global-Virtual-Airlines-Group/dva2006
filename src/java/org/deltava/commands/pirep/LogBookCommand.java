// Copyright 2005, 2006, 2007, 2008, 2009, 2010, 2012, 2017, 2018, 2021, 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.pirep;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.comparators.AirportComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PIREPAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display a Pilot's Flight Reports.
 * @author Luke
 * @version 10.3
 * @since 1.0
 */

public class LogBookCommand extends AbstractViewCommand {
	
    // List of query columns we can order by
    private static final String[] SORT_CODE = {"DATE DESC, PR.SUBMITTED DESC, PR.ID DESC", "EQTYPE", "DISTANCE DESC", "AIRPORT_D", "AIRPORT_A", "FLIGHT_TIME DESC", "AIRLINE, DATE DESC"};
    private static final String[] SORT_NAMES = {"Flight Date", "Equipment", "Distance", "Origin", "Destination", "Flight Time", "Airline"};
    private static final List<ComboAlias> SORT_OPTIONS = ComboUtils.fromArray(SORT_NAMES, SORT_CODE);
    
    // Exporter formats
    private static final List<ComboAlias> EXPORTERS = ComboUtils.fromArray(new String[] {"Default CSV",  "Volanta CSV", "Default JSON"}, new String[] {"DefaultCSVExport", "VolantaCSVExport", "JSONExport"});

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    @Override
    public void execute(CommandContext ctx) throws CommandException {
       
        // Get/set start/count parameters
    	ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
        ViewContext<FlightReport> vc = initView(ctx, FlightReport.class);
        if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
  		   	vc.setSortType(SORT_CODE[0]);
       
        // Determine who to display
        CommandResult result = ctx.getResult();
        int id = ctx.getID(); 
        if ((id == 0) && ctx.isAuthenticated()) {
        	result.setURL("logbook", null, ctx.getUser().getID());
        	result.setType(ResultType.REDIRECT);
        	result.setSuccess(true);
        	return;
        }
        
        // Redirect if no one specified
        if (id == 0) {
        	result.setURL("lroster.do");
        	result.setType(ResultType.REDIRECT);
        	result.setSuccess(true);
        	return;
        }
        
        // Initialize the search criteria
        LogbookSearchCriteria criteria = new LogbookSearchCriteria(vc.getSortType(), ctx.getDB());
        criteria.setEquipmentType(ctx.getParameter("eqType"));
        criteria.setAirportD(SystemData.getAirport(ctx.getParameter("airportD")));
        criteria.setAirportA(SystemData.getAirport(ctx.getParameter("airportA")));
        criteria.setLoadComments("log".equals(ctx.getCmdParameter(Command.OPERATION, "log")));
        ctx.setAttribute("comments", Boolean.valueOf(criteria.getLoadComments()), REQUEST);
        
        try {
            Connection con = ctx.getConnection();
            
            // Get the pilot profile
            GetPilot dao = new GetPilot(con);
            Pilot p = dao.get(id);
            if (p == null)
            	throw notFoundException("Invalid Pilot - " + id);
            else if (p.getIsForgotten() && !ctx.isUserInRole("HR"))
            	throw forgottenException();

            // Get the DAO and set the parameters
            GetFlightReports dao2 = new GetFlightReports(con);
            dao2.setQueryStart(vc.getStart());
            dao2.setQueryMax(vc.getCount());
            
            // Get the PIREP beans and load the promotion eligibility
            vc.setResults(dao2.getByPilot(id, criteria));
            dao2.getCaptEQType(vc.getResults());
            
            // Load the Equipment types
            GetAircraft acdao = new GetAircraft(con);
            ctx.setAttribute("eqTypes", acdao.getAircraftTypes(id), REQUEST);
            
            // Load the airport options
            Collection<Airport> airports = new TreeSet<Airport>(new AirportComparator(AirportComparator.NAME));
            GetAirport adao = new GetAirport(con);
            airports.addAll(adao.getByPilot(id)); 
            ctx.setAttribute("airports", airports, REQUEST);
            ctx.setAttribute("pilot", p, REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Get pre-approval ability
        PIREPAccessControl ac = new PIREPAccessControl(ctx, null);
        ac.validate();
        ctx.setAttribute("access", ac, REQUEST);
        ctx.setAttribute("exportTypes", EXPORTERS, REQUEST);
      
        // Set the result page and return
        result.setURL("/jsp/pilot/logBook.jsp");
        result.setSuccess(true);
    }
}
// Copyright 2005, 2009, 2016, 2018, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * A Web Site Command to display the Pilot Roster.
 * @author Luke
 * @version 11.1
 * @since 1.0
 */

public class RosterCommand extends AbstractViewCommand {

	private static final String[] SORT_TYPE = {"First Name", "Last Name", "Last Login", "Join Date", "Pilot Code", "Equipment Type", "Rank"};
    private static final String[] SORT_CODE = {"FIRSTNAME", "LASTNAME", "LAST_LOGIN DESC", "CREATED", "PILOT_ID", "EQTYPE", "RANKING DESC, PILOT_ID, CREATED"};
    
    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    @Override
	public void execute(CommandContext ctx) throws CommandException {
        
        // Get/set start/count parameters
        ViewContext<Pilot> vc = initView(ctx, Pilot.class);
        if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
           vc.setSortType(SORT_CODE[4]);
        
        try {
        	Connection con = ctx.getConnection();
        	
        	// Get equipment types
        	GetEquipmentType eqdao = new GetEquipmentType(con);
        	EquipmentType eq = eqdao.get(ctx.getParameter("eqType"));
        	ctx.setAttribute("eqTypes", eqdao.getActive(), REQUEST);
        	
        	// Load the roster
            GetPilot dao = new GetPilot(con);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            if (eq == null) {
            	List<Integer> IDs = dao.getActivePilots(vc.getSortType());
            	Map<Integer, Pilot> pilots = dao.getByID(IDs, "PILOTS");
            	vc.setResults(IDs.stream().map(pilots::get).filter(Objects::nonNull).collect(Collectors.toList()));
            } else
            	vc.setResults(dao.getPilotsByEQ(eq, vc.getSortType(), true, null));
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Save the sorter types in the request
        ctx.setAttribute("sortTypes", ComboUtils.fromArray(SORT_TYPE, SORT_CODE), REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/roster/mainRoster.jsp");
        result.setSuccess(true);
    }
}
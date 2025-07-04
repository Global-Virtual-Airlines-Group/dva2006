// Copyright 2005, 2006, 2008, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.beans.Staff;
import org.deltava.comparators.StaffComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display the Staff Roster.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class StaffRosterCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@SuppressWarnings("unchecked")
	@Override
    public void execute(CommandContext ctx) throws CommandException {
		
    	// Build the comparator
    	StaffComparator cmp = new StaffComparator(StaffComparator.AREA);
    	cmp.setAreas((Collection<String>) SystemData.getObject("staff.departments"));
    	Collection<Staff> results = new TreeSet<Staff>(cmp);
        try {
            GetStaff dao = new GetStaff(ctx.getConnection());
            results.addAll(dao.getStaff());
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Sort and save the profiles
        ctx.setAttribute("staffRoster", results, REQUEST);
        ctx.setAttribute("hasAreas", Boolean.valueOf(cmp.hasAreas()), REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/roster/staffRoster.jsp");
        result.setSuccess(true);
    }
}
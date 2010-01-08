// Copyright 2005, 2006, 2007, 2008, 2009, 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.beans.*;
import org.deltava.beans.stats.PilotLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.comparators.PilotComparator;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.security.*;
import org.deltava.util.*;

/**
 * A Web Site Command to list logged in users.
 * @author Luke
 * @version 2.8
 * @since 1.0
 */

public class UserListCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
    	
    	// Get the sort type
    	String sortOpt = ctx.getParameter("sortOpt");
    	if (StringUtils.isEmpty(sortOpt))
    		sortOpt = PilotComparator.TYPES[PilotComparator.PILOTCODE];
    	
    	// Initialize the comparator
    	Collection<Integer> IDs = new HashSet<Integer>();
    	Map<Pilot, UserSession> users = new TreeMap<Pilot, UserSession>(new PilotComparator(sortOpt));
    	for (Iterator<UserSession> i = UserPool.getPilots().iterator(); i.hasNext(); ) {
    		UserSession us = i.next();
    		users.put(us.getPerson(), us);
    		IDs.add(new Integer(us.getPerson().getID()));
    	}
    	
        // Check if we're displaying a map
        boolean showMap = "map".equals(ctx.getCmdParameter(OPERATION, "false"));

    	// Get maximum concurrent user data
    	ctx.setAttribute("maxUsers", Integer.valueOf(UserPool.getMaxSize()), REQUEST);
    	ctx.setAttribute("maxUserDate", UserPool.getMaxSizeDate(), REQUEST);

        // Get and save the users in the request
    	if (showMap) {
    		Map<Integer, GeoLocation> pLocs = new HashMap<Integer, GeoLocation>();
    		try {
    			GetPilotBoard pbdao = new GetPilotBoard(ctx.getConnection());
    			pLocs.putAll(pbdao.getByID(IDs));
    		} catch (DAOException de) {
    			throw new CommandException(de);
    		} finally {
    			ctx.release();
    		}
    		
    		// Set the location
    		boolean isHR = ctx.isUserInRole("HR");
    		Collection<MapEntry> markers = new ArrayList<MapEntry>();
    		for (Iterator<UserSession> i = users.values().iterator(); i.hasNext(); ) {
    			UserSession usr = i.next();
    			GeoLocation loc = pLocs.get(new Integer(usr.getPerson().getID()));
    			if (!isHR) {
    				if ((loc == null) && (usr.getAddressInfo() != null)) {
    					loc = new GeoPosition(usr.getAddressInfo());
    					markers.add(new PilotLocation(usr.getPerson(), loc));
    				}
    			} else if (usr.getAddressInfo() != null)
    				markers.add(new PilotLocation(usr.getPerson(), usr.getAddressInfo()));
    			else if (loc != null)
    				markers.add(new PilotLocation(usr.getPerson(), loc));
    		}
    		
    		ctx.setAttribute("pilots", markers, REQUEST);
    	} else
    		ctx.setAttribute("pilots", users.values(), REQUEST);
        
        // Save combobox options
        ctx.setAttribute("sortOpt", sortOpt, REQUEST);
        ctx.setAttribute("sortOptions", ComboUtils.fromArray(PilotComparator.TYPES), REQUEST);
        
        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL(showMap? "/jsp/main/onlineUserMap.jsp" : "/jsp/main/onlineUsers.jsp");
        result.setSuccess(true);
    }
}
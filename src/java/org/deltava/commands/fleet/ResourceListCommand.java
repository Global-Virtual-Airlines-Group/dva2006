// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.fleet;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.fleet.Resource;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.ResourceAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to display Web Resources.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ResourceListCommand extends AbstractViewCommand {
	
	private static final String[] SORT_CODES = {"CREATEDON DESC", "HITCOUNT DESC"};
	private static final List SORT_OPTIONS = ComboUtils.fromArray(new String[] {"Created On", "Popularity"}, SORT_CODES);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
        // Get/set start/count parameters
        ViewContext vc = initView(ctx);
        if (StringUtils.arrayIndexOf(SORT_CODES, vc.getSortType()) == -1)
        	vc.setSortType(SORT_CODES[0]);
        
        // Get the category
        String catName = ctx.getParameter("cat");
        
        // Check if we can see all resources
        boolean viewAll = (ctx.getRoles().size() > 1);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetResources dao = new GetResources(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			Collection<Resource> results = viewAll ? dao.getAll(catName, vc.getSortType()) : dao.getAll(catName, ctx.getUser().getID(), vc.getSortType());
			vc.setResults(results);
			
			// Create access Map
			Map<Integer, ResourceAccessControl> accessMap = new HashMap<Integer, ResourceAccessControl>();
			
			// Load author IDs and get access
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Iterator<Resource> i = results.iterator(); i.hasNext(); ) {
				Resource r = i.next();
				IDs.add(new Integer(r.getAuthorID()));
				
				// Calculate access
				ResourceAccessControl access = new ResourceAccessControl(ctx, r);
				access.validate();
				accessMap.put(new Integer(r.getID()), access);
			}
			
			// Save the access map
			ctx.setAttribute("accessMap", accessMap, REQUEST);
			
			// Load the Author profiles
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(IDs, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save our Access
		ResourceAccessControl ac = new ResourceAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);
		
		// Save sort options
		ctx.setAttribute("sortOptions", SORT_OPTIONS, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/fleet/resourceList.jsp");
		result.setSuccess(true);
	}
}
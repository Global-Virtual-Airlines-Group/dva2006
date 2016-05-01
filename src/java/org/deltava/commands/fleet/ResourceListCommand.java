// Copyright 2006, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 7.0
 * @since 1.0
 */

public class ResourceListCommand extends AbstractViewCommand {
	
	private static final String[] SORT_CODES = {"CREATEDON DESC", "HITCOUNT DESC", "DOMAIN", "CATEGORY, URL"};
	private static final List<?> SORT_OPTIONS = ComboUtils.fromArray(new String[] {"Created On", "Popularity", "Web Site", "Category"}, SORT_CODES);

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
        // Get/set start/count parameters
        ViewContext<Resource> vc = initView(ctx, Resource.class);
        if (StringUtils.arrayIndexOf(SORT_CODES, vc.getSortType()) == -1)
        	vc.setSortType(SORT_CODES[0]);
        
        // Get the category
        String catName = ctx.getParameter("cat");
        if ("ALL".equals(catName))
        	catName = null;
        
        // Check if we can see all resources
        boolean viewAll = (ctx.getRoles().size() > 1);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO
			GetResources dao = new GetResources(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(viewAll ? dao.getAll(catName, vc.getSortType()) : dao.getAll(catName, ctx.getUser().getID(), vc.getSortType()));
			
			// Load author IDs and get access
			Map<Integer, ResourceAccessControl> accessMap = new HashMap<Integer, ResourceAccessControl>();
			Collection<Integer> IDs = new HashSet<Integer>();
			for (Resource r : vc.getResults()) {
				IDs.add(Integer.valueOf(r.getAuthorID()));
				
				// Calculate access
				ResourceAccessControl access = new ResourceAccessControl(ctx, r);
				access.validate();
				accessMap.put(Integer.valueOf(r.getID()), access);
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
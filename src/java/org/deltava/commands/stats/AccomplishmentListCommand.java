// Copyright 2010, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.stats;

import java.util.*;

import org.deltava.beans.stats.Accomplishment;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AccomplishmentAccessControl;

/**
 * A Web Site Command to display Accomplishment profiles. 
 * @author Luke
 * @version 7.0
 * @since 3.2
 */

public class AccomplishmentListCommand extends AbstractViewCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		ViewContext<Accomplishment> vc = initView(ctx, Accomplishment.class);
		try {
			GetAccomplishment dao = new GetAccomplishment(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getAll());
			
			// Check our access
			Map<Accomplishment, AccomplishmentAccessControl> accessMap = new HashMap<Accomplishment, AccomplishmentAccessControl>();
			for (Accomplishment a : vc.getResults()) {
				AccomplishmentAccessControl ac = new AccomplishmentAccessControl(ctx, a);
				ac.validate();
				accessMap.put(a, ac);
			}
			
			ctx.setAttribute("accessMap", accessMap, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save default access
		AccomplishmentAccessControl ac = new AccomplishmentAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/stats/accomplishmentList.jsp");
		result.setSuccess(true);
	}
}
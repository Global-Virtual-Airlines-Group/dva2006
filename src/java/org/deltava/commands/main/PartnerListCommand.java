// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.util.*;

import org.deltava.beans.PartnerInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PartnerAccessControl;

/**
 * A Web Site Command to display partner organization information.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PartnerListCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		ViewContext<PartnerInfo> vc = initView(ctx, PartnerInfo.class);
		try {
			GetPartner dao = new GetPartner(ctx.getConnection());
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getPartners());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set access
		Map<Integer, PartnerAccessControl> accessMap = new HashMap<Integer, PartnerAccessControl>();
		for (PartnerInfo p : vc.getResults()) {
			PartnerAccessControl pac = new PartnerAccessControl(ctx, p);
			pac.validate();
			accessMap.put(Integer.valueOf(p.getID()), pac);
		}
		
		// Save access controller
		PartnerAccessControl ac = new PartnerAccessControl(ctx, null);
		ac.validate();
		ctx.setAttribute("access", ac, REQUEST);
		ctx.setAttribute("accessMap", accessMap, REQUEST);

		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/main/partners.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import org.deltava.beans.PartnerInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;

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

		// Foward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/main/partnerList.jsp");
		result.setSuccess(true);
	}
}
// Copyright 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.hr;

import java.sql.*;
import java.util.*;

import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display the login/registration blacklist.
 * @author Luke
 * @version 9.0
 * @since 9.0
 */

public class BlacklistCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<BlacklistEntry> vc = initView(ctx, BlacklistEntry.class);
		try {
			Connection con = ctx.getConnection();
			
			// Load the blacklist
			GetSystemData dao = new GetSystemData(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			vc.setResults(dao.getBlacklist());
			
			// Load the locations
			Map<String, IPBlock> locs = new HashMap<String, IPBlock>();
			GetIPLocation ipdao = new GetIPLocation(con);
			for (BlacklistEntry be : vc.getResults()) {
				String addr = be.getCIDR().getNetworkAddress();
				IPBlock ip = ipdao.get(addr);
				if (addr != null)
					locs.put(addr, ip);
			}

			ctx.setAttribute("locations", locs, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/hr/blacklistView.jsp");
		result.setSuccess(true);
	}
}
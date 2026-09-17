// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.system.IPBlock;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to view Admin Log Entries. 
 * @author Luke
 * @version 12.5
 * @since 12.5
 */

public class AdminLogCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<AdminLogEntry> vctx = initView(ctx, AdminLogEntry.class);
		int dayCount = StringUtils.parse(ctx.getParameter("days"), 7);
		try {
			Connection con = ctx.getConnection();
			
			// Get the log entries and aggregated stats
			GetAuditLog dao = new GetAuditLog(con);
			vctx.setResults(dao.getAdminEntries());
			
			// Load the IP addresses
			GetIPLocation ipdao = new GetIPLocation(con);
			Collection<String> addrs = vctx.getResults().stream().map(RemoteAddressBean::getRemoteAddr).collect(Collectors.toSet());
			Map<String, IPBlock> ipInfo = new HashMap<String, IPBlock>();
			for (String addr : addrs)
				ipInfo.put(addr, ipdao.get(addr));
			
			// Get the authors
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("authors", pdao.getByID(vctx.getResults().stream().map(AuthoredBean::getAuthorID).collect(Collectors.toSet()), "PILOTS"), REQUEST);
			
			// Save request attributes
			ctx.setAttribute("days", Integer.valueOf(dayCount), REQUEST);
			ctx.setAttribute("ip", ipInfo, REQUEST);
			ctx.setAttribute("stats", dao.getAdminStats(dayCount), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/adminLog.jsp");
		result.setSuccess(true);
	}
}
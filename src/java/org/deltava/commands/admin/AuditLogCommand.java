// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.system.IPBlock;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to view the Audit Log. 
 * @author Luke
 * @version 7.5
 * @since 7.5
 */

public class AuditLogCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		ViewContext<AuditLog> vc = initView(ctx, AuditLog.class);
		try {
			Connection con = ctx.getConnection();
			
			// Load the Audit Log
			GetAuditLog adao = new GetAuditLog(con);
			adao.setQueryStart(vc.getStart());
			adao.setQueryMax(vc.getCount());
			vc.setResults(adao.getEntries());
			
			// Load the IP addresses
			GetIPLocation ipdao = new GetIPLocation(con);
			Collection<String> addrs = vc.getResults().stream().map(AuditLog::getRemoteAddr).collect(Collectors.toSet());
			Map<String, IPBlock> ipInfo = new HashMap<String, IPBlock>();
			for (String addr : addrs)
				ipInfo.put(addr, ipdao.get(addr));
			
			// Load the Authors
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			Collection<Integer> IDs = vc.getResults().stream().map(AuthoredBean::getAuthorID).collect(Collectors.toSet());
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("authors", pdao.get(udm), REQUEST);
			ctx.setAttribute("userdata", udm, REQUEST);
			ctx.setAttribute("ipInfo", ipInfo, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/admin/auditLog.jsp");
		result.setSuccess(true);
	}
}
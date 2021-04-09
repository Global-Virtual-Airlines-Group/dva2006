// Copyright 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.*;
import org.deltava.beans.system.IPBlock;

import org.deltava.dao.*;

/**
 * A class to support form operations on auditable objects.
 * @author Luke
 * @version 10.0
 * @since 7.4
 */

public abstract class AbstractAuditFormCommand extends AbstractFormCommand {
	
	/**
	 * Loads the audit log for an object.
	 * @param ctx the CommandContext
	 * @param a the Auditable object
	 * @throws DAOException if a JDBC error occurs
	 */
	protected static void readAuditLog(CommandContext ctx, Auditable a) throws DAOException {
		if ((a == null) || !ctx.isAuthenticated()) return;
		boolean freeConnection = !ctx.hasConnection();
		Connection con = ctx.getConnection();
		
		// Load audit log
		try {
			GetAuditLog aldao = new GetAuditLog(con);
			Collection<AuditLog> entries =  aldao.getEntries(a);
			Collection<Integer> IDs = entries.stream().map(AuthoredBean::getAuthorID).collect(Collectors.toSet());
		
			// Load author IDs
			GetUserData uddao = new GetUserData(con);
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(IDs);
			ctx.setAttribute("auditAuthors", pdao.get(udm), REQUEST);
		
			// Load IP data
			Map<String, IPBlock> ipInfo = new HashMap<String, IPBlock>();
			GetIPLocation ipdao = new GetIPLocation(con);
			Collection<String> ipAddrs = entries.stream().map(AuditLog::getRemoteAddr).collect(Collectors.toSet());
			for (String addr : ipAddrs)
				ipInfo.put(addr, ipdao.get(addr));
			
			ctx.setAttribute("auditLog", entries, REQUEST);
			ctx.setAttribute("auditIPInfo", ipInfo, REQUEST);
		} finally {
			if (freeConnection)
				ctx.release();
		}
	}

	/**
	 * Writes an audit log entry for an object.
	 * @param ctx the CommandContext
	 * @param ae the AuditLog entry
	 * @throws DAOException if a JDBC error occurs
	 */
	protected static void writeAuditLog(CommandContext ctx, AuditLog ae) throws DAOException {
		if (ae == null) return;
		boolean freeConnection = !ctx.hasConnection();
		ae.setRemoteAddr(ctx.getRequest().getRemoteAddr());
		ae.setRemoteHost(ctx.getRequest().getRemoteHost());
		
		try {
			SetAuditLog awdao = new SetAuditLog(ctx.getConnection());
			awdao.write(ae);
		} finally {
			if (freeConnection)
				ctx.release();
		}
	}
}
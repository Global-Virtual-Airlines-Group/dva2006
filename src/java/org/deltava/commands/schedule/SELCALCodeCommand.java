// Copyright 2005 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.schedule.SelectCall;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.SELCALAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display aircraft SELCAL codes.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class SELCALCodeCommand extends AbstractViewCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the view context
		ViewContext vc = initView(ctx);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the parameters
			GetSELCAL dao = new GetSELCAL(con);
			dao.setQueryStart(vc.getStart());
			dao.setQueryMax(vc.getCount());
			
			// Pull up the SELCAL codes
			Collection codes = dao.getCodes();
			vc.setResults(codes);
			
			// Check if we can reserve new codes
			int maxCodes = SystemData.getInt("users.selcal.max", 2);
			int usrCodes = dao.getReserved(ctx.getUser().getID()).size();
			
			// Calculate the access control
			Map<String, SELCALAccessControl> access = new HashMap<String, SELCALAccessControl>();
			for (Iterator<SelectCall> i = codes.iterator(); i.hasNext(); ) {
				SelectCall sc = i.next();
				SELCALAccessControl ac = new SELCALAccessControl(ctx, sc);
				if (usrCodes >= maxCodes)
					ac.markUnavailable();
				
				ac.validate();
				access.put(sc.getCode(), ac);
			}
			
			// Save the access control map
			ctx.setAttribute("accessMap", access, REQUEST);
			
			// Load the Pilots
			Set<Integer> ids = new HashSet<Integer>();
			for (Iterator i = codes.iterator(); i.hasNext(); ) {
				SelectCall sc = (SelectCall) i.next();
				if (sc.getReservedBy() != 0)
					ids.add(new Integer(sc.getReservedBy()));
			}
			
			// Get the Pilot DAO and the Pilots
			GetPilot pdao = new GetPilot(con);
			ctx.setAttribute("pilots", pdao.getByID(ids, "PILOTS"), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/schedule/selcal.jsp");
		result.setSuccess(true);
	}
}
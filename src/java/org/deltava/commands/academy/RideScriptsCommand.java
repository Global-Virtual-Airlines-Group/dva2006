// Copyright 2010 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;

import org.deltava.beans.academy.AcademyRideScript;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.AcademyRideScriptAccessControl;

/**
 * A Web Site Command to display Flight Academy Check Ride script commands.
 * @author Luke
 * @version 3.4
 * @since 3.4
 */

public class RideScriptsCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		Map<String, AcademyRideScriptAccessControl> access = new HashMap<String, AcademyRideScriptAccessControl>();
		try {
			GetAcademyCertifications acdao = new GetAcademyCertifications(ctx.getConnection());
			Collection<AcademyRideScript> scripts = acdao.getScripts();
			for (AcademyRideScript sc : scripts) {
				AcademyRideScriptAccessControl ac = new AcademyRideScriptAccessControl(ctx, sc);
				ac.validate();
				access.put(sc.getCertificationName(), ac);
			}
			
			// Save request attributes
			ctx.setAttribute("scripts", scripts, REQUEST);
			ctx.setAttribute("access", access, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Check Access control
		AcademyRideScriptAccessControl ac = new AcademyRideScriptAccessControl(ctx, null);
		ac.validate();
		access.put("NEW", ac);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/crScripts.jsp");
		result.setSuccess(true);
	}
}
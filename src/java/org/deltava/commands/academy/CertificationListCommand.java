// Copyright 2006, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CertificationAccessControl;

/**
 * A Web Site Command to display Flight Academy certifications.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CertificationListCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		try {
			GetAcademyCertifications dao = new GetAcademyCertifications(ctx.getConnection());
			ctx.setAttribute("certs", dao.getAll(), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save the access controller
		CertificationAccessControl access = new CertificationAccessControl(ctx);
		access.validate();
		ctx.setAttribute("access", access, REQUEST);

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/certList.jsp");
		result.setSuccess(true);
	}
}
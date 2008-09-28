// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.sql.Connection;

import org.deltava.beans.academy.Certification;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CertificationAccessControl;

/**
 * A Web Site Command to delete Flight Academy certifications.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class CertificationDeleteCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		String name = (String) ctx.getCmdParameter(ID, "");
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the certification
			GetAcademyCertifications dao = new GetAcademyCertifications(con);
			Certification cert = dao.get(name);
			if (cert == null)
				throw notFoundException("Invalid Certification - " + cert);
			
			// Check our access
			CertificationAccessControl access = new CertificationAccessControl(ctx);
			access.validate();
			if (!access.getCanDelete())
				throw securityException("Cannot delete Certification");
			
			// Save in the request
			ctx.setAttribute("cert", cert, REQUEST);
			
			// Get the write DAO and delete the Certification
			SetAcademy wdao = new SetAcademy(con);
			wdao.delete(name);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set status attribute
		ctx.setAttribute("isDelete", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/academy/certUpdate.jsp");
		result.setType(ResultType.REQREDIRECT);
		result.setSuccess(true);
	}
}
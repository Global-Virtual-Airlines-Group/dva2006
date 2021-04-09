// Copyright 2005, 2006, 2007, 2008, 2014, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.AddressValidationHelper;

import org.deltava.util.StringUtils;

/**
 * A Web Site Command to validate e-mail addresses.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class ValidateEmailCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();
		
		Pilot p = null;
		AddressValidation av = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilot dao = new GetPilot(con);
			p = dao.get(ctx.getUser().getID());
			if (p == null)
				throw notFoundException("Invalid Pilot - " + ctx.getUser().getID());
			
			// Save the pilot in the request
			ctx.setAttribute("person", p, REQUEST);

			// Get the e-mail validation information
			GetAddressValidation avdao = new GetAddressValidation(con);
			av = avdao.get(p.getID());
			if (av == null) {
				ctx.release();
				result.setURL("/jsp/pilot/eMailValid.jsp");
				result.setSuccess(true);
				return;
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// If we're an invalid address, get a new one
		if (!av.getIsValid()) {
			ctx.setAttribute("addr", av, REQUEST);
			ctx.setAttribute("invalidatedAddr", Boolean.TRUE, REQUEST);
			result.setURL("/jsp/pilot/eMailUpdate.jsp");
			result.setSuccess(true);
			return;
		}

		// If the hashes don't match, then stop us
		String hash = AddressValidationHelper.formatHash(ctx.getParameter("code"));
		if (!av.getHash().equalsIgnoreCase(hash)) {
			ctx.setAttribute("addr", av, REQUEST);
			ctx.setAttribute("validationFailure", Boolean.valueOf(!StringUtils.isEmpty(hash)), REQUEST);
			result.setURL("/jsp/pilot/eMailValidate.jsp");
			result.setSuccess(true);
			return;
		}

		try {
			Connection con = ctx.getConnection();
			ctx.startTX();

			// Update the Pilot
			SetPilot wdao = new SetPilot(con);
			p.setEmail(av.getAddress());
			p.setEmailInvalid(false);
			wdao.write(p, ctx.getDB());

			// Clear the invalid e-mail entry
			SetAddressValidation wavdao = new SetAddressValidation(con);
			wavdao.delete(av.getID());

			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setURL("/jsp/pilot/eMailValid.jsp");
		result.setSuccess(true);
	}
}
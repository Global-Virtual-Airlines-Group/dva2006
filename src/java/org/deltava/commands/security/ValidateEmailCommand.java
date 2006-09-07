// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;
import org.deltava.beans.system.UserData;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.AddressValidationHelper;

/**
 * A Web Site Command to validate e-mail addresses.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ValidateEmailCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get the command result
		CommandResult result = ctx.getResult();
		
		// If no e-mail address provided, go direct to the JSP
		String addr = ctx.getParameter("email");
		if (addr == null) {
			result.setURL("/jsp/register/eMailValidate.jsp");
			result.setSuccess(true);
			return;
		}

		Person p = null;
		AddressValidation av = null;
		try {
			Connection con = ctx.getConnection();

			// Get the e-mail validation information
			GetAddressValidation avdao = new GetAddressValidation(con);
			av = avdao.getAddress(addr);
			if (av == null) {
				ctx.release();
				ctx.setMessage("The specified e-mail address does not exist.");
				ctx.setAttribute("addr", av, REQUEST);
				ctx.setAttribute("code", AddressValidationHelper.formatHash(ctx.getParameter("code")), REQUEST);
				
				// Forward to the JSP
				result.setURL("/jsp/register/eMailValidate.jsp");
				result.setSuccess(true);
				return;
			}

			// Get the User Data
			GetUserData usrdao = new GetUserData(con);
			UserData usr = usrdao.get(av.getID());
			if (usr == null)
				throw notFoundException("Invalid Pilot/Applicant ID - " + av.getID());

			// Get the applicant or pilot
			if (usr.isApplicant()) {
				GetApplicant dao = new GetApplicant(con);
				p = dao.get(av.getID());
			} else {
				GetPilot dao = new GetPilot(con);
				p = dao.get(av.getID());
			}

			// Check that the user exists
			if (p == null)
				throw notFoundException("Invalid Pilot/Applicant - " + av.getID());
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Make sure we're either anonymous or the same user
		if ((ctx.isAuthenticated()) && (ctx.getUser().getID() != p.getID()))
			throw securityException(ctx.getUser() + " attempting to Validate for " + p.getName());
		
		// If there's no validation record on file, then assume we're valid
		if (av == null) {
			AddressValidationHelper.clearSessionFlag(ctx.getSession());
			result.setURL("/jsp/register/eMailValid.jsp");
			result.setSuccess(true);
			return;
		}

		// If the hashes don't match, then stop us
		String code = AddressValidationHelper.formatHash(ctx.getParameter("code"));
		if (!av.getHash().equals(code)) {
			ctx.setAttribute("person", p, REQUEST);
			ctx.setAttribute("addr", av, REQUEST);
			ctx.setAttribute("validationFailure", Boolean.TRUE, REQUEST);
			result.setURL("/jsp/register/eMailValidate.jsp");
			result.setSuccess(true);
			return;
		}

		// Update the e-mail address
		p.setEmail(av.getAddress());
		
		// Remove the session attribute
		AddressValidationHelper.clearSessionFlag(ctx.getSession());

		try {
			Connection con = ctx.getConnection();

			// Start the transaction
			ctx.startTX();

			// Update the user record
			if (p instanceof Applicant) {
				SetApplicant wdao = new SetApplicant(con);
				wdao.write((Applicant) p);
			} else {
				SetPilot wdao = new SetPilot(con);
				wdao.write((Pilot) p);
			}

			// Clear the invalid e-mail entry
			SetAddressValidation wavdao = new SetAddressValidation(con);
			wavdao.delete(av.getID());

			// Commit the transaction
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Save the person in the request
		ctx.setAttribute("person", p, REQUEST);
		ctx.setAttribute("isApplicant", Boolean.valueOf(p instanceof Applicant), REQUEST);
		
		// Forward to the JSP
		result.setURL("/jsp/register/eMailValid.jsp");
		result.setSuccess(true);
	}
}
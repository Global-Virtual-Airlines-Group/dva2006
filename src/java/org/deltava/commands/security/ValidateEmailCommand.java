// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;
import org.deltava.beans.system.UserData;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.StringUtils;

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

		Person p = null;
		AddressValidation av = null;
		try {
			Connection con = ctx.getConnection();

			// Since the ID might not be hex-encoded get it a different way
			int id = StringUtils.parseHex((String) ctx.getCmdParameter(Command.ID, "0"));

			// Get the User Data
			GetUserData usrdao = new GetUserData(con);
			UserData usr = usrdao.get(id);

			// Get the applicant or pilot
			if (usr.isApplicant()) {
				GetApplicant dao = new GetApplicant(con);
				p = dao.get(id);
			} else {
				GetPilot dao = new GetPilot(con);
				p = dao.get(id);
			}

			// Check that the user exists
			if (p == null)
				throw new CommandException("Invalid Pilot/Applicant - " + id);

			// Load the Address validation data
			GetAddressValidation avdao = new GetAddressValidation(con);
			av = avdao.get(id);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Make sure we're either anonymous or the same user
		if ((ctx.isAuthenticated()) && (ctx.getUser().getID() != p.getID()))
				throw securityException("Attempting to Validate for " + p.getName());

		// Get the command result
		CommandResult result = ctx.getResult();

		// If there's no validation record on file, then assume we're valid
		if (av == null) {
			result.setURL("/jsp/register/eMailValid.jsp");
			result.setSuccess(true);
			return;
		}

		// If the hashes don't match, then stop us
		if (!av.getHash().equals(ctx.getParameter("code"))) {
			result.setURL("/jsp/register/eMailInvalid.jsp");
			result.setSuccess(true);
			return;
		}

		// Update the e-mail address
		p.setEmail(av.getAddress());

		try {
			Connection con = ctx.getConnection();

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
		} catch (DAOException de) {
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
// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.EMailAddress;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.AddressValidationHelper;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update a registered Pilot's e-mail address.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class UpdateEmailCommand extends AbstractCommand {

	/**
	 * Execute the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occrurs.
	 */
	public void execute(CommandContext ctx) throws CommandException {

		// Get Command results
		CommandResult result = ctx.getResult();

		// Save the plot in the request
		ctx.setAttribute("person", ctx.getUser(), REQUEST);

		// Get the address
		String addr = ctx.getParameter("email");

		try {
			Connection con = ctx.getConnection();

			// Load the address validation object
			GetAddressValidation avdao = new GetAddressValidation(con);
			AddressValidation av = avdao.get(ctx.getUser().getID());
			if (av == null) {
			   ctx.release();
			   
				AddressValidationHelper.clearSessionFlag(ctx.getSession());
				result.setURL("/jsp/register/eMailValid.jsp");
				result.setSuccess(true);
				return;
			}

			if (!EMailAddress.INVALID_ADDR.equals(av.getAddress()))
				ctx.setAttribute("addr", av, REQUEST);
			
			// Determine if we are updating the address
			boolean isSave = "save".equals(ctx.getCmdParameter(OPERATION, null));
			boolean isValidate = "validate".equals(ctx.getCmdParameter(OPERATION, null));
			if (!isSave && !isValidate) {
				ctx.release();
				
				result.setURL("/jsp/register/eMailValidate.jsp");
				result.setSuccess(true);
				return;
			}

			// Get the message template
			MessageContext mctxt = new MessageContext();
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EMAILUPDATE"));

			// Save the e-mail address in the address validation bean and update the hash
			SetAddressValidation avwdao = new SetAddressValidation(con);
			if (isSave) {
				// Check that the e-mail address isn't a dupe
				Pilot usr = new Pilot("!X", "!X");
				usr.setEmail(addr);
				GetPilotDirectory pdao = new GetPilotDirectory(con);
				Collection IDs = pdao.checkUnique(usr, SystemData.get("airline.db"));
				if (!IDs.isEmpty()) {
					ctx.release();
					ctx.setAttribute("dupeAddr", Boolean.TRUE, REQUEST);
					result.setURL("/jsp/register/eMailInvalidate.jsp");
					return;
				}

				// Update the address validation bean
				av.setAddress(addr);
				AddressValidationHelper.calculateHashCode(av);
				mctxt.addData("addrValid", av);
				mctxt.addData("user", ctx.getUser());
				ctx.setAttribute("addr", av, REQUEST);

				// Save the validation hash code
				avwdao.write(av);

				// Send the e-mail message
				Mailer mailer = new Mailer(null);
				mailer.setContext(mctxt);
				mailer.send(Mailer.makeAddress(av.getAddress(), ctx.getUser().getName()));

				// Forward to the JSP
				result.setURL("/jsp/register/eMailValidate.jsp");
				result.setSuccess(true);
			} else if (isValidate) {
				// Get the hash
				String code = AddressValidationHelper.formatHash(ctx.getParameter("code"));

				// If we're updating the address, update the hash and resend
				if (!av.getAddress().equals(addr)) {
					if (!EMailAddress.INVALID_ADDR.equals(addr)) {
						av.setAddress(addr);
						AddressValidationHelper.calculateHashCode(av);
						mctxt.addData("addrValid", av);
						mctxt.addData("user", ctx.getUser());

						// Save the new validation hash code
						avwdao.write(av);

						// Send the e-mail message
						Mailer mailer = new Mailer(null);
						mailer.setContext(mctxt);
						mailer.send(Mailer.makeAddress(av.getAddress(), ctx.getUser().getName()));
					}

					// Set the update attribute
					ctx.setAttribute("addrUpdate", Boolean.TRUE, REQUEST);
					result.setURL("/jsp/register/eMailInvalidate.jsp");
					result.setSuccess(true);
				} else if (!av.getHash().equals(code)) {
					ctx.setMessage("You have entered an incorrect E-Mail address validation code.");
					result.setURL("/jsp/register/eMailValidate.jsp");
				} else {
					// Get the DAO and the Pilot
					GetPilot pdao = new GetPilot(con);
					Pilot p = pdao.get(av.getID());
					if (p == null)
						throw notFoundException("You do not exist!");

					// Update the Pilot's address
					p.setEmail(av.getAddress());
					ctx.getUser().setEmail(av.getAddress());
					
					// Remove the session attribute
					AddressValidationHelper.clearSessionFlag(ctx.getSession());

					// Start a JDBC trasnaction
					ctx.startTX();

					// Delete the address validation bean
					avwdao.delete(ctx.getUser().getID());

					// Update the database
					SetPilot pwdao = new SetPilot(con);
					pwdao.write(p);

					// Commit the transaction
					ctx.commitTX();

					// Goto the success page
					result.setURL("/jsp/register/eMailValid.jsp");
					result.setSuccess(true);
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		result.setType(CommandResult.REQREDIRECT);
	}
}
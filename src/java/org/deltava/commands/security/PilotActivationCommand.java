// Copyright 2005, 2006, 2007, 2008, 2010, 2011, 2015, 2016, 2018, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.*;
import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.PasswordGenerator;
import org.deltava.util.system.SystemData;

import org.gvagroup.common.*;

/**
 * A Web Site Command to reactivate a Pilot.
 * @author Luke
 * @version 8.7
 * @since 1.0
 */

public class PilotActivationCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get command results
		CommandResult result = ctx.getResult();

		// Initialize the Message context
		MessageContext mctx = new MessageContext();
		mctx.addData("user", ctx.getUser());

		// Check if we're forcing
		boolean isFull = false;
		boolean doForce = "force".equals(ctx.getCmdParameter(OPERATION, null));

		Pilot p = null;
		try {
			Connection con = ctx.getConnection();

			// Check if we're full
			if (!doForce) {
				GetStatistics stdao = new GetStatistics(con);
				int size = stdao.getActivePilots(SystemData.get("airline.db"));
				isFull = (size >= SystemData.getInt("users.max", Integer.MAX_VALUE));
				if (isFull)
					ctx.setAttribute("airlineSize", Integer.valueOf(size), REQUEST);
			}

			// Get the DAO and the Pilot profile
			GetPilot dao = new GetPilot(con);
			p = dao.get(ctx.getID());
			if (p == null)
				throw notFoundException("Invalid Pilot - " + ctx.getID());

			// Check our access level
			PilotAccessControl access = new PilotAccessControl(ctx, p);
			access.validate();
			if (!access.getCanActivate())
				throw securityException("Cannot activate Pilot");
			
			// Load our internally hosted domains
			GetPilotEMail pedao = new GetPilotEMail(con);
			ctx.setAttribute("ourDomains", pedao.getDomains(), REQUEST);

			// Get the equipment program
			GetEquipmentType eqdao = new GetEquipmentType(con);
			EquipmentType eq = eqdao.get(p.getEquipmentType());
			ctx.setAttribute("eqType", eq, REQUEST);

			// Save the pilot in the request
			ctx.setAttribute("pilot", p, REQUEST);
			mctx.addData("pilot", p);
			mctx.addData("eqType", eq);
			mctx.addData("CP", dao.get(eq.getCPID()));

			// If we're full and not forcing, redirect to a warning page
			if (isFull && !doForce) {
				ctx.release();
				result.setURL("/jsp/admin/activatePilotFull.jsp");
				result.setSuccess(true);
				return;
			} else if (ctx.getParameter("eMail") == null) {
				ctx.release();
				result.setURL("/jsp/admin/activatePilot.jsp");
				result.setSuccess(true);
				return;
			}

			// Update the pilot status
			p.setEmail(ctx.getParameter("eMail"));
			p.setRank(Rank.fromName(ctx.getParameter("rank")));
			p.setStatus(Pilot.ACTIVE);
			p.setEmailInvalid(false);

			// Reset the password
			p.setPassword(PasswordGenerator.generate(8));

			// Get the Message Template DAO
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctx.setTemplate(mtdao.get("USERACTIVATE"));

			// Create the status update entry
			StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.STATUS_CHANGE);
			upd.setAuthorID(ctx.getUser().getID());
			upd.setDescription("Returned to Active status");

			// Start the transaction
			ctx.startTX();

			// Get the write DAO and save the pilot
			SetPilot pwdao = new SetPilot(con);
			pwdao.write(p);
			
			// Clear address validation
			SetAddressValidation avwdao = new SetAddressValidation(con);
			avwdao.delete(p.getID());

			// Write the inactivity entry
			SetInactivity iwdao = new SetInactivity(con);
			iwdao.setInactivity(p.getID(), 5, true);

			// Write the status update entry
			SetStatusUpdate sudao = new SetStatusUpdate(con);
			sudao.write(upd);

			// Get the authenticator and update the password
			Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR);
			if (auth instanceof SQLAuthenticator) {
				try (SQLAuthenticator sqlAuth = (SQLAuthenticator) auth) {
					sqlAuth.setConnection(con);
					if (auth.contains(p))
						sqlAuth.updatePassword(p, p.getPassword());
					else
						sqlAuth.add(p, p.getPassword());
				}
			} else {
				if (auth.contains(p))
					auth.updatePassword(p, p.getPassword());
				else
					auth.add(p, p.getPassword());
			}

			// Commit the transaction
			ctx.commitTX();
			
			// Get the authenticator and chcek
			if (auth instanceof SQLAuthenticator) {
				try (SQLAuthenticator sqlAuth = (SQLAuthenticator) auth) {
					sqlAuth.setConnection(con);
					sqlAuth.authenticate(p, p.getPassword());
				}
			} else
				auth.authenticate(p, p.getPassword());
			
			// Invalidate the Pilot cache across applications
			EventDispatcher.send(new UserEvent(SystemEvent.Type.USER_INVALIDATE, p.getID()));
		} catch (SecurityException se) {
			ctx.rollbackTX();
			throw new CommandException(se);
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Send e-mail notification
		Mailer mailer = new Mailer(ctx.getUser());
		mailer.setContext(mctx);
		mailer.send(p);

		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/admin/activatePilotComplete.jsp");
		result.setSuccess(true);
	}
}
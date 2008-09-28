// Copyright 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.security;

import java.util.Collection;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.AddressValidation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.AddressValidationHelper;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to update a registered Pilot's e-mail address.
 * @author Luke
 * @version 2.0
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

		// If we're not updating the address, forward to the update JSP
		String addr = ctx.getParameter("email");
		if (StringUtils.isEmpty(addr)) {
			result.setURL("/jsp/pilot/eMailUpdate.jsp");
			result.setSuccess(true);
			return;
		}

		Pilot p = null;
		AddressValidation av = null;
		MessageContext mctxt = new MessageContext();
		try {
			Connection con = ctx.getConnection();
			
			// Get the Pilot
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			p = pdao.get(ctx.getUser().getID());
			
			// Check that the e-mail address isn't a dupe
			Pilot usr = new Pilot("!X", "!X");
			usr.setEmail(addr);
			Collection<Integer> IDs = pdao.checkUnique(usr, SystemData.get("airline.db"));
			IDs.remove(new Integer(p.getID()));
			if (!IDs.isEmpty()) {
				ctx.release();
				ctx.setAttribute("dupeAddr", Boolean.TRUE, REQUEST);
				result.setURL("/jsp/pilot/eMailUpdate.jsp");
				return;
			}
			
			// Load the address validation object
			GetAddressValidation avdao = new GetAddressValidation(con);
			av = avdao.get(p.getID());
			if (av == null)
				av = new AddressValidation(p.getID(), addr);
			else
				av.setAddress(addr);
			
			// Calculate the hash code
			av.setHash(AddressValidationHelper.calculateHashCode(av.getAddress()));
			
			// Save the address validation entry
			SetAddressValidation avwdao = new SetAddressValidation(con);
			avwdao.write(av);

			// Get the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("EMAILUPDATE"));
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set message attributes
		mctxt.addData("addrValid", av);
		mctxt.addData("user", p);
		
		// Send the e-mail message
		EMailAddress newAddr = Mailer.makeAddress(av.getAddress(), p.getName());
		Mailer mailer = new Mailer(SystemData.getBoolean("smtp.testMode") ? newAddr : null);
		mailer.setContext(mctxt);
		mailer.send(newAddr);

		// Set status attribute
		ctx.setAttribute("addr", av, REQUEST);
		ctx.setAttribute("addrUpdate", Boolean.TRUE, REQUEST);
		
		// Forward to the JSP
		result.setType(ResultType.REQREDIRECT);
		result.setURL("/jsp/pilot/eMailInvalidate.jsp");
		result.setSuccess(true);
	}
}
// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.pilot;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.EMailConfiguration;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.PilotAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to create a new IMAP mailbox profile.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IMAPMailboxCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Pilot
			GetPilotDirectory dao = new GetPilotDirectory(con);
            GetPilotEMail edao = new GetPilotEMail(con);
			Pilot usr = dao.get(ctx.getID());
			if (usr == null)
				throw new CommandException("Invalid Pilot - " + ctx.getID());
			
			// Get the Mailbox profile
			EMailConfiguration emailCfg = edao.getEMailInfo(ctx.getID());
			if (emailCfg != null)
				throw new CommandException(usr.getName() + " already has an IMAP mailbox");
            
            // Check our access
            PilotAccessControl access = new PilotAccessControl(ctx, usr);
            access.validate();
            if (!access.getCanChangeMailProfile())
               throw securityException("Cannot create IMAP mailbox");
            
			// Pre-populate the mailbox address
            String mbAddr = usr.getFirstName().toLowerCase();
            if (!edao.isAvailable(mbAddr))
               mbAddr = mbAddr + usr.getLastName().substring(0, 1).toLowerCase();

            // Create the mailbox profile
            emailCfg = new EMailConfiguration(usr.getID(), mbAddr);
            emailCfg.setMailDirectory(String.valueOf(usr.getID()));
            emailCfg.setPassword("12345");
            emailCfg.setQuota(SystemData.getInt("smtp.imap.quota"));
            emailCfg.setActive(true);
            
            // Write the mailbox profile
            SetPilotEMail wdao = new SetPilotEMail(con);
            wdao.write(emailCfg, usr.getName());
            
            // Save context attributes
            ctx.setAttribute("pilot", usr, REQUEST);
            ctx.setAttribute("imap", emailCfg, REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setType(CommandResult.REQREDIRECT);
        result.setURL("/jsp/admin/imapCreated.jsp");
        result.setSuccess(true);
	}
}
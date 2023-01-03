// Copyright 2008, 2012, 2016, 2021, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mail;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.IMAPConfiguration;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display all IMAP mailboxes.
 * @author Luke
 * @version 10.3
 * @since 2.2
 */

public class MailboxListCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
        ViewContext<IMAPConfiguration> vc = initView(ctx, IMAPConfiguration.class);
        try {
        	Connection con = ctx.getConnection();
        	
        	// Load the mailboxes
        	GetPilotEMail idao = new GetPilotEMail(con);
        	idao.setQueryStart(vc.getStart());
        	idao.setQueryMax(vc.getCount());
        	vc.setResults(idao.getAll(ctx.getDB()));
        	
        	// Load the user data
        	Collection<Integer> IDs = vc.getResults().stream().map(IMAPConfiguration::getID).collect(Collectors.toSet());
        	GetUserData uddao = new GetUserData(con);
        	UserDataMap udm = uddao.get(IDs);
        	
        	// Load the Pilots and save the results
        	GetPilot pdao = new GetPilot(con);
        	ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
        } catch (DAOException de) {
        	throw new CommandException(de);
        } finally {
        	ctx.release();
        }

        // Forward to the JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/admin/imapList.jsp");
        result.setSuccess(true);
	}
}
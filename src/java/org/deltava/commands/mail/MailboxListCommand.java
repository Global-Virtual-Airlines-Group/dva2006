// Copyright 2008, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mail;

import java.util.*;
import java.util.stream.Collectors;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.IMAPConfiguration;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display all IMAP mailboxes.
 * @author Luke
 * @version 7.0
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
        	vc.setResults(idao.getAll());
        	
        	// Load the user data
        	Collection<Integer> IDs = vc.getResults().stream().map(IMAPConfiguration::getID).collect(Collectors.toSet());
        	GetUserData uddao = new GetUserData(con);
        	UserDataMap udm = uddao.get(IDs);
        	
        	// Trim out anyone who isn't part of our airline
        	for (Iterator<IMAPConfiguration> i = vc.getResults().iterator(); i.hasNext(); ) {
        		IMAPConfiguration cfg = i.next();
        		Integer id = Integer.valueOf(cfg.getID());
        		UserData ud = udm.get(id);
        		if ((ud == null) || (!ud.getDB().equals(SystemData.get("airline.db")))) {
        			i.remove();
        			udm.remove(id);
        		}
        	}
        	
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
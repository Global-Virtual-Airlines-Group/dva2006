// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.mail;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.system.IMAPConfiguration;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display all IMAP mailboxes.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public class MailboxListCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
        // Get/set start/count parameters
        ViewContext vc = initView(ctx);
        try {
        	Connection con = ctx.getConnection();
        	
        	// Load the mailboxes
        	GetPilotEMail idao = new GetPilotEMail(con);
        	idao.setQueryStart(vc.getStart());
        	idao.setQueryMax(vc.getCount());
        	Collection<IMAPConfiguration> results = idao.getAll();
        	
        	// Load the IDs
        	Collection<Integer> IDs = new HashSet<Integer>();
        	for (Iterator<IMAPConfiguration> i = results.iterator(); i.hasNext(); ) {
        		IMAPConfiguration cfg = i.next();
        		IDs.add(new Integer(cfg.getID()));
        	}
        	
        	// Load the user data
        	GetUserData uddao = new GetUserData(con);
        	UserDataMap udm = uddao.get(IDs);
        	
        	// Trim out anyone who isn't part of our airline
        	for (Iterator<IMAPConfiguration> i = results.iterator(); i.hasNext(); ) {
        		IMAPConfiguration cfg = i.next();
        		UserData ud = udm.get(new Integer(cfg.getID()));
        		if ((ud == null) || (!ud.getDB().equals(SystemData.get("airline.db")))) {
        			i.remove();
        			udm.remove(new Integer(cfg.getID()));
        		}
        	}
        	
        	// Load the Pilots and save the results
        	GetPilot pdao = new GetPilot(con);
        	ctx.setAttribute("pilots", pdao.get(udm), REQUEST);
        	vc.setResults(results);
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
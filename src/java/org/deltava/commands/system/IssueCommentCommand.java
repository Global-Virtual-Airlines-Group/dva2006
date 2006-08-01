// Copyright (c) 2005 Global Virtual Airline Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.IssueAccessControl;

/**
 * A web site command to save new Issue Comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueCommentCommand extends AbstractCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      try {
         Connection con = ctx.getConnection();

         // Load the Issue we are attempting to comment on
         GetIssue rdao = new GetIssue(con);
         Issue i = rdao.get(ctx.getID());

         // Check our access level
         IssueAccessControl access = new IssueAccessControl(ctx, i);
         access.validate();
         if (!access.getCanComment())
        	 throw securityException("Cannot comment on Issue " + ctx.getID());

         // Create the Issue comment bean
         IssueComment ic = new IssueComment(ctx.getParameter("comment"));
         ic.setIssueID(i.getID());
         ic.setAuthorID(ctx.getUser().getID());

         // Initialize the write DAO and write the comment
         SetIssue wdao = new SetIssue(con);
         wdao.writeComment(ic);

         // Check if we're sending this comment via e-mail
         boolean sendComment = Boolean.valueOf(ctx.getParameter("emailComment")).booleanValue();
         if (sendComment) {
            ctx.setAttribute("sendComment", Boolean.TRUE, REQUEST);
            Set<Integer> pilotIDs = new HashSet<Integer>();
            
            // Create and populate the message context
            MessageContext mctx = new MessageContext();
            mctx.addData("issue", i);
            mctx.addData("comment", ic);
            mctx.addData("user", ctx.getUser());
            
            // Check if we're sending to all commenters
            boolean sendAll = Boolean.valueOf(ctx.getParameter("emailAll")).booleanValue();
            if (sendAll) {
            	for (Iterator ci = i.getComments().iterator(); ci.hasNext(); ) {
            		IssueComment c = (IssueComment) ci.next();
            		pilotIDs.add(new Integer(c.getAuthorID()));
            	}
            }

            // Get the Issue creator and assignee, and remove the current user
            pilotIDs.add(new Integer(i.getAuthorID()));
            pilotIDs.add(new Integer(i.getAssignedTo()));
            pilotIDs.remove(new Integer(ctx.getUser().getID()));

            // Get the message template
            GetMessageTemplate mtdao = new GetMessageTemplate(con);
            mctx.setTemplate(mtdao.get("ISSUECOMMENT"));
            
            // Get the user data
            GetUserData uddao = new GetUserData(con);
            UserDataMap udm = uddao.get(pilotIDs);

            // Get the pilot profiles
            Set<Pilot> pilots = new HashSet<Pilot>();
            GetPilot pdao = new GetPilot(con);
            for (Iterator<String> pi = udm.getTableNames().iterator(); pi.hasNext(); ) {
            	String dbTableName = pi.next();
            	if (UserDataMap.isPilotTable(dbTableName)) {
            		Map<Integer, Pilot> pMap = pdao.getByID(udm.getByTable(dbTableName), dbTableName);
            		pilots.addAll(pMap.values());
            	}
            }

            // Create the e-mail message
            Mailer mailer = new Mailer(ctx.getUser());
            mailer.setContext(mctx);
            mailer.send(pilots);
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REDIRECT);
      result.setURL("issue", "read", ctx.getID());
      result.setSuccess(true);
   }
}
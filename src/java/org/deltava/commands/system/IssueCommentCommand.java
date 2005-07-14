package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.system.Issue;
import org.deltava.beans.system.IssueComment;

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
         if (!access.getCanComment()) throw new CommandSecurityException("Cannot comment on Issue " + ctx.getID());

         // Create the Issue comment bean
         IssueComment ic = new IssueComment(ctx.getParameter("comment"));
         ic.setIssueID(i.getID());
         ic.setCreatedBy(ctx.getUser().getID());

         // Initialize the write DAO and write the comment
         SetIssue wdao = new SetIssue(con);
         wdao.writeComment(ic);

         // Check if we're sending this comment via e-mail
         boolean sendComment = Boolean.valueOf(ctx.getParameter("emailComment")).booleanValue();
         if (sendComment) {
            ctx.setAttribute("sendComment", Boolean.valueOf(true), REQUEST);

            // Create and populate the message context
            MessageContext mctx = new MessageContext();
            mctx.addData("issue", i);
            mctx.addData("comment", ic);
            mctx.addData("user", ctx.getUser());

            // Get the Issue creator and assignee, and remove the current user
            Set pilotIDs = new HashSet();
            pilotIDs.add(new Integer(i.getCreatedBy()));
            pilotIDs.add(new Integer(i.getAssignedTo()));
            pilotIDs.remove(new Integer(ctx.getUser().getID()));

            // Get the message template
            GetMessageTemplate mtdao = new GetMessageTemplate(con);
            mctx.setTemplate(mtdao.get("ISSUECOMMENT"));

            // Get the pilot profiles
            GetPilot pdao = new GetPilot(con);
            Map pilots = pdao.getByID(pilotIDs, "PILOTS");

            // Create the e-mail message
            Mailer mailer = new Mailer(ctx.getUser());
            mailer.setContext(mctx);
            mailer.send(pilots.values());
         }
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setType(CommandResult.REQREDIRECT);
      result.setURL("issue", "read", ctx.getID());
      result.setSuccess(true);
   }
}
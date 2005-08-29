// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.system.Issue;
import org.deltava.commands.*;

import org.deltava.dao.GetIssue;
import org.deltava.dao.GetPilot;
import org.deltava.dao.DAOException;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.ComboUtils;

/**
 * A web site command to display issues and comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueListCommand extends AbstractViewCommand {
   
   // Sort options
   private static final List sortOptions = ComboUtils.fromArray(new String[] {"ID", "Priority", "Area", "Status", "Creation Date",
         "Resolution Date", "Last Comment"}, new String[] {"I.ID", "I.PRIORITY DESC", "I.AREA", "I.STATUS DESC", "I.CREATED",
         "I.RESOLVED", "LC DESC"});
    
    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        // Get/set start/count parameters and sort type
        ViewContext vc = initView(ctx);
        vc.setDefaultSortType("ID");
        
        // Get issue status
        int issueStatus = -1;
        if (ctx.getParameter("status") != null) {
            String statusName = ctx.getParameter("status");
            for (int x = 0; x < Issue.STATUS.length; x++) {
                if (Issue.STATUS[x].equals(statusName)) {
                    issueStatus = x;
                    break;
                }
            }
        }
        
        try {
            Connection c = ctx.getConnection();
            
            // Get the DAO
            GetIssue dao = new GetIssue(c);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            
            // If we are getting a user's issues, then grab them
            if (ctx.getID() != 0) {
                vc.setResults(dao.getUserIssues(ctx.getID()));
                
                // Get the user Profile
                GetPilot dao2 = new GetPilot(c);
                ctx.setAttribute("user", dao2.get(ctx.getID()), REQUEST);
            } else if (issueStatus != -1) {
                vc.setResults(dao.getByStatus(issueStatus));
                ctx.setAttribute("status", Issue.STATUS[issueStatus], REQUEST);
            } else {
                vc.setResults(dao.getAll(vc.getSortType()));
            }
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Calculate our access control for creating issues
        IssueAccessControl access = new IssueAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        // Set sort combo lists
        ctx.setAttribute("statuses", ComboUtils.fromArray(Issue.STATUS), REQUEST);
        ctx.setAttribute("sortTypes", sortOptions, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/issue/issueList.jsp");
        result.setSuccess(true);
    }
}
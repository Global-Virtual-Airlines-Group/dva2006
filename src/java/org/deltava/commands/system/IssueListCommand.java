// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
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
import org.deltava.util.StringUtils;

/**
 * A web site command to display issues and comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueListCommand extends AbstractViewCommand {
   
   // Sort options
   private static final String[] SORT_CODE = {"I.ID", "I.PRIORITY DESC", "I.AREA, I.RESOLVED DESC", "I.STATUS DESC", "I.CREATED",
         "I.RESOLVED DESC", "LC DESC"};
   private static final List SORT_OPTIONS = ComboUtils.fromArray(new String[] {"ID", "Priority", "Area", "Status", "Creation Date",
         "Resolution Date", "Last Comment"}, SORT_CODE);
    
    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        // Get/set start/count parameters and sort type
        ViewContext vc = initView(ctx);
        if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
           vc.setSortType(SORT_CODE[0]);
        
        // Get issue status
        int issueStatus = StringUtils.arrayIndexOf(Issue.STATUS, (String) ctx.getCmdParameter(OPERATION, null));
        
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
                GetPilot pdao = new GetPilot(c);
                pdao.setQueryMax(1);
                ctx.setAttribute("user", pdao.get(ctx.getID()), REQUEST);
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
        ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/issue/issueList.jsp");
        result.setSuccess(true);
    }
}
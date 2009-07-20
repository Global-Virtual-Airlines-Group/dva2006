// Copyright 2005, 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.system.Issue;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.*;

/**
 * A Web Site Command to display issues and comments.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class IssueListCommand extends AbstractViewCommand {
   
   private static final String[] SORT_CODE = {"I.ID", "I.PRIORITY DESC", "I.AREA, I.RESOLVED DESC", "I.STATUS DESC", "I.CREATED",
         "I.RESOLVED DESC", "LC DESC"};
   private static final List<?> SORT_OPTIONS = ComboUtils.fromArray(new String[] {"ID", "Priority", "Area", "Status", "Created On",
         "Resolved On", "Last Comment"}, SORT_CODE);
    
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
        
        // Get user ID
        int id = ctx.getID();
        
        // Get issue status/area
        Collection<Issue> results = new ArrayList<Issue>();
        int issueStatus = StringUtils.arrayIndexOf(Issue.STATUS, (String) ctx.getCmdParameter(OPERATION, null));
        int issueArea = StringUtils.arrayIndexOf(Issue.AREA, ctx.getParameter("area"));
        try {
            Connection c = ctx.getConnection();
            
            // Get the DAO
            GetIssue dao = new GetIssue(c);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            
            // If we are getting a user's issues, then grab them
            if (id != 0)
            	results.addAll(dao.getUserIssues(id));
            else if (issueStatus != -1)
                results.addAll(dao.getByStatus(issueStatus, issueArea, vc.getSortType()));
            else
                results.addAll(dao.getAll(vc.getSortType(), issueArea));
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Trim issues we cannot see
        for (Iterator<Issue> i = results.iterator(); i.hasNext(); ) {
        	Issue is = i.next();
        	IssueAccessControl access = new IssueAccessControl(ctx, is);
        	access.validate();
        	if (!access.getCanRead())
        		i.remove();
        }
        
        // Save results
        vc.setResults(results);
        
        // Calculate our access control for creating issues
        IssueAccessControl access = new IssueAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        // Set sort combo lists
        ctx.setAttribute("statusOpts", ComboUtils.fromArray(Issue.STATUS), REQUEST);
        ctx.setAttribute("areaOpts", ComboUtils.fromArray(Issue.AREA), REQUEST);
        ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/issue/issueList.jsp");
        result.setSuccess(true);
    }
}
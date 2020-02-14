// Copyright 2005, 2006, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.system;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.system.Issue;
import org.deltava.beans.system.IssueArea;
import org.deltava.beans.system.IssueStatus;
import org.deltava.commands.*;

import org.deltava.dao.*;

import org.deltava.security.command.IssueAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to display issues and comments.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class IssueListCommand extends AbstractViewCommand {
   
   private static final String[] SORT_CODE = {"I.ID", "I.PRIORITY DESC", "I.AREA, I.RESOLVED DESC", "I.STATUS DESC", "I.CREATED", "I.RESOLVED DESC", "LC DESC"};
   private static final List<?> SORT_OPTIONS = ComboUtils.fromArray(new String[] {"ID", "Priority", "Area", "Status", "Created On", "Resolved On", "Last Comment"}, SORT_CODE);
    
    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    @Override
	public void execute(CommandContext ctx) throws CommandException {

        // Get/set start/count parameters and sort type
        ViewContext<Issue> vc = initView(ctx, Issue.class);
        if (StringUtils.arrayIndexOf(SORT_CODE, vc.getSortType()) == -1)
           vc.setSortType(SORT_CODE[0]);
        
        // Get user ID
        int id = ctx.getID();
        
        // Get issue status/area
        IssueStatus issueStatus = EnumUtils.parse(IssueStatus.class, (String) ctx.getCmdParameter(OPERATION, null), null); 
        IssueArea issueArea = EnumUtils.parse(IssueArea.class, ctx.getParameter("area"), null); 
        try {
            Connection c = ctx.getConnection();
            
            // Get the DAO
            GetIssue dao = new GetIssue(c);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            
            // If we are getting a user's issues, then grab them
            String aCode = ctx.isUserInRole("Developer") ? null : SystemData.get("airline.code");
            if (id != 0)
            	vc.setResults(dao.getUserIssues(id));
            else if (issueStatus != null)
            	vc.setResults(dao.getByStatus(issueStatus, issueArea, vc.getSortType(), aCode));
            else
            	vc.setResults(dao.getAll(vc.getSortType(), issueArea, aCode));
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Trim issues we cannot see
        for (Iterator<Issue> i = vc.getResults().iterator(); i.hasNext(); ) {
        	Issue is = i.next();
        	IssueAccessControl access = new IssueAccessControl(ctx, is);
        	access.validate();
        	if (!access.getCanRead())
        		i.remove();
        }
        
        // Calculate our access control for creating issues
        IssueAccessControl access = new IssueAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        // Set sort combo list
        ctx.setAttribute("sortTypes", SORT_OPTIONS, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/issue/issueList.jsp");
        result.setSuccess(true);
    }
}
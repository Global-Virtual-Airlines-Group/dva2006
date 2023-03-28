// Copyright 2005, 2008, 2009, 2016, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.news;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.Notice;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to display Notices to Airmen (NOTAMs).
 * @author Luke
 * @version 10.6
 * @since 1.0
 */

public class NOTAMCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    @Override
	public void execute(CommandContext ctx) throws CommandException {

        // Get/set start/count parameters
        ViewContext<Notice> vc = initView(ctx, Notice.class);
        boolean doActive = !"all".equals(ctx.getCmdParameter(OPERATION, null));
        try {
        	Connection con = ctx.getConnection();
        	
        	// Get the NOTAMs
            GetNews dao = new GetNews(con);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            vc.setResults(doActive? dao.getActiveNOTAMs() : dao.getNOTAMs());
            
            // Get the authors
            Collection<Integer> IDs = vc.getResults().stream().map(Notice::getAuthorID).collect(Collectors.toSet());
            GetPilot pdao = new GetPilot(con);
            ctx.setAttribute("authors", pdao.getByID(IDs, "PILOTS"), REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Calculate access rights
        Map<Integer, NewsAccessControl> accessMap = new HashMap<Integer, NewsAccessControl>();
        for (Notice n : vc.getResults()) {
        	NewsAccessControl access = new NewsAccessControl(ctx, n);
        	access.validate();
        	accessMap.put(Integer.valueOf(n.getID()), access);
        }
        
        // Save our access
        NewsAccessControl access = new NewsAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        ctx.setAttribute("accessMap", accessMap, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/news/notams.jsp");
        result.setSuccess(true);
    }
}
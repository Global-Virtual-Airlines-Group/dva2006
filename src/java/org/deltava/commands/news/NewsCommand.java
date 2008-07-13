// Copyright 2005, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.news;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.News;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to display the System News.
 * @author Luke
 * @version 2.2
 * @since 1.0
 */

public class NewsCommand extends AbstractViewCommand {

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
            
            // Get the system news
            GetNews dao = new GetNews(con);
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            
            // Get the results
            vc.setResults(dao.getNews());
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Calculate access rights
        Map<Integer, NewsAccessControl> accessMap = new HashMap<Integer, NewsAccessControl>();
        for (Iterator i = vc.getResults().iterator(); i.hasNext(); ) {
        	News n = (News) i.next();
        	NewsAccessControl access = new NewsAccessControl(ctx, n);
        	access.validate();
        	accessMap.put(new Integer(n.getID()), access);
        }
        
        // Save our access
        NewsAccessControl access = new NewsAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        ctx.setAttribute("accessMap", accessMap, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/news/systemNews.jsp");
        result.setSuccess(true);
    }
}
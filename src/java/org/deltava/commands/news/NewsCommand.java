// Copyright 2005, 2008, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.news;

import java.util.*;

import org.deltava.beans.News;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to display the System News.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class NewsCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    @Override
	public void execute(CommandContext ctx) throws CommandException {

        ViewContext<News> vc = initView(ctx, News.class);
        try {
            GetNews dao = new GetNews(ctx.getConnection());
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            vc.setResults(dao.getNews());
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Calculate access rights
        Map<Integer, NewsAccessControl> accessMap = new HashMap<Integer, NewsAccessControl>();
        for (News n : vc.getResults()) {
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
        result.setURL("/jsp/news/systemNews.jsp");
        result.setSuccess(true);
    }
}
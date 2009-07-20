// Copyright 2005, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.news;

import java.util.*;

import org.deltava.beans.Notice;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to display Notices to Airmen (NOTAMs).
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class NOTAMCommand extends AbstractViewCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        // Get/set start/count parameters
        ViewContext vc = initView(ctx);
        boolean doActive = !"all".equals(ctx.getCmdParameter(OPERATION, null));
        try {
            GetNews dao = new GetNews(ctx.getConnection());
            dao.setQueryStart(vc.getStart());
            dao.setQueryMax(vc.getCount());
            vc.setResults(doActive? dao.getActiveNOTAMs() : dao.getNOTAMs());
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Calculate access rights
        Map<Integer, NewsAccessControl> accessMap = new HashMap<Integer, NewsAccessControl>();
        for (Iterator<?> i = vc.getResults().iterator(); i.hasNext(); ) {
        	Notice n = (Notice) i.next();
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
        result.setURL("/jsp/news/notams.jsp");
        result.setSuccess(true);
    }
}
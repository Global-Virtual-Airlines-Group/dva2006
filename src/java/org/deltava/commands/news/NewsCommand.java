package org.deltava.commands.news;

import java.sql.Connection;

import org.deltava.commands.*;

import org.deltava.dao.GetNews;
import org.deltava.dao.DAOException;

import org.deltava.security.command.NewsAccessControl;

/**
 * A Web Site Command to display the System News.
 * @author Luke
 * @version 1.0
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
        
        // Save our access
        NewsAccessControl access = new NewsAccessControl(ctx, null);
        access.validate();
        ctx.setAttribute("access", access, REQUEST);
        
        // Set the result page and return
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/news/systemNews.jsp");
        result.setSuccess(true);
    }
}
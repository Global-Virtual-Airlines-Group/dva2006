package org.deltava.commands.main;

import java.sql.Connection;

import org.deltava.commands.*;
import org.deltava.dao.*;

/**
 * A Web Site Command to display the home page.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HomeCommand extends AbstractCommand {

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error (typically database) occurs
     */
    public void execute(CommandContext ctx) throws CommandException {
        try {
            Connection con = ctx.getConnection();
            
            // Get the system news and save in the request
            GetNews daoN = new GetNews(con);
            daoN.setQueryMax(5);
            ctx.setAttribute("latestNews", daoN.getNews(), REQUEST);
            
            // Get the newest pilots and save in the request
            GetPilot daoP = new GetPilot(con);
            daoP.setQueryMax(10);
            ctx.setAttribute("latestPilots", daoP.getNewestPilots(), REQUEST);
            
            // Get the HTTP statistics and save in the request
            GetSystemData sysdao = new GetSystemData(con);
            ctx.setAttribute("httpStats", sysdao.getHTTPTotals(), REQUEST);
        } catch (DAOException de) {
            throw new CommandException(de);
        } finally {
            ctx.release();
        }
        
        // Redirect to the home page
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/main/home.jsp");
        result.setSuccess(true);
    }
}
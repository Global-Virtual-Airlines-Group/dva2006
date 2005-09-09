package org.deltava.commands.main;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.Person;
import org.deltava.beans.Notice;

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
            GetNews nwdao = new GetNews(con);
            nwdao.setQueryMax(5);
            ctx.setAttribute("latestNews", nwdao.getNews(), REQUEST);
            
            // Get the newest pilots and save in the request
            GetPilot daoP = new GetPilot(con);
            daoP.setQueryMax(10);
            ctx.setAttribute("latestPilots", daoP.getNewestPilots(), REQUEST);
            
            // Get the HTTP statistics and save in the request
            GetSystemData sysdao = new GetSystemData(con);
            ctx.setAttribute("httpStats", sysdao.getHTTPTotals(), REQUEST);
            
            // Get new/active NOTAMs since last login
            if (ctx.isAuthenticated()) {
            	Person usr = ctx.getUser();
            	Collection notams = nwdao.getActiveNOTAMs();
            	for (Iterator i = notams.iterator(); i.hasNext(); ) {
            		Notice ntm = (Notice) i.next();
            		if (ntm.getDate().before(usr.getLastLogin()))
            			i.remove();
            	}
            	
            	ctx.setAttribute("notams", notams, REQUEST);
            }
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
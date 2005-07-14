// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.event;

import java.sql.Connection;
import java.io.IOException;

import java.util.*;
import java.net.*;

import org.apache.log4j.Logger;

import org.deltava.beans.servinfo.*;
import org.deltava.commands.*;

import org.deltava.dao.GetPilotOnline;
import org.deltava.dao.http.GetServInfo;

import org.deltava.util.system.SystemData;

/**
 * A Command to display the "who is online" page.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightBoardCommand extends AbstractCommand {

    private static final Logger log = Logger.getLogger(FlightBoardCommand.class);

    /**
     * Helper method to open a connection to a particular URL.
     */
    private HttpURLConnection getURL(String dataURL) throws IOException {
    	URL url = new URL(dataURL);
    	log.info("Loading data from " + url.toString());
    	return (HttpURLConnection) url.openConnection();
    }

    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
    public void execute(CommandContext ctx) throws CommandException {

        // Get the network name
        String networkName = ctx.getRequest().getParameter("networkName");
        if (networkName == null)
            networkName = SystemData.get("online.default_network");
        
        // Get VATSIM/IVAO members
        Map idMap = null;
        try {
        	// Connect to info URL
        	HttpURLConnection urlcon = getURL(SystemData.get("online." + networkName.toLowerCase() + ".status_url"));
        	
            // Get network status
            GetServInfo sdao = new GetServInfo(urlcon);
            NetworkStatus status = sdao.getStatus(networkName);
            urlcon.disconnect();
            
            // Get network status
            urlcon = getURL(status.getDataURL());
            GetServInfo idao = new GetServInfo(urlcon);
            idao.setBufferSize(32768);
            NetworkInfo info = idao.getInfo(networkName);
            urlcon.disconnect();
        	
            // Get Online Members
        	Connection con = ctx.getConnection();

        	// Get the DAO and execute
        	GetPilotOnline dao = new GetPilotOnline(con);
        	idMap = dao.getIDs(networkName);
        	info.setPilotIDs(idMap);
        	
            // Save the network information in the request
            ctx.setAttribute("netInfo", info, REQUEST);
        } catch (Exception e) {
        	throw new CommandException(e);
        } finally {
        	ctx.release();
        }
        
        // Load the network names and save in the request
        List networkNames = (List) SystemData.getObject("online.networks");
        ctx.setAttribute("networks", networkNames, REQUEST);
        ctx.setAttribute("network", networkName, REQUEST);

        // Forward to the display JSP
        CommandResult result = ctx.getResult();
        result.setURL("/jsp/event/flightBoard.jsp");
        result.setSuccess(true);
    }
}
package org.deltava.servlet;

import java.io.*;
import java.util.*;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.event.*;

import org.deltava.dao.GetEvent;
import org.deltava.dao.DAOException;
import org.deltava.jdbc.ConnectionPool;

import org.deltava.util.URLParser;
import org.deltava.util.StringUtils;
import org.deltava.beans.system.VersionInfo;

/**
 * A servlet to download Online Event flight plans.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class FlightPlanServlet extends GenericServlet {

    private static final Logger log = Logger.getLogger(FlightPlanServlet.class);
    
    /**
     * Returns the servlet description.
     * @return name, author and copyright info for this servlet
     */
    public String getServletInfo() {
        return "Flight Plan Servlet " + VersionInfo.TXT_COPYRIGHT;
    }
    
    /**
     * Helper method to retrieve a flight plan from an Event bean.
     */
    private FlightPlan getPlan(Event e, String fName) {
    	for (Iterator i = e.getPlans().iterator(); i.hasNext(); ) {
    		FlightPlan fp = (FlightPlan) i.next();
    		if (fp.getFileName().equals(fName))
    			return fp;
    	}
    	
    	return null;
    }
    
    /**
     * Processes HTTP GET requests for Online Event Flight Plans. <i>Currently not implemented</i>.
     * @param req the HTTP request
     * @param rsp the HTTP response
     * @throws IOException if a network I/O error occurs
     */
    public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
    	
    	// Parse the URL to determine what we want
    	URLParser url = new URLParser(req.getRequestURI());
    	int eventID = StringUtils.parseHex(url.getLastPath());
    	
        // Get the connection pool
        ConnectionPool jdbcPool = getConnectionPool();
        
        // Get the Event
        Connection c = null;
        Event e = null;
        try {
        	c = jdbcPool.getConnection();
        	
        	// Get the DAO and the event
        	GetEvent dao = new GetEvent(c);
        	e = dao.get(eventID);
        } catch (DAOException de) {
        	log.error("Error retrieving Flight Plan - " + de.getMessage());
        } finally {
        	jdbcPool.release(c);
        }
        
        // Check if we got an event
        if (e == null) {
        	log.error("Cannot find Online Event " + url.getLastPath());
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Search through the flight plans
        FlightPlan plan = getPlan(e, url.getFileName());
        if (plan == null) {
        	log.error("Cannot find Flight Plan " + url.getFileName());
        	rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
    	
        // Set the response headers
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentLength(plan.getSize());
        rsp.setBufferSize(8192);
        
        // Prompt the browser to save the file
        rsp.setHeader("Content-disposition", "attachment; filename=" + url.getFileName());
        
        // Dump the data to the output stream
        try {
        	InputStream in = plan.getInputStream(); 
            OutputStream out = rsp.getOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead = in.read(buffer, 0, 8192);
            while (bytesRead != -1) {
            	out.write(buffer);
            	bytesRead = in.read(buffer, 0, 8192);
            }

            out.flush();
            rsp.flushBuffer();
            out.close();
        } catch (IOException ie) {
            log.error("Error writing Flight Plan - " + ie.getMessage());
        }
    }
}
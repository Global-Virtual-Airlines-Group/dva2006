package org.deltava.servlet;

import java.io.*;
import java.sql.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.Pilot;
import org.deltava.beans.system.VersionInfo;

import org.deltava.jdbc.ConnectionPool;

import org.deltava.dao.*;
import org.deltava.util.*;

/**
 * The Image serving Servlet. This serves all database-contained images.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ImageServlet extends BasicAuthServlet {

    private static final Logger log = Logger.getLogger(ImageServlet.class);
    private static final String IMG_REALM = "\"DVA Approach Charts\"";
    
    private static final int IMG_CHART = 0;
    private static final int IMG_GALLERY = 1;
    private static final int IMG_SIG = 2;

    private static final String[] IMG_TYPES = { "charts", "gallery", "sig" };

    /**
     * Returns the servlet description.
     * @return name, author and copyright info for this servlet
     */
    public String getServletInfo() {
        return "Database Image Servlet "  + VersionInfo.TXT_COPYRIGHT;
    }

    /**
     * A helper method to get the image type from the URL.
     */
    private int getImageType(URLParser up) {
        for (int x = 0; x < IMG_TYPES.length; x++) {
            if (up.containsPath(IMG_TYPES[x]))
                return x;
        }

        return -1;
    }
    
    /**
     * Processes HTTP GET requests for images.
     * @param req the HTTP request
     * @param rsp the HTTP response
     * @throws IOException if a network I/O error occurs
     */
    public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {

        // Parse the URL to figure out what kind of image we want
        URLParser url = new URLParser(req.getRequestURI());
        int imgType = getImageType(url);
        if (imgType == -1) {
            log.warn("Invalid Image type - " + url.getLastPath());
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // If we're loading a chart, make sure we are authenticated
        if (imgType == IMG_CHART) {
        	Pilot usr = (Pilot) req.getUserPrincipal();
    		if (usr == null)
    			usr = authenticate(req);
    		
    		//	Check if we need to be authenticated
    		if (usr == null) {
    			challenge(rsp, IMG_REALM);
    			return;
    		}
        }

        // Get the image ID
        int imgID = 0;
        try {
            imgID = StringUtils.parseHex(url.getName());
        } catch (Exception e) {
            log.warn("Error parsing ID " + url.getName() + " - " + e.getClass().getName());
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Get the connection pool
        ConnectionPool jdbcPool = getConnectionPool();

        byte[] imgBuffer = null;
        log.debug("Getting " + IMG_TYPES[imgType] + " image ID" + String.valueOf(imgID));
        Connection c = null;
        try {
            c = jdbcPool.getConnection();
            String dbName = null;

            // Get the retrieve image DAO and execute the right method
            GetImage dao = new GetImage(c);
            switch (imgType) {
            case IMG_CHART:
                imgBuffer = dao.getChart(imgID);
                break;

            case IMG_GALLERY:
            	dbName = url.getLastPath();
                imgBuffer = dao.getGalleryImage(imgID, dbName);
                break;

            case IMG_SIG:
            	dbName = url.getLastPath();
                imgBuffer = dao.getSignatureImage(imgID, dbName);
                break;

            default:
            }
        } catch (DAOException de) {
        	log.error("Error retrieving image - " + de.getMessage());
        } finally {
            jdbcPool.release(c);
        }

        // If we got nothing, then throw an error
        if (imgBuffer == null) {
            log.error("Cannot find image " + url.getLastPath() + "/" + imgID);
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Get the image type
        ImageInfo info = new ImageInfo(imgBuffer);
        if (!info.check()) {
            rsp.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        // Set the content-type and content length
        rsp.setStatus(HttpServletResponse.SC_OK);
        rsp.setContentType(info.getMimeType());
        rsp.setContentLength(imgBuffer.length);
        rsp.setBufferSize((imgBuffer.length > 65520) ? 65520 : imgBuffer.length);

        // Dump the data to the output stream
        try {
            OutputStream out = rsp.getOutputStream();
            out.write(imgBuffer);
            rsp.flushBuffer();
            out.close();
        } catch (IOException ie) {
            log.error("Error writing image - " + ie.getMessage());
        }
    }
}
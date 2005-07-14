// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.sql.Connection;

import javax.servlet.http.*;

import org.apache.log4j.Logger;

import org.deltava.beans.testing.CheckRide;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.GetExam;
import org.deltava.dao.DAOException;
import org.deltava.jdbc.ConnectionPool;

import org.deltava.security.command.ExamAccessControl;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A servlet to support the downloading of Check Ride videos.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class VideoServlet extends GenericServlet {

   private static final Logger log = Logger.getLogger(VideoServlet.class);
   private static final int BUFFER_SIZE = 102400;
   
   /**
    * Returns the servlet description.
    * @return name, author and copyright info for this servlet
    */
   public String getServletInfo() {
       return "Check Ride Video Servlet " + VersionInfo.TXT_COPYRIGHT;
   }
   
   /**
    * Private helper method to get a File handle to the requested resource. 
    */
   private File getResource(String fileName) {
       return new File(SystemData.get("path.video"), fileName);
   }
   
   /**
    * Processes HTTP GET requests for Check Ride videos.
    * @param req the HTTP request
    * @param rsp the HTTP response
    * @throws IOException if a network I/O error occurs
    */
   public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
      
      // Get the image ID
      URLParser url = new URLParser(req.getRequestURI());
      int examID = 0;
      try {
          examID = StringUtils.parseHex(url.getName());
      } catch (Exception e) {
          log.warn("Error parsing ID " + url.getName() + " - " + e.getClass().getName());
          rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
          return;
      }
      
      // Get the connection pool
      ConnectionPool jdbcPool = getConnectionPool();
      
      // Get a connection to the database
      CheckRide cr = null;
      Connection con = null;
      try {
         con = jdbcPool.getConnection();
         
         // Get the DAO and retrieve the check ride
         GetExam dao = new GetExam(con);
         cr = dao.getCheckRide(examID);
      } catch (DAOException de) {
         log.error("Error retrieving video - " + de.getMessage());
      } finally {
         jdbcPool.release(con);
      }
      
      // Check that we retrieved a checkride
      if (cr == null) {
         log.error("Cannot find check ride " + url.getLastPath() + "/" + examID);
         rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
         return;
      }
      
      // Validate access to the video
      ServletSecurityContext sctxt = new ServletSecurityContext(req);
      ExamAccessControl access = new ExamAccessControl(sctxt, cr);
      try {
         access.validate();
      } catch (Exception e) {
         rsp.sendError(403);
         return;
      }
      
      // Check that there's a file attached
      if (cr.getFileName() == null) {
         log.error("No file attached to checkride " + examID);
         rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
         return;
      }
      
      // Get the resource
      File f = getResource(cr.getFileName());
      if (!f.exists()) {
         rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
         return;
      }
      
      // Set the response headers
      rsp.setBufferSize(BUFFER_SIZE);
      rsp.setStatus(HttpServletResponse.SC_OK);
      rsp.setContentLength((int) f.length());
      rsp.setContentType("application/octet-stream");

      // Prompt the browser to save the file
      rsp.setHeader("Content-disposition", "attachment; filename=" + cr.getFileName());
      
      // Stream the file
      long startTime = System.currentTimeMillis();
      try {
         byte[] buf = new byte[BUFFER_SIZE];
         InputStream is = new FileInputStream(f);
         OutputStream out = rsp.getOutputStream();
         int bytesRead = is.read(buf, 0, BUFFER_SIZE);
         while (bytesRead != -1) {
            out.write(buf, 0, bytesRead);
            bytesRead = is.read(buf, 0, BUFFER_SIZE);
         }

         is.close();
         out.flush();
      } catch (IOException ie) {
         log("Error streaming " + f.getAbsolutePath() + " - " + ie.getMessage());
         throw ie;
      }

      // Close the file and log download time
      long totalTime = System.currentTimeMillis() - startTime;
      log.info(f.getName().toLowerCase() + " download complete, " + (totalTime / 1000) + "s, "
            + (f.length() * 1000 / totalTime) + " bytes/sec");
   }
}
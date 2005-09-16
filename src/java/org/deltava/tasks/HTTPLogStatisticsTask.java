// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.tasks;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.stats.HTTPStatistics;

import org.deltava.dao.SetSystemLog;
import org.deltava.dao.DAOException;

import org.deltava.taskman.DatabaseTask;

import org.deltava.util.StringUtils;
import org.deltava.util.log.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to aggregate HTTP log statistics.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class HTTPLogStatisticsTask extends DatabaseTask {
   
   protected static final Logger log = Logger.getLogger(HTTPLogStatisticsTask.class);
   
   private class HTTPLogFilter implements FileFilter {
      
      private Calendar _startTime;
      
      HTTPLogFilter() {
         super();
         _startTime = Calendar.getInstance();
         _startTime.set(Calendar.HOUR_OF_DAY, 0);
         _startTime.set(Calendar.MINUTE, 0);
         _startTime.set(Calendar.SECOND, 0);
         _startTime.add(Calendar.SECOND, -1);
      }
      
      public boolean accept(File f) {
         
         // Ensure that we start with httpd-access
         String name = f.getName();
         if (!name.startsWith(SystemData.get("log.http.format")))
            return false;
         
         try {
            String ext = name.substring(name.lastIndexOf('.') + 1);
            Date d = new Date(Long.parseLong(ext) * 1000);
            log.info("Cutoff date = " + _startTime.getTime() + " log date=" + d);
            return d.before(_startTime.getTime());
         } catch (Exception e) {
            return false;
         }
      }
   }

   /**
    * Initializes the task.
    */
   public HTTPLogStatisticsTask() {
      super("HTTP Log Statistics");
   }

   /**
    * Executes the Task. This will parse through HTTP server log entries and aggregate the statistics.
    */
   protected void execute() {
      
      // Get the HTTP log path
      File logPath = new File(SystemData.get("path.httplog"));
      if (!logPath.exists()) {
         log.error("Cannot find HTTP log path " + logPath.getAbsolutePath());
         return;
      }
      
      // Initialize the Log parser
      LogParser parser = initParser(SystemData.get("log.http.parser"));
      if (parser == null)
         return;
      
      // Get the write DAO
      SetSystemLog dao = new SetSystemLog(_con);

      // Look for logs
      Collection files = Arrays.asList(logPath.listFiles(new HTTPLogFilter()));
      for (Iterator i = files.iterator(); i.hasNext(); ) {
         File f = (File) i.next();
         HTTPStatistics stats = parser.parseLog(f);
         if (stats != null) {
            log.info("Updating statistics for " + StringUtils.format(stats.getDate(), "MM/dd/yyyy"));
            try {
               dao.write(stats);
               if (!f.delete()) {
                  log.error("Cannot delete " + f.getName());
               } else {
            	   log.info("Deleted " + f.getAbsolutePath());
               }
            } catch (DAOException de) {
               log.error("Error saving statistics for " + StringUtils.format(stats.getDate(), "MM/dd/yyyy") + " - "
                     + de.getMessage(), de);
            }
         }
      }
      
      // Log completion
      log.info("Processing Complete");
   }
   
   /**
    * Helper method to init the log parser.
    */
   private LogParser initParser(String className) {
      try {
         Class c = Class.forName(className);
         return (LogParser) c.newInstance();
      } catch (Exception e) {
         log.error("Cannot load " + className + " - " + e.getClass().getName(), e);
      } 
      
      return null;
   }
}
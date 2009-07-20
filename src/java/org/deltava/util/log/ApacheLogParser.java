// Copyright 2005, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.log;

import java.io.*;
import java.util.Date;

import org.apache.log4j.Logger;

import org.deltava.beans.stats.HTTPStatistics;

/**
 * A Log Parser for Apache 2.0 common access logs. <i>This requires that the log be in the Apache 2.x 
 * format &quot;%h %u %t \"%r\" %>s %B %D&quot;.</i>
 * @author Luke
 * @version 2.6
 * @since 1.0
 */

public class ApacheLogParser implements LogParser {
   
   private static final Logger log = Logger.getLogger(ApacheLogParser.class);
   
   private class LogTokenizer {
      
      private String _entry;
      private int _pos;
      
      public LogTokenizer(String entry) {
         super();
         _entry = entry;
      }
      
      private boolean checkEnd() {
         return (_pos >= _entry.length());
      }
      
      public String nextDate() {
         if (checkEnd())
            return null;
         
         // Make sure it's really a date
         if (_entry.charAt(_pos) != '[')
            return nextToken();
         
         _pos++;
         StringBuilder buf = new StringBuilder();
         while ((_pos < _entry.length()) && (_entry.charAt(_pos) != ']')) {
            buf.append(_entry.charAt(_pos));
            _pos++;
         }
         
         _pos += 2;
         return buf.toString();
      }
      
      public String nextToken() {
         if (checkEnd())
            return null;
         
         // Check if we have quotes and spaces
         boolean isQuote = (_entry.charAt(_pos) == '\"');
         if (isQuote)
            _pos++;
         
         StringBuilder buf = new StringBuilder();
         while ((_pos < _entry.length()) && (_entry.charAt(_pos) != ((isQuote) ? '\"' : ' '))) {
            buf.append(_entry.charAt(_pos));
            _pos++;
         }
         
         // If we have a quote at the end, then add two
         _pos += ((isQuote) ? 2 : 1); 
         return buf.toString();
      }
   }

   /**
    * Parses an Apache 2.0 common log file.
    * @param f the log file
    * @return the HTTP statistics for that log period
    */
   public HTTPStatistics parseLog(File f) {
      
      // Init counters
      int totalHits = 0;
      int homeHits = 0;
      int execTime = 0;
      int beTime = 0;
      long bandwidth = 0;
      
      try {
         LineNumberReader br = new LineNumberReader(new FileReader(f), 40960);         
         while (br.ready()) {
            String logEntry = br.readLine();
            
            // Parse the entry
            
            LogTokenizer ltk = new LogTokenizer(logEntry);
            ltk.nextToken(); // IP address
            ltk.nextToken(); // User ID
            ltk.nextDate(); // date/time
            String url = ltk.nextToken();
            ltk.nextToken(); // Status Code

            // Get size and status code
            int size = 0;
            long time = 0;
            try {
               size = Integer.parseInt(ltk.nextToken());
               time = Long.parseLong(ltk.nextToken()) / 1000;
            } catch (Exception e) {
            	// empty
            }
            
            // Increment counters
            totalHits++;
            execTime += time;
            bandwidth += size;
            if (url != null) {
               if (url.startsWith("GET / ") || url.startsWith("GET /home.do"))
                  homeHits++;
            
               if (url.indexOf(".do") != -1)
                  beTime += time;
            }
         }
         
         // Close the reader
         br.close();
      } catch (Exception e) {
         log.error("Error loading " + f.getAbsolutePath(), e);
         return null;
      }
      
      // Get the date of the log file
      String ext = f.getName().substring(f.getName().lastIndexOf('.') + 1);
      HTTPStatistics results = new HTTPStatistics(new Date(Long.parseLong(ext) * 1000));
      results.setExecutionTime(execTime);
      results.setBackEndTime(beTime);
      results.setBandwidth(bandwidth);
      results.setRequests(totalHits);
      results.setHomePageHits(homeHits);
      return results;
   }
}
// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;

import org.deltava.beans.acars.ACARSLogEntry;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A helper class for viewing ACARS logs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class ACARSLogViewCommand extends AbstractViewCommand {
   
   private static final String[] SEARCH_TYPES = new String[] { "USR", "id", "DATE", "LATEST" };
   protected static final int SEARCH_USR = 0;
   protected static final int SEARCH_ID = 1;
   protected static final int SEARCH_DATE = 2;
   protected static final int SEARCH_LATEST = 3;
   private static final List _searchTypes = ComboUtils.fromArray(new String[] { "Pilot ID", "User ID", "Date Range", 
         "Latest Entries" }, SEARCH_TYPES);
   
   /**
    * Calculates the search type from the request, and updates request attributes.
    * @param ctx the Command context
    * @return a search type constant
    */
   protected int getSearchType(CommandContext ctx) {
      int searchType = StringUtils.arrayIndexOf(SEARCH_TYPES, ctx.getParameter("searchType"));
      if (searchType == -1)
      	searchType = SEARCH_LATEST;
      
      ctx.setAttribute("searchType", SEARCH_TYPES[searchType], REQUEST);
      ctx.setAttribute("searchTypes", _searchTypes, REQUEST);
      return searchType;
   }
   
   /**
    * Returns a Set of Pilot IDs from the view results.
    * @param viewEntries the view result entries
    * @return a Set of Pilot IDs
    */
   protected Set getPilotIDs(Collection viewEntries) {
      Set results = new HashSet();
      for (Iterator i = viewEntries.iterator(); i.hasNext(); ) {
         ACARSLogEntry entry = (ACARSLogEntry) i.next();
         results.add(new Integer(entry.getPilotID()));
      }
      
      return results;
   }
   
   /**
    * Validates that a Pilot Code contains a valid database name.
    * @param pCode the Pilot Code
    * @return TRUE if the Pilot Code starts with a valid database, otherwise FALSE
    */
   protected boolean validatePilotCode(String pCode) {
      
      // Get the airline codes
      Map apps = (Map) SystemData.getObject("apps");
      for (Iterator i = apps.values().iterator(); i.hasNext(); ) {
         AirlineInformation info = (AirlineInformation) i.next();
         if (pCode.toUpperCase().startsWith(info.getCode().toUpperCase()))
            return true;
      }
      
      return false;
   }
}
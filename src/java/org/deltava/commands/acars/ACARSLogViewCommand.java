// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;

import org.deltava.beans.acars.ACARSLogEntry;
import org.deltava.commands.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.StringUtils;

/**
 * A helper class for viewing ACARS logs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public abstract class ACARSLogViewCommand extends AbstractViewCommand {
   
   private static final String[] SEARCH_TYPES = new String[] { "USR", "id", "DATE" };
   protected static final int SEARCH_USR = 0;
   protected static final int SEARCH_ID = 1;
   protected static final int SEARCH_DATE = 2;
   private static final List _searchTypes = ComboUtils.fromArray(new String[] { "Pilot ID", "User ID", "Date Range" },
         SEARCH_TYPES);
   
   /**
    * Calculates the search type from the request, and updates request attributes.
    * @param ctx the Command context
    * @return a search type constant
    */
   protected int getSearchType(CommandContext ctx) {
      int searchType = StringUtils.arrayIndexOf(SEARCH_TYPES, ctx.getParameter("searchType"));
      if (searchType == -1)
      	searchType = SEARCH_USR;
      
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
}
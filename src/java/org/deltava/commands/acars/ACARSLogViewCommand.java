// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;
import java.text.*;

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
   
   private static final DateFormat _df = new SimpleDateFormat("MM/dd/yyyy HH:mm");
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
      	searchType = SEARCH_ID;
      
      ctx.setAttribute("searchType", SEARCH_TYPES[searchType], REQUEST);
      ctx.setAttribute("searchTypes", _searchTypes, REQUEST);
      return searchType;
   }
   
   /**
    * Loads a date parameter from the request.
    * @param ctx the Command context
    * @param paramName the request parameter name
    * @return the parsed date/time or null if it cannot be parsed
    */
   protected Date getDate(CommandContext ctx, String paramName) {
      
      try {
         synchronized (_df) {
            return _df.parse(ctx.getParameter(paramName));
         }
      } catch (ParseException pe) {
         return null;
      }
   }
}

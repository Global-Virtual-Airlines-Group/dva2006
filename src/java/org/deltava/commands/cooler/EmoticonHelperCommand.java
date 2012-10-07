// Copyright 2005, 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;

import org.deltava.beans.cooler.Emoticons;

import org.deltava.commands.*;

/**
 * A Web Site Command to display Water Cooler emoticons.
 * @author Luke
 * @version 5.0
 * @since 1.0
 */

public class EmoticonHelperCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 */
	@Override
   public void execute(CommandContext ctx) {

      // Convert smiley codes into a map
      Map<String, String> codes = new LinkedHashMap<String, String>();
      for (Emoticons ei : Emoticons.values()) {
         if (ei.getCode() != null)
            codes.put(ei.getName(), ei.getCode());
      }
      
      // Save the smiley codes
      ctx.setAttribute("iconCodes", codes, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/cooler/emoticonHelper.jsp");
      result.setSuccess(true);
   }
}
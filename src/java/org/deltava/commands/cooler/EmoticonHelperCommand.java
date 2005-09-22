// Copyright 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;

import org.deltava.beans.cooler.Emoticons;
import org.deltava.commands.*;


/**
 * A Web Site Command to display Water Cooler emoticons.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class EmoticonHelperCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
   public void execute(CommandContext ctx) throws CommandException {

      // Save Cooler image names in the request
      ctx.setAttribute("iconNames", Arrays.asList(Emoticons.ICON_NAMES), REQUEST);

      // Convert smiley codes into a map
      Map codes = new HashMap();
      for (int x = 0; x < Emoticons.ICON_NAMES.length; x++) {
         if (Emoticons.ICON_CODES[x] != null)
            codes.put(Emoticons.ICON_NAMES[x], Emoticons.ICON_CODES[x]);
      }
      
      // Save the smiley codes
      ctx.setAttribute("iconCodes", codes, REQUEST);
      
      // Forward to the JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/cooler/emoticonHelper.jsp");
      result.setSuccess(true);
   }
}
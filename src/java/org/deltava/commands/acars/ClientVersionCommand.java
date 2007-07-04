// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;

import org.deltava.commands.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.acars.ACARSClientInfo;
import org.gvagroup.common.SharedData;

/**
 * A Web Site Command to control minimum ACARS client versions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ClientVersionCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Ensure ACARS is enabled
		if (!SystemData.getBoolean("acars.enabled"))
			throw notFoundException("ACARS Server not enabled");
		
		// Load the client versions
		ACARSClientInfo cInfo = (ACARSClientInfo) SharedData.get(SharedData.ACARS_CLIENT_BUILDS);
		Collection<String> versions = cInfo.getVersions();
		
		// Get Command results
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("latestBuild") == null) {
			Map<String, Integer> versionMap = new HashMap<String, Integer>();
			for (Iterator<String> i = versions.iterator(); i.hasNext(); ) {
				String ver = i.next();
				versionMap.put(ver, new Integer(cInfo.getMinimumBuild(ver)));
			}
			
			// Save in the request
			ctx.setAttribute("versions", versions, REQUEST);
			ctx.setAttribute("versionInfo", versionMap, REQUEST);
			ctx.setAttribute("latestBuild", new Integer(cInfo.getLatest()), REQUEST);
			
			// Redirect to the JSP
			result.setURL("/jsp/acars/clientVersion.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the minimum/latest client builds
		cInfo.setLatest(StringUtils.parse(ctx.getParameter("latestBuild"), cInfo.getLatest()));
		for (Iterator<String> i = versions.iterator(); i.hasNext(); ) {
			String ver = i.next();
			String paramName = "min_" + ver.replace('.', '_') + "_build";
			cInfo.setMinimumBuild(ver, StringUtils.parse(ctx.getParameter(paramName), cInfo.getMinimumBuild(ver)));
		}
		
		// Return to success page
		result.setType(CommandResult.REDIRECT);
		result.setURL("acarsversion.do");
		result.setSuccess(true);
	}
}
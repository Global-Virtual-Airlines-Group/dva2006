// Copyright 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 2.3
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
		Collection<Integer> betas = cInfo.getBetas();
		
		// Get Command results
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("latestBuild") == null) {
			Map<String, Integer> versionMap = new LinkedHashMap<String, Integer>();
			for (Iterator<String> i = versions.iterator(); i.hasNext(); ) {
				String ver = i.next();
				versionMap.put(ver, Integer.valueOf(cInfo.getMinimumBuild(ver)));
			}
			
			// Get beta builds
			Map<Integer, Integer> betaMap = new TreeMap<Integer, Integer>();
			for (Iterator<Integer> i = betas.iterator(); i.hasNext(); ) {
				Integer build = i.next();
				betaMap.put(build, Integer.valueOf(cInfo.getMinimumBetaBuild(build.intValue())));
			}
			
			// Save in the request
			ctx.setAttribute("versionInfo", versionMap, REQUEST);
			ctx.setAttribute("betaInfo", betaMap, REQUEST);
			ctx.setAttribute("noDispatch", cInfo.getNoDispatchBuilds(), REQUEST);
			ctx.setAttribute("latestBuild", Integer.valueOf(cInfo.getLatest()), REQUEST);
			ctx.setAttribute("latestDispatch", Integer.valueOf(cInfo.getMinimumDispatchBuild()), REQUEST);
			
			// Redirect to the JSP
			result.setURL("/jsp/acars/clientVersion.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the minimum/latest client builds
		cInfo.setLatest(StringUtils.parse(ctx.getParameter("latestBuild"), cInfo.getLatest()));
		cInfo.setMinimumDispatchBuild(StringUtils.parse(ctx.getParameter("latestDispatch"), cInfo.getMinimumDispatchBuild()));
		for (Iterator<String> i = versions.iterator(); i.hasNext(); ) {
			String ver = i.next();
			String paramName = "min_" + ver.replace('.', '_') + "_build";
			cInfo.setMinimumBuild(ver, StringUtils.parse(ctx.getParameter(paramName), cInfo.getMinimumBuild(ver)));
		}
		
		// Get the minimum beta versions
		for (Iterator<Integer> i = betas.iterator(); i.hasNext(); ) {
			Integer build = i.next();
			String paramName = "min_" + build.toString() + "_beta";
			cInfo.setMinimumBetaBuild(build.intValue(), StringUtils.parse(ctx.getParameter(paramName), 0));
		}
		
		// Add new beta build
		int newBuild = StringUtils.parse("newBuild", 0);
		if (newBuild > 0) {
			int newBeta = StringUtils.parse("newBeta", 0);
			if (newBeta > 0)
				cInfo.setMinimumBetaBuild(newBuild, newBeta);
		}
		
		// Get the ACARS client versions that cannot request dispatch
		List<String> noDsp = StringUtils.split(ctx.getParameter("noDispatch"), ",");
		cInfo.setNoDispatchBuilds(new HashSet<Integer>());
		for (Iterator<String> i = noDsp.iterator(); i.hasNext(); )
			cInfo.addNoDispatchBuild(StringUtils.parse(i.next(), 0));
		
		// Return to success page
		result.setType(ResultType.REDIRECT);
		result.setURL("acarsversion.do");
		result.setSuccess(true);
	}
}
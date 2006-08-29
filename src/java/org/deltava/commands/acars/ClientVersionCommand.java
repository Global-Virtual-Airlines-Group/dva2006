// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.acars;

import java.util.*;

import org.deltava.commands.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to control minimum ACARS client versions.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ClientVersionCommand extends AbstractCommand {
	
	public class VersionKey implements Comparable {
		
		private String _version;
		private String _key;
		private int _minBuild;
		
		protected VersionKey(String key, int minVersion) {
			super();
			_key = key;
			_version = StringUtils.replace(key, "_", ".");
			_minBuild = minVersion;
		}
		
		public String getKey() {
			return _key;
		}
		
		public String getVersion() {
			return _version;
		}
		
		public int getMinBuild() {
			return _minBuild;
		}
		
		public int compareTo(Object o) {
			VersionKey v2 = (VersionKey) o;
			Double v1 = new Double(_version.substring(1));
			return v1.compareTo(new Double(v2._version.substring(1)));
		}
	}

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@SuppressWarnings("unchecked")
	public void execute(CommandContext ctx) throws CommandException {
		
		// Ensure ACARS is enabled
		if (!SystemData.getBoolean("acars.enabled"))
			throw notFoundException("ACARS Server not enabled");
		
		// Load the client versions
		Map builds = (Map) SystemData.getObject("acars.build.minimum");
		Collection<VersionKey> versions = new TreeSet<VersionKey>();
		for (Iterator i = builds.keySet().iterator(); i.hasNext(); ) {
			String verKey = (String) i.next();
			versions.add(new VersionKey(verKey, Integer.parseInt((String) builds.get(verKey))));
		}
		
		// Get Command results
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("latestBuild") == null) {
			ctx.setAttribute("versions", versions, REQUEST);
			
			// Redirect to the JSP
			result.setURL("/jsp/acars/clientVersion.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the minimum/latest client builds
		try {
			SystemData.add("acars.build.latest", new Integer(ctx.getParameter("latestBuild")));
			for (Iterator<VersionKey> i = versions.iterator(); i.hasNext(); ) {
				VersionKey ver = i.next();
				String paramName = "min_" + ver.getKey() + "_build";
				builds.put(ver.getKey(), new Integer(ctx.getParameter(paramName)).toString());
			}
		} catch (NumberFormatException nfe) {
			ctx.setMessage(nfe.getMessage());
		}
		
		// Return to success page
		ctx.setMessage("Client Versions updated");
		result.setType(CommandResult.REDIRECT);
		result.setURL("acarsversion.do");
		result.setSuccess(true);
	}
}
// Copyright 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simfdr;

import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.util.*;

import org.deltava.service.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service that authenticates using the simFDR user account. 
 * @author Luke
 * @version 7.0
 * @since 7.0
 */

abstract class SimFDRService extends WebService {

	/**
	 * Authenticates the simFDR User.
	 * @param ctx the ServiceContext
	 */
	protected void authenticate(ServiceContext ctx) throws ServiceException {
		
		// Check for Authentication header
		String authHdr = ctx.getRequest().getHeader("Authorization");
		if ((authHdr == null) || (!authHdr.toUpperCase().startsWith("BASIC "))) {
			ctx.setHeader("WWW-Authenticate", "Basic realm=\"simFDR Services\"");
			throw error(SC_UNAUTHORIZED, "Invalid Credentials", false);
		}
		
		// Get encoded username/password, and decode them
		Base64.Decoder b64d = Base64.getDecoder();
		String userPwd = new String(b64d.decode(authHdr.substring(6)));
		StringTokenizer tkns = new StringTokenizer(userPwd, ":");
		if (tkns.countTokens() != 2)
			throw error(SC_UNAUTHORIZED, "Invalid Credentials", false);

		// Authenticate simFDR
		if (!tkns.nextToken().equalsIgnoreCase("simFDR"))
			throw error(SC_UNAUTHORIZED, "Invalid Credentials", false);
		else if (!tkns.nextToken().equals(SystemData.get("security.simFDR")))
			throw error(SC_UNAUTHORIZED, "Invalid Credentials", false);
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return FALSE always
	 */
	@Override
	public final boolean isSecure() {
		return false;
	}
	
	/**
	 * Returns whether this web service requires an SSL connection.
	 * @return TRUE always
	 */
	@Override
	public final boolean requiresSSL() {
		return true;
	}
}
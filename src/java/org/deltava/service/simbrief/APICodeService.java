// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.simbrief;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.IOException;

import org.deltava.crypt.MessageDigester;

import org.deltava.service.*;

import org.deltava.util.system.SystemData;

/**
 * A Web Service to generate SimBrief API codes.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class APICodeService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Combine API key and payload
		StringBuilder keyBuf = new StringBuilder(SystemData.get("security.key.simbrief"));
		keyBuf.append(ctx.getParameter("api_req"));
		
		// Calculate MD5
		MessageDigester md = new MessageDigester("MD5", 512);
		String md5 = MessageDigester.convert(md.digest(keyBuf.toString().getBytes()));
		try {
			ctx.setContentType("text/javascript", "utf-8");
			ctx.println(String.format("var api_code = '%s';", md5));
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isLogged() {
		return false;
	}
	
	@Override
	public final boolean isSecure() {
		return true;
	}
}
// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import org.deltava.service.*;

/**
 * The XACARS acknowledge service, which ACKs a message. 
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

class ACKService extends XAService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		ctx.print("1|");
		return SC_OK;
	}
}
// Copyright 2011, 2014, 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import static javax.servlet.http.HttpServletResponse.*;

import java.util.*;
import java.io.IOException;

import org.deltava.service.*;
import org.deltava.util.StringUtils;

/**
 * The XACARS Address Web Service. 
 * @author Luke
 * @version 9.0
 * @since 4.1
 */

public class XAddressService extends XAService {
	
	private static final Map<String, XAService> _svcs = new HashMap<String, XAService>() {{
		put("TEST", new ACKService());
		put("BEGINFLIGHT", new StartFlightService());
		put("PAUSEFLIGHT", new ACKService());
		put("ENDFLIGHT", new EndFlightService());
		put("MESSAGE", new MessageService());
	}};

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get command name
		String cmd = ctx.getParameter("DATA2");
		int resultCode = SC_OK;
		try {
			ctx.setContentType("text/plain", "UTF-8");
			if (!StringUtils.isEmpty(cmd)) {
				XAService svc = _svcs.get(cmd.toUpperCase());
				if (svc != null)
					resultCode = svc.execute(ctx);
				else {
					log(ctx);
					ctx.print("0|Unknown XACARS Command");
				}
			} else
				log(ctx);
			
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_INTERNAL_SERVER_ERROR, "I/O Error", false);
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
		}
		
		return resultCode;
	}
}
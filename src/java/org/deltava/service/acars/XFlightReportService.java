// Copyright 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import java.util.*;

import static javax.servlet.http.HttpServletResponse.*;

import org.apache.log4j.Logger;

import org.deltava.service.*;

/**
 * A Web Service to save XACARS Flight Reports.
 * @author Luke
 * @version 2.1
 * @since 2.1
 */

public class XFlightReportService extends WebService {
	
	private static final Logger log = Logger.getLogger(XFlightReportService.class); 

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Dump out each parameters
		Map params = ctx.getRequest().getParameterMap();
		for (Iterator i = params.keySet().iterator(); i.hasNext(); ) {
			String pName = (String) i.next();
			log.warn(pName + " = " + ctx.getParameter(pName));
		}
		
		// Return success code
		return SC_OK;
	}
}
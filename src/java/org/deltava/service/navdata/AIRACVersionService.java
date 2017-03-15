// Copyright 2013, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.dao.*;
import org.deltava.service.*;

/**
 * A Web Service to return the current AIRAC version cycle.
 * @author Luke
 * @version 7.3
 * @since 5.1
 */

public class AIRACVersionService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		try {
			GetMetadata mddao = new GetMetadata(ctx.getConnection());
			ctx.println(mddao.get("navdata.cycle", "1101"));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		try {
			ctx.setContentType("text/plain", "UTF-8");
			ctx.setExpiry(1800);
			ctx.commit();
		} catch (Exception e) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	/**
	 * Returns whether this web service requires authentication.
	 * @return TRUE always
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}
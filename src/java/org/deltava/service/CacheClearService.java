// Copyright 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.util.cache.*;

/**
 * A Web Service to clear a cache. 
 * @author Luke
 * @version 6.2
 * @since 6.2
 */

public class CacheClearService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		if (!ctx.isUserInRole("Admin"))
			throw error(SC_UNAUTHORIZED, "Not in Admin role", false);
		
		// Get the cache and clear
		Cache<?> c = CacheManager.get(Cacheable.class, ctx.getParameter("id"));
		if (c == null)
			return SC_NOT_FOUND;
		
		c.clear();
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
// Copyright 2022 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.deltava.beans.PartnerInfo;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to track and redirect virtual airline Partner links.
 * @author Luke
 * @version 10.3
 * @since 10.3
 */

public class PartnerRedirectService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		try {
			Connection con = ctx.getConnection();
			
			// Get the partner data
			GetPartner dao = new GetPartner(con);
			PartnerInfo p = dao.get(StringUtils.parse(ctx.getParameter("id"), 0));
			if (p == null)
				throw error(SC_NOT_FOUND, "Unknown Partner - " + ctx.getParameter("id"));
			
			// Increment reference count and redirect
			SetPartner pwdao = new SetPartner(con);
			pwdao.refer(p.getID());
			ctx.setHeader("Location", p.getURL());
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		return SC_MOVED_PERMANENTLY;
	}
}
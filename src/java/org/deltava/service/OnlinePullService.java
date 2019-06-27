// Copyright 2019 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.time.Instant;

import org.deltava.beans.OnlineNetwork;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to record ServInfo feed pulls. 
 * @author Luke
 * @version 8.6
 * @since 8.6
 */

public class OnlinePullService extends WebService {
	
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		Instant pullDate = StringUtils.parseInstant(ctx.getParameter("d").trim(), "yyyyMMddHHmmss");
		try {
			SetOnlineTrack twdao = new SetOnlineTrack(ctx.getConnection());
			twdao.writePull(OnlineNetwork.fromName(ctx.getParameter("net")), pullDate);
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), true);
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}
	
	@Override
	public boolean isLogged() {
		return false;
	}
}
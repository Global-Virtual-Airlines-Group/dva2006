// Copyright 2011, 2012, 2013, 2015 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.acars.ClientInfo;
import org.deltava.beans.acars.ClientType;
import org.deltava.beans.acars.UpdateChannel;
import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to determine whether a new ACARS client is available.
 * @author Luke
 * @version 6.1
 * @since 4.1
 */

public class UpdateAvailableService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Parse the info
		ClientInfo cInfo = new ClientInfo(StringUtils.parse(ctx.getParameter("version"), 3),
				StringUtils.parse(ctx.getParameter("build"), 100), StringUtils.parse(ctx.getParameter("beta"), 0));
		if (Boolean.valueOf(ctx.getParameter("dispatch")).booleanValue())
			cInfo.setClientType(ClientType.DISPATCH);
		else if (Boolean.valueOf(ctx.getParameter("atc")).booleanValue())
			cInfo.setClientType(ClientType.ATC);
		
		// Get channel
		UpdateChannel ch = UpdateChannel.RELEASE;
		if (cInfo.getClientType() == ClientType.PILOT) {
			try {
				ch = UpdateChannel.valueOf(ctx.getParameter("channel").toUpperCase());
			} catch (Exception e) {
				if (cInfo.isBeta())
					ch = UpdateChannel.BETA;
			}
		}

		ClientInfo latest = null;
		try {
			GetACARSBuilds abdao = new GetACARSBuilds(ctx.getConnection());
			latest = abdao.getLatestBuild(cInfo);
			
			// If we're a beta, check the beta release
			if (ch == UpdateChannel.BETA) {
				ClientInfo beta = abdao.getLatestBeta(cInfo);
				if ((beta != null) && (beta.getClientBuild() >= cInfo.getClientBuild()) && (beta.compareTo(latest) > 0))
					latest = beta;
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());			
		} finally {
			ctx.release();
		}
		
		// Set header with latest data
		ctx.setHeader("X-update-channel", ch.toString().toLowerCase());
		if (latest != null)
			ctx.setHeader("X-update-latest", latest.toString());
		
		// See if anything is available
		return ((latest == null) || (latest.compareTo(cInfo) < 1)) ? SC_NOT_MODIFIED : SC_OK;
	}
}
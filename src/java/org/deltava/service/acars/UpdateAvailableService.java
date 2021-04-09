// Copyright 2011, 2012, 2013, 2015, 2016, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import static javax.servlet.http.HttpServletResponse.*;

import org.deltava.beans.acars.*;
import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to determine whether a new ACARS client is available.
 * @author Luke
 * @version 10.0
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
		ClientInfo cInfo = new ClientInfo(StringUtils.parse(ctx.getParameter("version"), 3), StringUtils.parse(ctx.getParameter("build"), 100), StringUtils.parse(ctx.getParameter("beta"), 0));
		if (Boolean.valueOf(ctx.getParameter("dispatch")).booleanValue())
			cInfo.setClientType(ClientType.DISPATCH);
		else if (Boolean.valueOf(ctx.getParameter("atc")).booleanValue())
			cInfo.setClientType(ClientType.ATC);
		
		// Get channel
		UpdateChannel ch = UpdateChannel.RELEASE;
		if (cInfo.getClientType() == ClientType.PILOT)
			ch = EnumUtils.parse(UpdateChannel.class, ctx.getParameter("channel"), cInfo.isBeta() ? UpdateChannel.BETA : UpdateChannel.RELEASE);

		ClientInfo latest = null; boolean isForced = false;
		try {
			GetACARSBuilds abdao = new GetACARSBuilds(ctx.getConnection());
			latest = abdao.getLatestBuild(cInfo);
			isForced = !abdao.isValid(cInfo);
			
			// Check for a forced upgrade
			if (!isForced) {
				ClientInfo forced = abdao.getLatestBuild(cInfo, true);
				isForced |= (forced != null) && (forced.compareTo(cInfo) > 0);
			}
			
			// If we're a beta, check the beta release
			if (ch == UpdateChannel.BETA) {
				ClientInfo beta = abdao.getLatestBeta(cInfo);
				if ((beta != null) && (beta.getClientBuild() >= cInfo.getClientBuild()) && (beta.compareTo(latest) > 0))
					latest = beta;
			}
		} catch (Exception e) {
			throw error(SC_INTERNAL_SERVER_ERROR, e.getMessage());			
		} finally {
			ctx.release();
		}
		
		if (latest == null) return SC_OK;
		
		// Set header with latest data
		ctx.setHeader("X-Update-Channel", ch.toString().toLowerCase());
		ctx.setHeader("X-Force-Upgrade", String.valueOf(isForced));
		ctx.setHeader("X-Update-Latest", latest.toString());
		
		// Check if we're running a beta
		if ((latest.getClientBuild() < cInfo.getClientBuild()) && cInfo.isBeta() && (ch == UpdateChannel.RELEASE))
			return SC_OK;
		
		// See if anything is available
		return (latest.compareTo(cInfo) < 1) ? SC_NOT_MODIFIED : SC_OK;
	}
}
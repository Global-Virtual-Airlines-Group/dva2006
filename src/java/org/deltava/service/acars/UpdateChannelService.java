// Copyright 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.acars;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.UpdateChannel;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.EnumUtils;

/**
 * A Web Serivce to switch ACARS client update channels. 
 * @author Luke
 * @version 10.0
 * @since 10.0
 */

public class UpdateChannelService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service Context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the channel
		UpdateChannel ch = EnumUtils.parse(UpdateChannel.class, ctx.getParameter("channel"), UpdateChannel.RELEASE);
		if (ch == ctx.getUser().getACARSUpdateChannel())
			return SC_NOT_MODIFIED;
		
		// Update the pilot
		boolean isChanged = false;
		try {
			Connection con = ctx.getConnection();
			GetPilot pdao = new GetPilot(con);
			Pilot p = pdao.get(ctx.getUser().getID()); // get latest
			if (p == null)
				return SC_NOT_FOUND;
			
			isChanged = (p.getACARSUpdateChannel() != ch);
			if (isChanged) {
				p.setACARSUpdateChannel(ch);
				SetPilot pwdao = new SetPilot(con);
				pwdao.write(p, ctx.getDB());
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}
		
		return isChanged ? SC_OK : SC_NOT_MODIFIED;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}
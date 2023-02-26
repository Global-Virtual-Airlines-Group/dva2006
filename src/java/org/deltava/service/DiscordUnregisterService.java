// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.dao.*;

/**
 * A Web Serivce to unregister a Discord UUID. 
 * @author Luke
 * @version 10.5
 * @since 10.5
 */

public class DiscordUnregisterService extends WebService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		String uuid = ctx.getParameter("id"); Pilot p = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the pilot
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			p = pdao.getByIMAddress(uuid);
			if (p == null)
				return SC_NOT_FOUND;
			
			// Remove the ID
			p.setExternalID(ExternalID.DISCORD, null);
			
			// Create status update
			StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.EXT_AUTH);
			upd.setAuthorID(p.getID());
			upd.setDescription("Discord Integration removed");
			
			// Start transaction
			ctx.startTX();
			
			// Save the profile
			SetPilot pwdao = new SetPilot(con);
			SetStatusUpdate swdao = new SetStatusUpdate(con);
			pwdao.write(p, ctx.getDB());
			swdao.write(upd, ctx.getDB());
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
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
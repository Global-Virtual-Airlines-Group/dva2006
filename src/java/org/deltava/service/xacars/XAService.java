// Copyright 2011, 2015, 2016, 2017, 2018, 2019, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.*;
import org.deltava.dao.*;
import org.deltava.security.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to support XACARS HTTP requests.
 * @author Luke
 * @version 9.0
 * @since 4.1
 */

abstract class XAService extends WebService {

	private static final Logger log = Logger.getLogger(XAService.class);

	/**
	 * Returns whether this web service's calls are logged.
	 * @return FALSE
	 */
	@Override
	public boolean isLogged() {
		return false;
	}

	/**
	 * Logs XACARS request parameters.
	 * @param ctx the ServiceContext
	 */
	protected static void log(ServiceContext ctx) {
		log.info(ctx.getRequest().getRequestURI());
		for (int x = 1; x <= 4; x++) {
			String data = ctx.getParameter("DATA" + x);
			if (!StringUtils.isEmpty(data))
				log.info("DATA" + x + " = '" + data + "'");
		}
	}

	/**
	 * Returns the XACARS protocol version.
	 * @param ctx the ServiceContext
	 * @return the protocol version
	 */
	protected static String getProtocolVersion(ServiceContext ctx) {
		String data = ctx.getParameter("DATA1");
		if ((data == null) || (data.indexOf('|') == -1))
			return "1.1";
		
		String ver = data.substring(data.indexOf('|') + 1);
		if (ver.indexOf('.') == -1)
			ver += ".0";
		
		return ver;
	}
	
	/**
	 * Returns the simulator used.
	 * @param ctx the ServiceContext
	 * @return the Simulator
	 */
	protected static Simulator getSimulator(ServiceContext ctx) {

		String data = ctx.getParameter("DATA1");
		if ((data == null) || (data.indexOf('_') == -1)) {
			log.warn("Unknown simulator - " + data);
			log(ctx);
			return Simulator.XP11;
		}
		
		int pos = data.indexOf('_');
		String sim = data.substring(pos + 1, data.indexOf('|', pos));
		log.info("Valid simulator " + sim);
		return sim.contains("MSFS") ? Simulator.FSX : Simulator.XP11;
	}
	
	/**
	 * Authenticates a user.
	 * @param ctx the ServiceContext
	 * @param userID the UserID
	 * @param pwd the password
	 * @return the Pilot if authenticated
	 * @throws SecurityException if authentication failed
	 */
	protected static Pilot authenticate(ServiceContext ctx, String userID, String pwd) throws SecurityException {
		try {
			Connection con = ctx.getConnection();
			
			// Find the user
			GetPilotDirectory pdao = new GetPilotDirectory(con);
			Pilot usr = pdao.getByCode(userID);
			if (usr == null)
				throw new SecurityException("Unknown Pilot ID");
			
			// Authenticate the user
			try (Authenticator auth = (Authenticator) SystemData.getObject(SystemData.AUTHENTICATOR)) {
				if (auth instanceof SQLAuthenticator) ((SQLAuthenticator) auth).setConnection(con);
				auth.authenticate(usr, pwd);
			}
			
			return usr;
		} catch (DAOException de) {
			throw new SecurityException(de);
		} finally {
			ctx.release();
		}
	}
}
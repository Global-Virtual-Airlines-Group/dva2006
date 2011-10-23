// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.xacars;

import static javax.servlet.http.HttpServletResponse.*;

import java.sql.Connection;

import org.deltava.beans.acars.XAFlightInfo;

import org.deltava.dao.*;
import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * The XACARS End Flight Service.
 * @author Luke
 * @version 4.1
 * @since 4.1
 */

public class EndFlightService extends XAService {

	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		int flightID = StringUtils.parse(ctx.getParameter("DATA3"), 0);
		try {
			Connection con = ctx.getConnection();
			
			// Get the flight
			GetXACARS dao = new GetXACARS(con);
			XAFlightInfo info = dao.getFlight(flightID);
			if (info == null)
				throw new InvalidDataException("Invalid Flight ID");
			
			// Mark the flight complete
			SetXACARS xwdao = new SetXACARS(con);
			xwdao.endFlight(flightID);
			ctx.print("1|");
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} catch (InvalidDataException ide) {
			ctx.print(ide.getResponse());
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}
}
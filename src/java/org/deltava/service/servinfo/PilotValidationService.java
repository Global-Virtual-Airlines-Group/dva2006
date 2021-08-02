// Copyright 2007, 2008, 2009, 2010, 2011, 2012, 2016, 2017, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.servinfo;

import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.servinfo.Certificate;
import org.deltava.beans.system.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.http.DAO.Compression;
import org.deltava.dao.http.GetVATSIMData;

import org.deltava.service.*;

import org.deltava.util.StringUtils;

/**
 * A Web Service to validate VATSIM membership data.
 * @author Luke
 * @version 10.1
 * @since 1.0
 */

public class PilotValidationService extends WebService {
	
	/**
	 * Executes the Web Service, returning VATSIM CERT data.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the validation URL
		String id = ctx.getParameter("id");
		if (StringUtils.isEmpty(id))
			return SC_NOT_FOUND;
		
		Certificate c = null;
		try {
			GetVATSIMData dao = new GetVATSIMData();
			dao.setCompression(Compression.GZIP);
			c = dao.getInfo(id);
			APILogger.add(new APIRequest(API.VATSIM.createName("CERT"), !ctx.isAuthenticated()));
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		}
		
		// Return a 404 if not found
		if (c == null)
			return SC_NOT_FOUND;
		
		// Build the JSON Document
		JSONObject jo = new JSONObject();
		jo.put("id", c.getID());
		jo.put("network", "VATSIM");
		jo.put("registeredOn", StringUtils.format(c.getRegistrationDate(), "yyyy/MM/dd HH:mm"));
		jo.put("active", c.isActive());
		
		// Dump the JSON to the output stream
		try {
			ctx.setContentType("application/json", "utf-8");
			ctx.println(jo.toString());
			ctx.setExpiry(60);
			ctx.commit();
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);
		}
		
		return SC_OK;
	}
	
	@Override
	public final boolean isLogged() {
		return false;
	}
}
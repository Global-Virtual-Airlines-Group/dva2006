// Copyright 2005, 2007, 2010, 2012, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.*;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.json.*;

import org.deltava.beans.fleet.Installer;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Fleet Library Information.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class InstallerInfoService extends WebService {

	/**
	 * Executes the Web Service, returning an XML snippet for the Fleet Library page 
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the installer code
		String app = SystemData.get("airline.code");
		String code = ctx.getParameter("code");
		if (code == null)
			throw new ServiceException(SC_BAD_REQUEST, "No Installer Code");

		// Check if we're using DB.CODE notation
		if (code.indexOf('.') != -1) {
			StringTokenizer tkns = new StringTokenizer(code, ".");
			app = tkns.nextToken();
			code = tkns.nextToken();
		}

		// Get the DAO and do the search
		AirlineInformation ai = SystemData.getApp(app);
		Installer i = null;
		try {
			GetLibrary dao = new GetLibrary(ctx.getConnection());
			i = dao.getInstallerByCode(code, ai.getDB());
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// If no installer found, return a 404 error
		if (i == null)
			throw new ServiceException(SC_NOT_FOUND, code + " not found");

		// Get the format strings
		String dFmt = ctx.isAuthenticated() ? ctx.getUser().getDateFormat() : SystemData.get("date_format");
		String nFmt = ctx.isAuthenticated() ? ctx.getUser().getNumberFormat() : "#,##0";
		if (nFmt.contains("."))
			nFmt = nFmt.substring(0, nFmt.indexOf('.'));
		
		// Generate the JSON document
		JSONObject jo = new JSONObject();
		jo.put("code", i.getCode());
		jo.put("fileName", i.getFileName());
		jo.put("title", i.getName());
		jo.put("size", StringUtils.format(i.getSize(), nFmt));
		if (i.getLastModified() != null)
			jo.put("date", StringUtils.format(i.getLastModified(), dFmt));
		jo.put("version", i.getVersion());
		jo.put("dl", StringUtils.format(i.getDownloadCount(), nFmt));
		jo.put("img", "/" + SystemData.get("path.img") + "/fleet/" + i.getImage());
		i.getFSVersions().forEach(sim -> jo.append("sims", sim.getName()));
		jo.put("desc", i.getDescription());
		
		// Dump the JSON to the output stream
		JSONUtils.ensureArrayPresent(jo, "version");
		try {
			ctx.setContentType("application/json", "UTF-8");
			ctx.println(jo.toString());
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
		}

		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	@Override
	public final boolean isLogged() {
		return false;
	}

	/**
	 * Marks the Web Service as secure.
	 * @return TRUE always
	 */
	@Override
	public final boolean isSecure() {
		return true;
	}
}
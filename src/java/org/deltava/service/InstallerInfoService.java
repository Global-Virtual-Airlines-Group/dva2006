// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.util.StringTokenizer;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;

import org.jdom.*;
import org.jdom.output.*;

import org.deltava.beans.fleet.Installer;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

/**
 * A Web Service to display Fleet Library Information.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class InstallerInfoService extends WebService {

	/**
	 * Executes the Web Service, returning an INI file for use with the Fleet Installers.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	public int execute(ServiceContext ctx) throws ServiceException {

		// Get the installer code
	   String db = SystemData.get("airline.db");
		String code = ctx.getParameter("code");
		if (code == null)
			throw new ServiceException(SC_BAD_REQUEST, "No Installer Code");
		
		// Check if we're using DB.CODE notation
		if (code.indexOf('.') != -1) {
		   StringTokenizer tkns = new StringTokenizer(code, ".");
		   db = tkns.nextToken();
		   code = tkns.nextToken();
		}

		// Get the DAO and do the search
		Installer i = null;
		try {
			GetLibrary dao = new GetLibrary(ctx.getConnection());
			i = dao.getInstallerByCode(code, db);
		} catch (DAOException de) {
			throw new ServiceException(SC_INTERNAL_SERVER_ERROR, de.getMessage());
		} finally {
			ctx.release();
		}

		// If no installer found, return a 404 error
		if (i == null)
			throw new ServiceException(SC_NOT_FOUND, code + " not found");

		// Generate the XML document
		Document doc = new Document();
		Element re = new Element("wsdata");
		doc.setRootElement(re);

		// Create the installer entry
		Element le = new Element("installer");
		re.addContent(le);
		le.setAttribute("code", i.getCode());
		le.setAttribute("filename", i.getFileName());
		le.setAttribute("title", i.getName());
		le.setAttribute("size", StringUtils.format(i.getSize(), "#,#00"));
		le.setAttribute("version", i.getVersion());
		le.setAttribute("dl", StringUtils.format(i.getDownloadCount(), "#,##0"));
		le.setAttribute("img", "/" + SystemData.get("path.img") + "/fleet/" + i.getImage());
		le.addContent(new CDATA(i.getDescription()));

		// Dump the XML to the output stream
		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat().setEncoding("ISO-8859-1"));
		try {
			ctx.getResponse().setContentType("text/xml");
			ctx.println(xmlOut.outputString(doc));
			ctx.commit();
		} catch (IOException ie) {
			throw new ServiceException(SC_CONFLICT, "I/O Error");
		}

		// Write result code
		return SC_OK;
	}

	/**
	 * Tells the Web Service Servlet not to log invocations of this service.
	 * @return FALSE
	 */
	public final boolean isLogged() {
		return false;
	}

	/**
	 * Marks the Web Service as secure.
	 * @return TRUE always
	 */
	public final boolean isSecure() {
		return true;
	}
}
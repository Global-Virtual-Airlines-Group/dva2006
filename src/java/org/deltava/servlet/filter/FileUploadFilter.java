// Copyright 2005, 2007, 2009, 2011, 2012, 2015, 2017, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import static org.deltava.commands.CommandContext.*;

import org.deltava.beans.FileUpload;

import org.deltava.util.StringUtils;

/**
 * A servlet filter to support saving multi-part form upload data into the servlet request.
 * @author Luke
 * @version 9.0
 * @since 1.0
 */

public class FileUploadFilter extends HttpFilter {

	private static final Logger log = Logger.getLogger(FileUploadFilter.class);
	private static final String CONTENT_TYPE = "multipart/form-data";

	@Override
	public void init(FilterConfig cfg) throws ServletException {
		log.info("Started");
	}

	/**
	 * Called by the servlet container on each request. Saves file upload fields in the request.
	 * @param req the request
	 * @param rsp the response
	 * @param fc the Filter Chain
	 * @throws IOException if an I/O error occurs
	 * @throws ServletException if a general error occurs
	 */
	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain fc) throws IOException, ServletException {

		// Convert the request type and encoding
		req.setCharacterEncoding("UTF-8");

		// Check if we're doing a POST
		String cType = req.getContentType();
		if (("POST".equalsIgnoreCase(req.getMethod())) && (!StringUtils.isEmpty(cType)) && (cType.startsWith(CONTENT_TYPE))) {
			FileUploadRequestWrapper reqWrap = new FileUploadRequestWrapper(req);
			if (log.isDebugEnabled())
				log.debug("Processing form upload request");

			for (Part p : req.getParts()) {
				if (log.isDebugEnabled())
					log.debug(p.getName() + " " + p.getHeaderNames());
				String fName = p.getSubmittedFileName();
				if (!StringUtils.isEmpty(fName)) {
					if (log.isDebugEnabled())
						log.debug("Found File element " + p.getName() + ", file=" + fName);

					FileUpload upload = new FileUpload(fName);
					try {
						upload.load(p.getInputStream());
						req.setAttribute("FILE$" + p.getName(), upload);
					} catch (IOException ie) {
						log.warn("Cannot load attachment - " + ie.getMessage());
						reqWrap.setAttribute(INVALIDREQ_ATTR_NAME, ie);
					} finally {
						p.delete();
					}
				} else {
					StringBuilder buf = new StringBuilder();
					try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) {
						while (br.ready()) {
							buf.append(br.readLine());
							if (br.ready())
								buf.append('\n');
						}
					}
					
					if (buf.length() > 0)
						reqWrap.addParameter(p.getName(), buf.toString());
				}
			}

			// Add requests from the command line and convert to the proper character set
			Enumeration<String> pNames = req.getParameterNames();
			while (pNames.hasMoreElements()) {
				String pName = pNames.nextElement();
				String[] rawValues = req.getParameterValues(pName);
				for (int x = 0; x < rawValues.length; x++)
					rawValues[x] = new String(rawValues[x].getBytes("ISO-8859-1"), "UTF-8");

				reqWrap.addParameter(pName, rawValues);
			}

			// Filter with the new request
			fc.doFilter(reqWrap, rsp);
		} else
			fc.doFilter(req, rsp);
	}

	@Override
	public void destroy() {
		log.info("Stopped");
	}
}
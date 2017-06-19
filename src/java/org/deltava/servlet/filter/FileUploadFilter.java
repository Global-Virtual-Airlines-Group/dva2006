// Copyright 2005, 2007, 2009, 2011, 2012, 2015, 2017 Global Virtual Airlines Group. All Rights Reserved.
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
 * @version 7.5
 * @since 1.0
 */

public class FileUploadFilter implements Filter {

	private static final Logger log = Logger.getLogger(FileUploadFilter.class);
	private static final String CONTENT_TYPE = "multipart/form-data";

	/**
	 * Called by the servlet container when the filter is started. Logs a message.
	 * @param cfg the Filter Configuration
	 */
	@Override
	public void init(FilterConfig cfg) throws ServletException {
		log.info("Started");
	}

	/**
	 * Called by the servlet container on each request. Saves file upload fields in the request.
	 * @param req the Servlet Request
	 * @param rsp the Servlet Response
	 * @param fc the Filter Chain
	 * @throws IOException if an I/O error occurs
	 * @throws ServletException if a general error occurs
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {

		// Convert the request type and encoding
		HttpServletRequest hreq = (HttpServletRequest) req;
		hreq.setCharacterEncoding("UTF-8");

		// Check if we're doing a POST
		String cType = hreq.getContentType();
		if (("POST".equalsIgnoreCase(hreq.getMethod())) && (!StringUtils.isEmpty(cType)) && (cType.startsWith(CONTENT_TYPE))) {
			FileUploadRequestWrapper reqWrap = new FileUploadRequestWrapper(hreq);
			if (log.isDebugEnabled())
				log.debug("Processing form upload request");

			for (Part p : hreq.getParts()) {
				if (log.isDebugEnabled())
					log.debug(p.getName() + " " + p.getHeaderNames());
				String fName = p.getSubmittedFileName();
				if (!StringUtils.isEmpty(fName)) {
					if (log.isDebugEnabled())
						log.debug("Found File element " + p.getName() + ", file=" + fName);

					FileUpload upload = new FileUpload(fName);
					try {
						upload.load(p.getInputStream());
						hreq.setAttribute("FILE$" + p.getName(), upload);
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
			Enumeration<String> pNames = hreq.getParameterNames();
			while (pNames.hasMoreElements()) {
				String pName = pNames.nextElement();
				String[] rawValues = hreq.getParameterValues(pName);
				for (int x = 0; x < rawValues.length; x++)
					rawValues[x] = new String(rawValues[x].getBytes("ISO-8859-1"), "UTF-8");

				reqWrap.addParameter(pName, rawValues);
			}

			// Filter with the new request
			fc.doFilter(reqWrap, rsp);
		} else
			fc.doFilter(req, rsp);
	}

	/**
	 * Called by the servlet container when the filter is stopped. Logs a message.
	 */
	@Override
	public void destroy() {
		log.info("Stopped");
	}
}
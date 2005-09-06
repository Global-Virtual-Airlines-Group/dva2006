package org.deltava.servlet.filter;

import java.util.Enumeration;
import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import com.oreilly.servlet.multipart.*;

import org.deltava.beans.FileUpload;

/**
 * A servlet filter to support saving multi-part form upload data into the servlet request.
 * @author Luke
 * @version 1.0
 * @since 1.0 Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
 */

public class FileUploadFilter implements Filter {

	private static final Logger log = Logger.getLogger(FileUploadFilter.class);
	private static final String CONTENT_TYPE = "multipart/form-data";

	/**
	 * Called by the servlet container when the filter is started. Logs a message.
	 * @param cfg the Filter Configuration
	 */
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
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain fc) throws IOException, ServletException {

		// Convert the request type
		HttpServletRequest hreq = (HttpServletRequest) req;
		if (("POST".equals(hreq.getMethod())) && (hreq.getContentType().startsWith(CONTENT_TYPE))) {
			log.debug("Processing form upload request");
			FileUploadRequestWrapper reqWrap = new FileUploadRequestWrapper(hreq);

			// Parse the request
			MultipartParser parser = new MultipartParser(hreq, 8192000, true, true);
			Part p = parser.readNextPart();
			while (p != null) {
				if (p.isFile()) {
					FilePart fp = (FilePart) p;

					// Save the file data in the request
					if (fp.getFileName() != null) {
						log.debug("Found File element " + p.getName() + ", file=" + fp.getFileName());
						FileUpload upload = new FileUpload(fp.getFileName());
						upload.load(fp.getInputStream());
						hreq.setAttribute("FILE$" + p.getName(), upload);
					}
				} else if (p.isParam()) {
					ParamPart pp = (ParamPart) p;
					reqWrap.addParameter(p.getName(), pp.getStringValue());
				}

				// Get next part
				p = parser.readNextPart();
			}

			// Add requests from the command line
			Enumeration pNames = hreq.getParameterNames();
			while (pNames.hasMoreElements()) {
				String pName = (String) pNames.nextElement();
				reqWrap.addParameter(pName, hreq.getParameterValues(pName));
			}

			// Filter with the new request
			fc.doFilter(reqWrap, rsp);
		} else {
			fc.doFilter(req, rsp);
		}
	}

	/**
	 * Called by the servlet container when the filter is stopped. Logs a message.
	 */
	public void destroy() {
		log.info("Stopped");
	}
}
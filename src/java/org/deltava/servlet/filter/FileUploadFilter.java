// Copyright 2005, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet.filter;

import java.util.Enumeration;
import java.io.IOException;

import java.nio.charset.Charset;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import com.oreilly.servlet.multipart.*;

import org.deltava.beans.FileUpload;

/**
 * A servlet filter to support saving multi-part form upload data into the servlet request, and
 * @author Luke
 * @version 2.0
 * @since 1.0 
 */

public class FileUploadFilter implements Filter {

	private static final Logger log = Logger.getLogger(FileUploadFilter.class);
	private static final String CONTENT_TYPE = "multipart/form-data";
	
	private String _encoding = "UTF-8";

	/**
	 * Called by the servlet container when the filter is started. Logs a message.
	 * @param cfg the Filter Configuration
	 */
	public void init(FilterConfig cfg) throws ServletException {
		String encoding = cfg.getInitParameter("encoding");
		if (encoding != null) {
			if (!Charset.availableCharsets().containsKey(encoding))
				log.error("Unknown character set - " + encoding);
			else
				_encoding = encoding;
		}
		
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

		// Convert the request type and encoding
		HttpServletRequest hreq = (HttpServletRequest) req;
		hreq.setCharacterEncoding(_encoding);
		
		// Check if we're doing a POST
		if (("POST".equalsIgnoreCase(hreq.getMethod())) && (hreq.getContentType().startsWith(CONTENT_TYPE))) {
			FileUploadRequestWrapper reqWrap = new FileUploadRequestWrapper(hreq);
			if (log.isDebugEnabled())
				log.debug("Processing form upload request");

			// Parse the request
			MultipartParser parser = null;
			try {
			   parser = new MultipartParser(hreq, 8192000, true, true, _encoding);
			} catch (IOException ie) {
			   log.warn(ie.getMessage());
			}
			
			Part p = (parser != null) ? parser.readNextPart() : null;
			while (p != null) {
				if (p.isFile()) {
					FilePart fp = (FilePart) p;

					// Save the file data in the request
					if (fp.getFileName() != null) {
						if (log.isDebugEnabled())
							log.debug("Found File element " + p.getName() + ", file=" + fp.getFileName());
						
						FileUpload upload = new FileUpload(fp.getFileName());
						try {
							upload.load(fp.getInputStream());
							hreq.setAttribute("FILE$" + p.getName(), upload);
						} catch (IOException ie) {
							log.warn("Cannot load attachment - " + ie.getMessage());
						}
					}
				} else if (p.isParam())
					reqWrap.addParameter(p.getName(), ((ParamPart) p).getStringValue());

				// Get next part
				p = parser.readNextPart();
			}

			// Add requests from the command line and convert to the proper character set
			Enumeration pNames = hreq.getParameterNames();
			while (pNames.hasMoreElements()) {
				String pName = (String) pNames.nextElement();
				String[] rawValues = hreq.getParameterValues(pName);
				for (int x = 0; x < rawValues.length; x++)
					rawValues[x] = new String(rawValues[x].getBytes("ISO-8859-1"), _encoding);
				
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
	public void destroy() {
		log.info("Stopped");
	}
}
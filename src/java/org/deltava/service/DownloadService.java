// Copyright 2008, 2011, 2013, 2015, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.deltava.util.system.SystemData;

/**
 * A Web Service supporting file downloads.
 * @author Luke
 * @version 10.5
 * @since 2.2
 */

public abstract class DownloadService extends WebService {
	
	private static final Logger log = Logger.getLogger(DownloadService.class);

	/**
	 * Sends a file to the HTTP output stream, either via mod_xsendfile or through native Java I/O streaming.
	 * @param f the file to send
	 * @param rsp the HTTP Servlet response
	 */
	protected static void sendFile(File f, HttpServletResponse rsp) {
		if (!f.exists() || !f.isFile())
			throw new IllegalStateException(f.getAbsolutePath() + " does not exist");
		
		// Check if we stream via mod_xsendfile
		if (SystemData.getBoolean("airline.files.sendfile")) {
			if (log.isDebugEnabled()) log.debug("Sending " + f.getName() + " via mod_xsendfile");
			rsp.addHeader("X-Sendfile", f.getAbsolutePath());
			return;
		}
		
		// Send via regular buffering
		int bufferSize = (int) Math.min(131072, f.length());
		try {
			rsp.setContentLength((int) f.length());
			rsp.setBufferSize(bufferSize);
			try (InputStream is = new BufferedInputStream(new FileInputStream(f), 65536); OutputStream out = rsp.getOutputStream()) {
				byte[] buf = new byte[bufferSize];
				int bytesRead = is.read(buf, 0, bufferSize);
				while (bytesRead != -1) {
					out.write(buf, 0, bytesRead);
					bytesRead = is.read(buf, 0, bufferSize);
				}
			}
		} catch (IOException ie) {
			log.info("Download canceled");
		} catch (Exception e) {
			log.error("Error downloading " + f.getName(), e);
		}
	}
}
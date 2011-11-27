// Copyright 2008, 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service;

import java.io.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.deltava.util.system.SystemData;

/**
 * A Web Service supporting file downloads.
 * @author Luke
 * @version 4.1
 * @since 2.2
 */

public abstract class DownloadService extends WebService {
	
	private static final Logger log = Logger.getLogger(DownloadService.class);

	/**
	 * Sends a file to the HTTP output stream, either via mod_xsendfile or through native Java I/O streaming.
	 * @param f the file to send
	 * @param rsp the HTTP Servlet response
	 */
	protected void sendFile(File f, HttpServletResponse rsp) {
		if (!f.exists() || !f.isFile())
			return;
		
		// Check if we stream via mod_xsendfile
		if (SystemData.getBoolean("airline.files.sendfile")) {
			log.info("Sending " + f.getName() + " via mod_xsendfile");
			rsp.addHeader("X-Sendfile", f.getAbsolutePath());
			return;
		}
		
		// Send via regular buffering
		try {
			rsp.setContentLength((int) f.length());
			int bufferSize = (int) Math.min(131072, f.length());
			rsp.setBufferSize(bufferSize);
			byte[] buf = new byte[bufferSize];
			InputStream is = new FileInputStream(f);
			OutputStream out = rsp.getOutputStream();
			int bytesRead = is.read(buf, 0, bufferSize);
			while (bytesRead != -1) {
				out.write(buf, 0, bytesRead);
				bytesRead = is.read(buf, 0, bufferSize);
			}

			is.close();
			out.flush();
		} catch (IOException ie) {
			log.info("Download canceled");
		} catch (Exception e) {
			log.error("Error downloading " + f.getName(), e);
		}
	}
}
// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.navdata;

import java.io.*;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.deltava.service.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * An abstract class to store a common cache for Dispatch Web Services.
 * @author Luke
 * @version 2.2
 * @since 2.2
 */

public abstract class DispatchDataService extends WebService {
	
	private static final Logger log = Logger.getLogger(DispatchDataService.class);
	
	protected static final FileSystemCache _dataCache = new FileSystemCache(16, SystemData.get("schedule.cache"));
	
	/**
	 * Adds a file to the shared file cache. 
	 * @param key the cache key
	 * @param f the file
	 */
	protected void addCacheEntry(Object key, File f) {
		if ((f != null) && f.exists())
			_dataCache.add(new CacheableFile(key, f));
	}
	
	/**
	 * Clears the file cache.
	 * @see FileSystemCache#clear()
	 */
	public static synchronized void invalidate() {
		_dataCache.clear();
	}
	
	/**
	 * Sends a file to the HTTP output stream, either via mod_xsendfile or through
	 * native Java I/O streaming.
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
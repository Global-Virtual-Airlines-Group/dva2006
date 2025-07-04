// Copyright 2017, 2019, 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.servlet;

import java.io.*;
import java.security.Principal;

import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;

import org.apache.logging.log4j.*;

import org.deltava.beans.UploadInfo;
import org.deltava.beans.system.VersionInfo;

import org.deltava.util.StringUtils;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A servlet to support file uploads.
 * @author Luke
 * @version 11.1
 * @since 7.5
 */

@MultipartConfig
public class UploadServlet extends BasicAuthServlet {

	private static final Logger log = LogManager.getLogger(UploadServlet.class);

	private static final Cache<UploadInfo> _cache = CacheManager.get(UploadInfo.class, "UploadState");

	/**
	 * Temporary file filter.
	 */
	static class TempFilter implements FileFilter {

		@Override
		public boolean accept(File f) {
			return f.isFile() && f.getName().endsWith(".tmp");
		}
	}

	/**
	 * Returns the servlet description.
	 * @return name, author and copyright info for this servlet
	 */
	@Override
	public String getServletInfo() {
		return "File Upload Servlet " + VersionInfo.TXT_COPYRIGHT;
	}

	/**
	 * Processes HTTP POST requests for attachments.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 * @throws IOException if a network I/O error occurs
	 */
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		int chunk = StringUtils.parse(req.getParameter("c"), -1);

		// Make sure we are authenticated
		Principal usr = req.getUserPrincipal();
		if (usr == null)
			usr = authenticate(req);
		if (usr == null) {
			challenge(rsp, "File Upload");
			return;
		}

		UploadInfo info = getInfo(req, true);
		Part p = req.getPart("file");
		if (p == null) {
			rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		try (RandomAccessFile raf = new RandomAccessFile(info.getTempFile(), "rw")) {
			raf.seek((chunk - 1) * (long) info.getChunkSize()); // Seek to position

			int totalRead = 0; byte[] bytes = new byte[Math.min(info.getChunkSize(), 262144)];
			try (InputStream is = p.getInputStream()) {
				int r = is.read(bytes);
				while (r != -1) {
					raf.write(bytes, 0, r);
					totalRead += r;
					r = is.read(bytes);
				}
			}

			log.info("Wrote {} bytes for chunk {} {}", Integer.valueOf(totalRead), Integer.valueOf(chunk), p.getSubmittedFileName());
		}

		info.complete(chunk);
		_cache.add(info);

		// Check if all chunks uploaded, and change filename
		try (PrintWriter rw = rsp.getWriter()) {
			if (info.isComplete()) {
				File nf = new File(info.getTempFile().getParentFile(), info.getFileName());
				if (nf.exists()) {
					log.warn("{} already exists, deleting", nf);
					nf.delete();
				}

				info.getTempFile().renameTo(nf);
				_cache.remove(info.getID());
				log.info("Renaming {} to {}", info.getTempFile(), nf);
				rw.print("Complete");
			} else
				rw.print("Uploaded " + chunk);
		} finally {
			cleanTempFiles();	
		}
	}

	/**
	 * Process HTTP GET requests for chunk completion. Returns a 200 if the chunk is uploaded, or a 404 if not.
	 * @param req the HTTP request
	 * @param rsp the HTTP response
	 * @throws IOException if something bad happens
	 */
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse rsp) throws IOException {

		// Make sure we are authenticated
		Principal usr = req.getUserPrincipal();
		if (usr == null)
			usr = authenticate(req);
		if (usr == null) {
			challenge(rsp, "File Upload");
			return;
		}

		UploadInfo info = getInfo(req, false);
		if ((info != null) && info.getTempFile().exists()) {
			int chunk = StringUtils.parse(req.getParameter("c"), -1);
			rsp.setStatus(info.isComplete(chunk) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
		} else
			rsp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

	/*
	 * Helper method to parse request parameters for chunk data.
	 */
	private static UploadInfo getInfo(HttpServletRequest req, boolean createIfUnknown) {
		String id = req.getParameter("resumableIdentifier");
		UploadInfo info = _cache.get(id);
		if ((info == null) && createIfUnknown) {
			info = new UploadInfo(StringUtils.parse(req.getParameter("cs"), -1), StringUtils.parse(req.getParameter("ts"), -1));
			info.setID(id);
			info.setFileName(req.getParameter("resumableFilename"));
			info.setTempFile(new File(SystemData.get("path.upload"), info.getFileName() + ".tmp"));
			_cache.add(info);
		}

		return info;
	}

	/*
	 * Helper method to purge partial uploads after 4h.
	 */
	private static void cleanTempFiles() {
		File d = new File(SystemData.get("path.upload"));
		File[] ff = d.listFiles(new TempFilter());

		long now = System.currentTimeMillis();
		for (int x = 0; (ff != null) && (x < ff.length); x++) {
			File f = ff[x];
			if (((now - f.lastModified()) / 1000) > 14400) {
				log.warn("Deleting partial upload {}", f.getName());
				f.delete();
			}
		}
	}
}
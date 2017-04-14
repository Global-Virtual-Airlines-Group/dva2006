// Copyright 2006, 2007, 2008, 2009, 2011, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;

import java.sql.Connection;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.HeadMethod;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.VersionInfo;
import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to validate the integrity of Water Cooler Image URLs.
 * @author Luke
 * @version 7.3
 * @since 1.0
 */

public class ImageLinkTestTask extends Task {

	protected Collection<?> _mimeTypes;
	
	private class ImageLinkWorker extends Thread {
		private final Logger tLog;
		private final Queue<LinkedImage> _work;
		private final Collection<String> _invalidHosts;
		private final Queue<LinkedImage> _output;
		
		ImageLinkWorker(int id, Queue<LinkedImage> work, Collection<String> badHosts, Queue<LinkedImage> out) {
			super("ImageLinkWorker-" + String.valueOf(id));
			setDaemon(true);
			tLog = Logger.getLogger(ImageLinkTestTask.class.getPackage().getName() + "." + getName());
			_work = work;
			_invalidHosts = badHosts;
			_output = out;
		}
		
		@Override
		public void run() {
			HttpClient hc = new HttpClient();
			hc.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
			hc.getParams().setParameter("http.useragent",  VersionInfo.USERAGENT);
			hc.getParams().setParameter("http.tcp.nodelay", Boolean.TRUE);
			hc.getParams().setParameter("http.socket.timeout", Integer.valueOf(8250));
			hc.getParams().setParameter("http.connection.timeout", Integer.valueOf(8250));
			
			// Go through each image
			LinkedImage img = _work.poll();
			while (img != null) {
				boolean isOK = false;
				URL url = null;
				try {
					url = new URL(img.getURL());
					if (_invalidHosts.contains(url.getHost()))
						throw new IllegalArgumentException("Bad Host!");
					
					// Open the connection
					HeadMethod hm = new HeadMethod(url.toExternalForm());
					hm.setFollowRedirects(false);

					// Validate the result code
					int resultCode = hc.executeMethod(hm);
					if (resultCode != HttpURLConnection.HTTP_OK)
						tLog.warn("Invalid Image HTTP result code - " + resultCode);
					else {
						Header[] hdrs = hm.getResponseHeaders("Content-Type");
						String cType = (hdrs.length == 0) ? "unknown" : hdrs[0].getValue();
						isOK = _mimeTypes.contains(cType);
						if (!isOK)
							tLog.warn("Invalid MIME type for " + img + " - " + cType);
						else
							tLog.info("Validated " + url.toExternalForm());
					}
				} catch (IllegalArgumentException iae) {
					if (url != null)
						tLog.warn("Known bad host - " + url.getHost());
				} catch (MalformedURLException mue) {
					tLog.warn("Invalid URL - " + img);
				} catch (IOException ie) {
					tLog.warn("Error validating " + img + " - " + ie.getMessage());
					if ("Connection timed out".equals(ie.getMessage()) && (url != null))
						_invalidHosts.add(url.getHost());
				}
				
				// If not OK, push onto the error stack
				if (!isOK)
					_output.add(img);
				
				// Get next image
				img = isInterrupted() ? null : _work.poll();
			}
		}
	}

	/**
	 * Initializes the task.
	 */
	public ImageLinkTestTask() {
		super("Image URL Test", ImageLinkTestTask.class);
		_mimeTypes = (Collection<?>) SystemData.getObject("cooler.imgurls.mime_types");
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Get the images to check
			GetCoolerLinks dao = new GetCoolerLinks(con);
			Collection<Integer> ids = dao.getThreads();
			log.info("Validating images in " + ids.size() + " discussion threads");
			
			// Load the images
			Queue<LinkedImage> work = new ConcurrentLinkedQueue<LinkedImage>();
			for (Integer id : ids)
				work.addAll(dao.getURLs(id.intValue(), false));
			
			ctx.release();

			// Keep track of invalid hosts
			Collection<String> invalidHosts = Collections.synchronizedSet(new HashSet<String>());
			Queue<LinkedImage> badImgs = new ConcurrentLinkedQueue<LinkedImage>();
			
			// Fire up the workers
			int tpSize = Math.max(1, Math.min(12, work.size() / 16));
			Collection<Thread> workers = new ArrayList<Thread>();
			for (int x = 1; x <= tpSize; x++) {
				ImageLinkWorker wrk = new ImageLinkWorker(x, work, invalidHosts, badImgs);
				wrk.setUncaughtExceptionHandler(this);
				workers.add(wrk);
				wrk.start();
			}
			
			// Wait for the workers to finish
			ThreadUtils.waitOnPool(workers);
			
			// Save the last update date/time for each thread
			Map<Integer, Long> updTimes = new HashMap<Integer, Long>();
			
			// Re-open the database connection
			con = ctx.getConnection();
			ctx.startTX();

			// Nuke the bad images
			SetCoolerLinks wdao = new SetCoolerLinks(con);
			SetCoolerMessage msgdao = new SetCoolerMessage(con);
			for (LinkedImage img : badImgs) {
				ThreadUpdate upd = new ThreadUpdate(img.getThreadID());
				upd.setMessage("Removed linked image " + img.getURL());
				upd.setAuthorID(ctx.getUser().getID());

				// Get the update time
				Integer id = Integer.valueOf(img.getThreadID());
				if (updTimes.containsKey(id)) {
					long time = updTimes.get(id).longValue();
					time = Math.max(System.currentTimeMillis(), time + 1000);
					updTimes.put(id, new Long(time));
					upd.setDate(Instant.ofEpochMilli(time));
				} else {
					updTimes.put(id, new Long(System.currentTimeMillis()));
					upd.setDate(Instant.now());
				}
				
				// Write a thread update and delete the link
				msgdao.write(upd);
				wdao.disable(img.getThreadID(), img.getID());
			}
			
			// Commit
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error("Error validating images - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}
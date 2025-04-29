// Copyright 2006, 2007, 2008, 2009, 2011, 2016, 2017, 2021, 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;

import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.deltava.beans.cooler.*;
import org.deltava.beans.system.VersionInfo;

import org.deltava.dao.*;
import org.deltava.taskman.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to validate the integrity of Water Cooler Image URLs.
 * @author Luke
 * @version 11.6
 * @since 1.0
 */

public class ImageLinkTestTask extends Task {

	protected Collection<?> _mimeTypes;

	private class ImageLinkWorker implements Runnable {
		private final Logger tLog = LogManager.getLogger(ImageLinkWorker.class);
		
		private final LinkedImage _img;
		private final Collection<String> _invalidHosts;
		private final Queue<LinkedImage> _output;

		ImageLinkWorker(LinkedImage img, Collection<String> badHosts, Queue<LinkedImage> out) {
			super();
			_img = img;
			_invalidHosts = badHosts;
			_output = out;
		}

		@Override
		public void run() {
			boolean isOK = false; URI url = null;
			try (HttpClient hc = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()) {
				url = new java.net.URI(_img.getURL());
				if (_invalidHosts.contains(url.getHost()))
					throw new IllegalArgumentException("Bad Host!");

				// Open the connection
				HttpRequest req = HttpRequest.newBuilder().timeout(Duration.ofMillis(8250)).uri(url).header("User-Agent", VersionInfo.getUserAgent()).HEAD().build();

				// Validate the result code
				TaskTimer tt = new TaskTimer();
				HttpResponse<String> rsp = hc.send(req, HttpResponse.BodyHandlers.ofString());
				int resultCode = rsp.statusCode();
				tt.stop();
				if (resultCode != HttpURLConnection.HTTP_OK)
					tLog.warn("Invalid Image HTTP result code - {}", Integer.valueOf(resultCode));
				else {
					String cType = rsp.headers().firstValue("Content-Type").orElse("unknown");
					isOK = _mimeTypes.contains(cType);
					if (!isOK)
						tLog.warn("Invalid MIME type for {} - {}", _img, cType);
					else
						tLog.info("Validated {} in {}ms", url.toString(), Long.valueOf(tt.getMillis()));
				}
			} catch (IllegalArgumentException iae) {
				if (url != null)
					tLog.warn("Known bad host - {}", url.getHost());
			} catch (URISyntaxException se) {
				tLog.warn("Invalid URL - {}", _img);
			} catch (IOException | InterruptedException ie) {
				tLog.warn("Error validating {} - {} {}", _img, ie.getClass().getSimpleName(), ie.getMessage());
				if ("Connection timed out".equals(ie.getMessage()))
					_invalidHosts.add(url.getHost());
			}

			// If not OK, push onto the error stack
			if (!isOK)
				_output.add(_img);
		}
	}

	/**
	 * Initializes the task.
	 */
	public ImageLinkTestTask() {
		super("Image URL Test", ImageLinkTestTask.class);
		_mimeTypes = (Collection<?>) SystemData.getObject("cooler.imgurls.mime_types");
	}

	@Override
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();

			// Get the images to check
			GetCoolerLinks dao = new GetCoolerLinks(con);
			Collection<Integer> ids = dao.getThreads();
			log.info("Validating images in {} discussion threads", Integer.valueOf(ids.size()));
			
			// Create the dead letter queues
			Collection<String> invalidHosts = Collections.synchronizedSet(new HashSet<String>());
			Queue<LinkedImage> badImgs = new ConcurrentLinkedQueue<LinkedImage>();

			// Load the images
			Collection<ImageLinkWorker> work = new ArrayList<ImageLinkWorker>();
			for (Integer id : ids) {
				Collection<LinkedImage> imgs = dao.getURLs(id.intValue(), false);
				imgs.stream().map(img -> new ImageLinkWorker(img, invalidHosts, badImgs)).forEach(work::add);
			}

			ctx.release();

			// Fire up the workers
			int tpSize = Math.max(1, Math.min(12, work.size() / 16));
			try (ThreadPoolExecutor exec = new ThreadPoolExecutor(tpSize, tpSize, 250, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), Thread.ofVirtual().factory())) {
				exec.allowCoreThreadTimeOut(true);
				work.forEach(exec::execute);
				exec.shutdown();
				exec.awaitTermination(5, TimeUnit.MINUTES);
			} catch (InterruptedException ie) {
				log.warn("Interrupted validating images");
			}

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
				upd.setDescription("Disabled linked image " + img.getURL());
				upd.setAuthorID(ctx.getUser().getID());

				// Get the update time
				Integer id = Integer.valueOf(img.getThreadID());
				if (updTimes.containsKey(id)) {
					long time = updTimes.get(id).longValue();
					time = Math.max(System.currentTimeMillis(), time + 1000);
					updTimes.put(id, Long.valueOf(time));
					upd.setDate(Instant.ofEpochMilli(time));
				} else {
					updTimes.put(id, Long.valueOf(System.currentTimeMillis()));
					upd.setDate(Instant.now());
				}

				// Write a thread update and delete the link
				msgdao.write(upd);
				wdao.disable(img.getThreadID(), img.getID());
			}

			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.atError().withThrowable(de).log("Error validating images - {}", de.getMessage());
		} finally {
			ctx.release();
		}

		log.info("Processing Complete");
	}
}
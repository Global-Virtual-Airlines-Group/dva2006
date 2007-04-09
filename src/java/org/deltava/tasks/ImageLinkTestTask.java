// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import java.io.IOException;

import java.sql.Connection;

import org.deltava.beans.Pilot;
import org.deltava.beans.cooler.*;
import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to validate the integrity of Water Cooler Image URLs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ImageLinkTestTask extends Task {

	private Collection _mimeTypes;

	/**
	 * Initializes the task.
	 */
	public ImageLinkTestTask() {
		super("Image URL Test", ImageLinkTestTask.class);
		_mimeTypes = (Collection) SystemData.getObject("cooler.imgurls.mime_types");
	}

	/**
	 * Executes the Task.
	 * @see org.deltava.taskman.Task#execute(TaskContext)
	 */
	protected void execute(TaskContext ctx) {
		try {
			Connection con = ctx.getConnection();
			
			// Figure out who we're operating as
			GetPilot pdao = new GetPilot(con);
			Pilot taskBy = pdao.getByName(SystemData.get("users.tasks_by"), SystemData.get("airline.db"));
			
			// Get the images to check
			GetCoolerLinks dao = new GetCoolerLinks(con);
			Collection<Integer> ids = dao.getThreads();
			log.info("Validating images in " + ids.size() + " discussion threads");
			ctx.release();

			// Keep track of invalid hosts
			Collection<String> invalidHosts = new HashSet<String>();

			// Loop through the threads
			for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
				Integer id = i.next();
				long lastUpdateTime = (System.currentTimeMillis() / 1000);

				// Get the images
				final Collection<LinkedImage> urls = dao.getURLs(id.intValue());
				for (Iterator<LinkedImage> ui = urls.iterator(); ui.hasNext();) {
					LinkedImage img = ui.next();
					boolean isOK = false;

					URL url = null;
					try {
						url = new URL(img.getURL());
						if (invalidHosts.contains(url.getHost()))
							throw new IllegalArgumentException("Bad Host!");

						HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
						urlcon.setRequestMethod("HEAD");
						urlcon.setReadTimeout(8250);
						urlcon.setConnectTimeout(8250);
						urlcon.connect();

						// Validate the result code
						int resultCode = urlcon.getResponseCode();
						if (resultCode != HttpURLConnection.HTTP_OK)
							log.warn("Invalid Image HTTP result code - " + resultCode);
						else {
							String cType = urlcon.getHeaderField("Content-Type");
							isOK = _mimeTypes.contains(cType);
							if (!isOK)
								log.warn("Invalid MIME type for " + img + " - " + cType);
						}

						urlcon.disconnect();
					} catch (IllegalArgumentException iae) {
						log.warn("Known bad host - " + url.getHost());
					} catch (MalformedURLException mue) {
						log.warn("Invalid URL - " + img);
					} catch (IOException ie) {
						log.warn("Error validating " + img + " - " + ie.getMessage());
						if ("Connection timed out".equals(ie.getMessage()))
							invalidHosts.add(url.getHost());
					}

					// If it's invalid, nuke it
					if (!isOK) {
						ThreadUpdate upd = new ThreadUpdate(id.intValue());
						upd.setMessage("Removed linked image " + img.getURL());
						upd.setAuthorID(taskBy.getID());
						if ((upd.getDate().getTime() / 1000) <= lastUpdateTime) {
							lastUpdateTime++;
							upd.setDate(new Date(lastUpdateTime * 1000));
						}
						
						// Get a connection
						con = ctx.getConnection();
						ctx.startTX();
						
						// Write a Thread update
						SetCoolerMessage msgdao = new SetCoolerMessage(con);
						msgdao.write(upd);
						
						// Delete the linked image
						SetCoolerLinks wdao = new SetCoolerLinks(con);
						wdao.delete(id.intValue(), img.getURL());
						
						// Commit
						ctx.commitTX();
						ctx.release();
					}
				}
			}
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error("Error validating images - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}

		// Log completion
		log.info("Processing Complete");
	}
}
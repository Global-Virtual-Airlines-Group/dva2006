// Copyright 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import java.io.IOException;

import org.deltava.dao.*;

import org.deltava.beans.cooler.LinkedImage;

import org.deltava.taskman.DatabaseTask;

import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to validate the integrity of Water Cooler Image URLs.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ImageLinkTestTask extends DatabaseTask {

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
	 * @see org.deltava.taskman.Task#execute()
	 */
	protected void execute() {
		try {
			GetCoolerLinks dao = new GetCoolerLinks(getConnection());
			final Collection<Integer> ids = dao.getThreads();
			log.info("Validating images in " + ids.size() + " discussion threads");
			release();

			// Keep track of invalid hosts
			final Collection<String> invalidHosts = new HashSet<String>();

			// Loop through the threads
			
			for (Iterator<Integer> i = ids.iterator(); i.hasNext();) {
				Integer id = i.next();

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
						urlcon.setReadTimeout(1950);
						urlcon.setConnectTimeout(1950);
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
						SetCoolerLinks wdao = new SetCoolerLinks(getConnection());
						wdao.delete(id.intValue(), img.getURL());
						release();
					}
				}
			}
		} catch (DAOException de) {
			log.error("Error validating images - " + de.getMessage(), de);
		} finally {
			release();
		}

		// Log completion
		log.info("Processing Complete");
	}
}
// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import java.io.IOException;

import org.deltava.dao.*;

import org.deltava.beans.cooler.LinkedImage;

import org.deltava.taskman.DatabaseTask;
import org.deltava.util.http.HttpTimeoutHandler;

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
			GetCoolerLinks dao = new GetCoolerLinks(_con);
			Collection<Integer> ids = dao.getThreads();
			
			// Loop through the threads
			SetCoolerLinks wdao = new SetCoolerLinks(_con);
			for (Iterator<Integer> i = ids.iterator(); i.hasNext(); ) {
				Integer id = i.next();
				
				// Get the images
				Collection<LinkedImage> urls = dao.getURLs(id.intValue());
				for (Iterator<LinkedImage> ui = urls.iterator(); ui.hasNext(); ) {
					LinkedImage img = ui.next();
					boolean isOK = true;
					
					try {
						URL url = new URL(null, img.getURL(), new HttpTimeoutHandler(1950));
						HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
						urlcon.setRequestMethod("HEAD");
						urlcon.connect();
						
						// Validate the result code
						int resultCode = urlcon.getResponseCode();
						if (resultCode != HttpURLConnection.HTTP_OK) {
							log.warn("Invalid Image HTTP result code - " + resultCode);
							isOK = false;
						} else {
							String cType = urlcon.getHeaderField("Content-Type");
							if (!_mimeTypes.contains(cType)) {
								log.warn("Invalid MIME type for " + img + " - " + cType);
								isOK = false;
							}
						}
						
						urlcon.disconnect();
					} catch (MalformedURLException mue) {
						isOK = false;
						log.warn("Invalid URL - " + img);
					} catch (IOException ie) {
						isOK = false;
						log.warn("Error validating " + img + " - " + ie.getMessage());
					}
					
					// If it's invalid, nuke it
					if (!isOK)
						wdao.delete(id.intValue(), img.getURL());
				}
			}
		} catch (DAOException de) {
			log.error("Error validating images - " + de.getMessage(), de);
		}

		// Log completion
		log.info("Processing Complete");
	}
}
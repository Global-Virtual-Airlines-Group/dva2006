// Copyright 2006, 2007, 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import java.io.IOException;

import java.sql.Connection;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.HeadMethod;

import org.deltava.beans.Pilot;
import org.deltava.beans.cooler.*;
import org.deltava.beans.system.VersionInfo;
import org.deltava.dao.*;
import org.deltava.taskman.*;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to validate the integrity of Water Cooler Image URLs.
 * @author Luke
 * @version 2.1
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
			HttpClient hc = new HttpClient();
			hc.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
			hc.getParams().setParameter("http.useragent",  VersionInfo.USERAGENT);
			hc.getParams().setParameter("http.tcp.nodelay", Boolean.TRUE);
			hc.getParams().setParameter("http.socket.timeout", new Integer(8250));
			hc.getParams().setParameter("http.connection.timeout", new Integer(8250));
			
			// Check the images
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
						
						// Open the connection
						HeadMethod hm = new HeadMethod(url.toExternalForm());
						hm.setFollowRedirects(false);

						// Validate the result code
						int resultCode = hc.executeMethod(hm);
						if (resultCode != HttpURLConnection.HTTP_OK)
							log.warn("Invalid Image HTTP result code - " + resultCode);
						else {
							Header[] hdrs = hm.getResponseHeaders("Content-Type");
							String cType = (hdrs.length == 0) ? "unknown" : hdrs[0].getValue();
							isOK = _mimeTypes.contains(cType);
							if (!isOK)
								log.warn("Invalid MIME type for " + img + " - " + cType);
						}
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
						upd.setDate(new Date());
						
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
						ThreadUtils.sleep(1000);
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
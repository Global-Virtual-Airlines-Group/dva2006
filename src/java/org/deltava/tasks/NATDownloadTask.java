// Copyright 2002, 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import javax.net.ssl.*;
import java.security.cert.*;

import java.util.Date;
import java.io.IOException;

import org.deltava.beans.schedule.OceanicRoute;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.taskman.*;

import org.deltava.util.http.SSLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download Oceanic Track data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class NATDownloadTask extends Task {

	/**
	 * Initializes the Scheduled Task.
	 */
	public NATDownloadTask() {
		super("NAT Download", NATDownloadTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute(TaskContext ctx) {
		try {
			HttpURLConnection con = null;
			
			// Create the URL connection to the NAT Download side
			URL url = new URL(SystemData.get("config.nat.url"));
			if ("https".equals(url.getProtocol())) {
				con = (HttpsURLConnection) url.openConnection();

				// Load a special keystore if necessary
				String keyStore = SystemData.get("config.nat.keystore");
				if (keyStore != null) {
					log.info("Loading custom SSL keystore " + keyStore);
					X509Certificate cert = SSLUtils.load(keyStore);
					SSLContext sslctx = SSLUtils.getContext(cert);
					((HttpsURLConnection) con).setSSLSocketFactory(sslctx.getSocketFactory());
				}
			} else {
				con = (HttpURLConnection) url.openConnection();				
			}
			
			// Build the oceanic route bean
			OceanicRoute or = new OceanicRoute(OceanicRoute.NAT);
			or.setDate(new Date());
			or.setSource(url.getHost());

			// Get the DAO and the NAT data
			log.info("Loading NAT track data from " + url.toString());
			TrackDAO dao = new GetNATs(con);
			or.setRoute(dao.getTrackInfo());

			// Write the route data to the database
			SetRoute wdao = new SetRoute(ctx.getConnection());
			wdao.write(or);
		} catch (CertificateException ce) {
			log.error("Cannot load SSL certificate - " + ce.getMessage());
		} catch (IOException ie) {
			log.error("Error downloading NAT Tracks - " + ie.getMessage(), ie);
		} catch (DAOException de) {
			log.error("Error saving NAT Data - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}
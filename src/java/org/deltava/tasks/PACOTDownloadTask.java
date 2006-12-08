// Copyright 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import javax.net.ssl.*;

import java.util.Date;
import java.io.IOException;
import java.security.cert.*;

import org.deltava.beans.schedule.OceanicRoute;

import org.deltava.dao.*;
import org.deltava.dao.file.*;

import org.deltava.taskman.DatabaseTask;

import org.deltava.util.http.SSLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download PACOT data.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class PACOTDownloadTask extends DatabaseTask {

	/**
	 * Initializes the Scheduled Task.
	 */
	public PACOTDownloadTask() {
		super("PACOT Download", PACOTDownloadTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute() {
		try {
			HttpURLConnection con = null;
			
			// Create the URL connection to the PACOT Download side
			URL url = new URL(SystemData.get("config.pacot.url"));
			if ("https".equals(url.getProtocol())) {
				con = (HttpsURLConnection) url.openConnection();

				// Load a special keystore if necessary
				String keyStore = SystemData.get("config.pacot.keystore");
				if (keyStore != null) {
					log.info("Loading custom SSL keystore " + keyStore);
					X509Certificate cert = SSLUtils.load(keyStore);
					SSLContext ctx = SSLUtils.getContext(cert);
					((HttpsURLConnection) con).setSSLSocketFactory(ctx.getSocketFactory());
				}
			} else {
				con = (HttpURLConnection) url.openConnection();				
			}
			
			// Build the oceanic route bean
			OceanicRoute or = new OceanicRoute(OceanicRoute.PACOT);
			or.setDate(new Date());
			or.setSource(url.getHost());

			// Get the DAO and the NAT data
			log.info("Loading PACOT track data from " + url.toString());
			TrackDAO dao = new GetPACOTs(con);
			or.setRoute(dao.getTrackInfo());

			// Write the route data to the database
			SetRoute wdao = new SetRoute(getConnection());
			wdao.write(or);
		} catch (CertificateException ce) {
			log.error("Cannot load SSL certificate - " + ce.getMessage());
		} catch (IOException ie) {
			log.error("Error downloading PACOT Tracks - " + ie.getMessage(), ie);
		} catch (DAOException de) {
			log.error("Error saving PACOT Data - " + de.getMessage(), de);
		} finally {
			release();
		}
	}
}
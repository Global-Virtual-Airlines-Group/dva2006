// Copyright 2006, 2007, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;

import java.util.Date;
import java.io.IOException;
import java.security.cert.*;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.taskman.*;

import org.deltava.util.http.SSLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download PACOT data.
 * @author Luke
 * @version 2.4
 * @since 1.0
 */

public class PACOTDownloadTask extends Task {

	/**
	 * Initializes the Scheduled Task.
	 */
	public PACOTDownloadTask() {
		super("PACOT Download", PACOTDownloadTask.class);
	}

	/**
	 * Executes the task.
	 */
	protected void execute(TaskContext ctx) {
		try {
			
			// Create the URL connection to the PACOT Download side
			URL url = new URL(SystemData.get("config.pacot.url"));
			
			// Build the oceanic route bean
			OceanicNOTAM or = new OceanicNOTAM(OceanicRoute.PACOT, new Date());
			or.setSource(url.getHost());
			
			// Load a key store if necessary
			String keyStore = SystemData.get("config.pacot.keystore");

			// Get the DAO and the PACOT data
			log.info("Loading PACOT track data from " + url.toString());
			GetPACOTs dao = new GetPACOTs(url.toExternalForm());
			if (("https".equals(url.getProtocol())) && (keyStore != null)) {
				log.info("Loading custom SSL keystore " + keyStore);
				X509Certificate cert = SSLUtils.load(keyStore);
				dao.setSSLContext(SSLUtils.getContext(cert));
			}
			
			// Download the track
			or.setRoute(dao.getTrackInfo());

			// Write the route data to the database
			SetRoute wdao = new SetRoute(ctx.getConnection());
			wdao.write(or);
		} catch (CertificateException ce) {
			log.error("Cannot load SSL certificate - " + ce.getMessage());
		} catch (IOException ie) {
			log.error("Error downloading PACOT Tracks - " + ie.getMessage(), ie);
		} catch (DAOException de) {
			log.error("Error saving PACOT Data - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}
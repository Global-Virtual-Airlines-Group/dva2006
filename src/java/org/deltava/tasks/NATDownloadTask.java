// Copyright 2002, 2005, 2006, 2007 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import javax.net.ssl.*;
import java.security.cert.*;

import java.io.IOException;
import java.sql.Connection;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

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
			HttpURLConnection sslcon = null;
			
			// Create the URL connection to the NAT Download side
			URL url = new URL(SystemData.get("config.nat.url"));
			if ("https".equals(url.getProtocol())) {
				sslcon = (HttpsURLConnection) url.openConnection();

				// Load a special keystore if necessary
				String keyStore = SystemData.get("config.nat.keystore");
				if (keyStore != null) {
					log.info("Loading custom SSL keystore " + keyStore);
					X509Certificate cert = SSLUtils.load(keyStore);
					SSLContext sslctx = SSLUtils.getContext(cert);
					((HttpsURLConnection) sslcon).setSSLSocketFactory(sslctx.getSocketFactory());
				}
			} else
				sslcon = (HttpURLConnection) url.openConnection();				
			
			// Build the oceanic route bean
			OceanicNOTAM or = new OceanicNOTAM(OceanicRoute.NAT, new Date());
			or.setDate(new Date());
			or.setSource(url.getHost());

			// Get the DAO and the NAT data
			log.info("Loading NAT track data from " + url.toString());
			GetNATs dao = new GetNATs(sslcon);
			or.setRoute(dao.getTrackInfo());
			
			// Get the waypoint data
			Map<String, Collection<String>> trackData = dao.getWaypoints();
			Collection<String> waypointIDs = new HashSet<String>();
			for (Iterator<Collection<String>> i = trackData.values().iterator(); i.hasNext(); ) {
				Collection<String> waypoints = i.next();
				for (Iterator<String> wi = waypoints.iterator(); wi.hasNext(); ) {
					String id = wi.next();
					if (!id.contains("/"))
						waypointIDs.add(id);
				}
			}

			// Get the connection
			Connection con = ctx.getConnection();
			
			// Get the intersection navdata values
			GetNavData nddao = new GetNavData(con);
			NavigationDataMap ndmap = nddao.getByID(waypointIDs);
			
			// Build the Route waypoints
			log.info("Building NAT track waypoints");
			Collection<OceanicWaypoints> oTracks = new ArrayList<OceanicWaypoints>();
			for (Iterator<String> i = trackData.keySet().iterator(); i.hasNext(); ) {
				String trackCode = i.next();
				Collection<String> codes = trackData.get(trackCode);
				OceanicWaypoints ot = new OceanicWaypoints(OceanicRoute.NAT, or.getDate());
				ot.setTrack(trackCode);
				
				// Calculate the location of the waypoint
				GeoLocation lastLoc = new GeoPosition(42, -30);
				for (Iterator<String> wi = codes.iterator(); wi.hasNext(); ) {
					String code = wi.next();
					NavigationDataBean ndb = (lastLoc == null) ? ndmap.get(code) : ndmap.get(code, lastLoc);
					if (ndb != null) {
						Intersection wp = new Intersection(ndb.getLatitude(), ndb.getLongitude());
						wp.setCode(code);
						ot.addWaypoint(wp);
						lastLoc = ndb;
					} else if (code.contains("/")) {
						try {
							Intersection wp = Intersection.parseNAT(code);
							ot.addWaypoint(wp);
							lastLoc = wp;
						} catch (IllegalArgumentException iae) {
							log.warn(iae.getMessage() + " - Track " + ot.getTrack());
						}
					}
						
				}
				
				oTracks.add(ot);
			}
			
			// Start a transaction
			ctx.startTX();
			
			// Write the route data to the database
			SetRoute wdao = new SetRoute(con);
			wdao.write(or);
			for (Iterator<OceanicWaypoints> i = oTracks.iterator(); i.hasNext(); )
				wdao.write(i.next());
			
			// Commit the transaction
			ctx.commitTX();
		} catch (CertificateException ce) {
			log.error("Cannot load SSL certificate - " + ce.getMessage());
		} catch (IOException ie) {
			log.error("Error downloading NAT Tracks - " + ie.getMessage(), ie);
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error("Error saving NAT Data - " + de.getMessage(), de);
		} finally {
			ctx.release();
		}
	}
}
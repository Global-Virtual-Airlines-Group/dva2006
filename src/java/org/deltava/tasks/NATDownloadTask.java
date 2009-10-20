// Copyright 2002, 2005, 2006, 2007, 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.net.*;
import java.util.*;
import java.security.cert.*;

import java.io.IOException;
import java.sql.Connection;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.navdata.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.taskman.*;

import org.deltava.util.http.SSLUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download Oceanic Track data.
 * @author Luke
 * @version 2.4
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
			URL url = new URL(SystemData.get("config.nat.url"));
			
			// Build the oceanic route bean
			OceanicNOTAM or = new OceanicNOTAM(OceanicRoute.NAT, new Date());
			or.setDate(new Date());
			or.setSource(url.getHost());
			
			// Load a key store if necessary
			String keyStore = SystemData.get("config.nat.keystore");

			// Get the DAO
			log.info("Loading NAT track data from " + url.toString());
			GetNATs dao = new GetNATs(url.toExternalForm());
			if (("https".equals(url.getProtocol())) && (keyStore != null)) {
				log.info("Loading custom SSL keystore " + keyStore);
				X509Certificate cert = SSLUtils.load(keyStore);
				dao.setSSLContext(SSLUtils.getContext(cert));
			}
			
			// Get the waypoint data
			or.setRoute(dao.getTrackInfo());
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
			for (Iterator<Map.Entry<String, Collection<String>>> i = trackData.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<String, Collection<String>> e = i.next();
				OceanicWaypoints ot = new OceanicWaypoints(OceanicRoute.NAT, or.getDate());
				ot.setTrack(e.getKey());
				
				// Calculate the location of the waypoint
				GeoLocation lastLoc = new GeoPosition(42, -30);
				for (Iterator<String> wi = e.getValue().iterator(); wi.hasNext(); ) {
					String code = wi.next();
					NavigationDataBean ndb = (lastLoc == null) ? ndmap.get(code) : ndmap.get(code, lastLoc);
					if (ndb != null) {
						NavigationDataBean nd = NavigationDataBean.create(ndb.getType(), ndb.getLatitude(), ndb.getLongitude());
						nd.setCode(ndb.getCode());
						nd.setRegion(ndb.getRegion());
						ot.addWaypoint(nd);
						lastLoc = ndb;
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
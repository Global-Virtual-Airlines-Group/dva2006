// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import java.io.*;
import java.util.List;
import java.util.SequencedCollection;

import org.json.JSONObject;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.*;

import org.deltava.beans.flight.DatabaseID;
import org.deltava.beans.servinfo.PositionData;

import org.deltava.dao.file.*;

import org.deltava.util.*;

/**
 * A utility class to serialize flight data.
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

@Helper(DataExportService.class)
class DataSerializer {

	private static final Logger log = LogManager.getLogger(DataSerializer.class);

	/**
	 * Serializes a FlightData record.
	 * @param fd the Flight Data
	 * @return a FlightJS record
	 */
	static FlightJS serialize(FlightData fd) {
		
		// Serialize the flight report
		JSONObject fo = new JSONObject();
		fo.put("flight", JSONFlightExport.format(fd.aircraft(), fd.PIREP()));

		// Load serialized positions
		File pf = ArchiveHelper.getPositions(fd.PIREP().getDatabaseID(DatabaseID.ACARS));
		if (fd.positions().isEmpty() && (fd.error() == null) && pf.exists()) {
			try {
				Compression c = Compression.detect(pf);
				try (InputStream is = c.getStream(new BufferedInputStream(new FileInputStream(pf), 32768))) {
					GetSerializedPosition posdao = new GetSerializedPosition(is);	
					SequencedCollection<? extends RouteEntry> pts = posdao.read();
					pts.forEach(re -> fo.accumulate("data", JSONFlightExport.format(fd.PIREP().getFDR(), re)));
				}
			} catch (Exception e) {
				fo.put("error", e.getMessage());
				log.atError().withThrowable(e).log("Error reading serialized positions for Flight {} - {}", Integer.valueOf(fd.getID()), e.getMessage());
			}
		} else {
			fo.putOpt("error", fd.error());
			fd.positions().forEach(re -> fo.accumulate("data", JSONFlightExport.format(fd.PIREP().getFDR(), re)));
		}
		
		// Add positions
		JSONUtils.ensureArrayPresent(fo, "data");
		
		// Load serialized route data
		File rf = ArchiveHelper.getRoute(fd.getID());
		if (!StringUtils.isEmpty(fd.PIREP().getRoute()) && rf.exists()) {
			JSONObject ro = new JSONObject();
			try (InputStream is = new BufferedInputStream(new FileInputStream(rf), 4096)) {
				GetSerializedRoute rtdao = new GetSerializedRoute(is);
				ArchivedRoute rt = rtdao.read();
				ro.put("airac", rt.getAIRACVersion());
				ro.put("text", rt.getRoute());
				rt.getWaypoints().forEach(wp -> ro.accumulate("pts", JSONFlightExport.format(wp)));
				JSONUtils.ensureArrayPresent(ro, "pts");
			} catch (Exception e) {
				ro.put("error", e.getMessage());
				log.atError().withThrowable(e).log("Error reading serialized route for Flight {} - {}", Integer.valueOf(fd.getID()), e.getMessage());
			} finally {
				fo.put("route", ro);
			}
		}
		
		// Load serialized online positions
		File of = ArchiveHelper.getOnline(fd.getID());
		if (of.exists()) {
			JSONObject oo = new JSONObject();
			try (InputStream is = new BufferedInputStream(new FileInputStream(of), 4096)) {
				GetSerializedOnline otdao = new GetSerializedOnline(is);
				List<PositionData> pts = otdao.read();
				oo.put("network", fd.PIREP().getNetwork());
				pts.forEach(pd -> oo.accumulate("pts", JSONFlightExport.format(pd)));
				JSONUtils.ensureArrayPresent(oo, "pts");
			} catch (Exception e) {
				oo.put("error", e.getMessage());
				log.atError().withThrowable(e).log("Error reading serialized online track for Flight {} - {}", Integer.valueOf(fd.getID()), e.getMessage());
			} finally {
				fo.put("onlineTrack", oo);
			}
		}
		
		return new FlightJS(fd.PIREP().getID(), fo.toString(2));
	}
}
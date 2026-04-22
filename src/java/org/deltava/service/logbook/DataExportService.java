// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.*;
import java.sql.Connection;
import java.time.Instant;
import java.nio.file.attribute.FileTime;

import org.json.JSONObject;
import org.apache.logging.log4j.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.servinfo.PositionData;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Web Service to export complete Log Book data. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class DataExportService extends WebService {
	
	private static final Logger log = LogManager.getLogger(DataExportService.class);
	
	private static final Cache<CacheableCollection<FlightReport>> _cache = CacheManager.getCollection(FlightReport.class, "Logbook");
	
	private record FlightData (FlightReport PIREP, Aircraft aircraft, SequencedCollection<RouteEntry> positions, String error) {}
	private record FlightJS (int id, String js) {}
		
	/**
	 * Executes the Web Service.
	 * @param ctx the Web Service context
	 * @return the HTTP status code
	 * @throws ServiceException if an error occurs
	 */
	@Override
	public int execute(ServiceContext ctx) throws ServiceException {
		
		// Get the Pilot ID
		int userID = ctx.getUser().getID();
		if (ctx.isUserInRole("HR"))
			userID = StringUtils.parse(ctx.getParameter("id"), userID);
		
		IntervalTaskTimer tt = new IntervalTaskTimer();
		LogbookSearchCriteria lsc = new LogbookSearchCriteria("DATE, PR.SUBMITTED", ctx.getDB());
		lsc.setLoadComments(true);
		try {
			Connection con = ctx.getConnection();
			
			// Load the Pilot and aircraft profiles
			GetPilot pdao = new GetPilot(con);
			GetAircraft acdao = new GetAircraft(con);
			Pilot p = pdao.get(userID);
			Map<String,Aircraft> acTypes = CollectionUtils.createMap(acdao.getAircraftTypes(), Aircraft::getName);
			tt.mark("data");
			
			// Get the Flight Reports for the Pilot
			GetFlightReports frdao = new GetFlightReports(con);
			CacheableCollection<FlightReport> pireps = _cache.get(Integer.valueOf(userID));
			if (pireps == null) {
				pireps = new CacheableList<FlightReport>(Integer.valueOf(userID));
				pireps.addAll(frdao.getByPilot(userID, lsc));
				_cache.add(pireps);
			}
					
			// Remove flights not completed and scored
			pireps.removeIf(fr -> !fr.getStatus().getIsComplete());
			frdao.loadCaptEQTypes(userID, pireps, ctx.getDB());
			tt.mark("logbook");
			
			// Load flight data
			Collection<FlightData> work = new ArrayList<FlightData>();
			GetACARSData fidao = new GetACARSData(con);
			GetACARSPositions posdao = new GetACARSPositions(con);
			for (FlightReport fr : pireps) {
				Aircraft ac = acTypes.get(fr.getEquipmentType());
				SequencedCollection<RouteEntry> rtData = new ArrayList<RouteEntry>();
				String error = null;
				
				// Deserialize the positions
				if (fr.hasAttribute(Attribute.ACARS)) {
					FlightInfo fi = fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
					if (fi == null)
						error = String.format("No ACARS Flight for Flight Report %d (ACARS ID = %d)", Integer.valueOf(fr.getID()), Integer.valueOf(fr.getDatabaseID(DatabaseID.ACARS)));
					else 
						rtData.addAll(posdao.getRouteEntries(userID, fi.getArchived()));
				}
				
				work.add(new FlightData(fr, ac, rtData, error));
			}
			
			// Serialize in a multi-threaded fashion
			tt.mark("flightdata");
			Collection<FlightJS> jsData = work.parallelStream().map(DataExportService::serialize).collect(Collectors.toList());
			
			// Write to ZIP file
			tt.mark("serialize");
			FileTime now = FileTime.from(Instant.now());
			ByteArrayOutputStream bos = new ByteArrayOutputStream(262144);
			try (ZipOutputStream zout = new ZipOutputStream(bos)) {
				for (Iterator<FlightJS> i = jsData.iterator(); i.hasNext(); ) {
					FlightJS js = i.next();
					ZipEntry ze = new ZipEntry(String.valueOf(js.id()) + ".json");
					ze.setMethod(ZipEntry.DEFLATED);
					ze.setCreationTime(now);
					zout.putNextEntry(ze);
					PrintWriter pw = new PrintWriter(zout);
					pw.print(js.js());
					pw.flush();
					i.remove();
				}
			}
			
			// Dump to the output stream
			tt.stop();
			log.error("Timings = {}", tt);
			ctx.setHeader("Content-disposition", String.format("attachment; filename=FlightData_%s.zip", p.getPilotCode()));
			ctx.setHeader("Content-Length", bos.size());
			ctx.setContentType("application/zip");
			ctx.setExpiry(1800);
			try (OutputStream os = ctx.getResponse().getOutputStream()) {
				os.write(bos.toByteArray());
				os.flush();
			}
		} catch (IOException | DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		return SC_OK;
	}

	/*
	 * Helper method to serialize data into JSON.
	 */
	private static FlightJS serialize(FlightData fd) {

		// Serialize the flight report and positions
		JSONObject fo = new JSONObject();
		fo.put("flight", JSONFlightExport.format(fd.aircraft(), fd.PIREP));
		fo.putOpt("error", fd.error());
		fd.positions().forEach(re -> fo.accumulate("data", JSONFlightExport.format(fd.PIREP.getFDR(), re)));
		JSONUtils.ensureArrayPresent(fo, "data");

		// Load serialized route data -- Move to outsade
		File rf = ArchiveHelper.getRoute(fd.PIREP.getID());
		if (!StringUtils.isEmpty(fd.PIREP.getRoute()) && rf.exists()) {
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
			} finally {
				fo.put("route", ro);
			}
		}

		// Load serialized online positions
		File of = ArchiveHelper.getOnline(fd.PIREP.getID());
		if (of.exists()) {
			JSONObject oo = new JSONObject();
			try (InputStream is = new BufferedInputStream(new FileInputStream(of), 4096)) {
				GetSerializedOnline otdao = new GetSerializedOnline(is);
				List<PositionData> pts = otdao.read();
				oo.put("network", fd.PIREP.getNetwork());
				pts.forEach(pd -> oo.accumulate("pts", JSONFlightExport.format(pd)));
				JSONUtils.ensureArrayPresent(oo, "pts");
			} catch (Exception e) {
				oo.put("error", e.getMessage());
			} finally {
				fo.put("onlineTrack", oo);
			}
		}
		
		
		return new FlightJS(fd.PIREP.getID(), fo.toString(2));
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}
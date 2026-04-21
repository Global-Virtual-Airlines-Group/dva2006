// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.sql.Connection;
import java.time.Instant;
import java.nio.file.attribute.FileTime;

import org.json.JSONObject;

import org.apache.logging.log4j.*;

import org.deltava.beans.acars.*;
import org.deltava.beans.acars.FlightInfo;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Aircraft;
import org.deltava.beans.servinfo.PositionData;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.service.*;
import org.deltava.util.*;

/**
 * A Web Service to export complete Log Book data. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class DataExportService extends WebService {
	
	private static final Logger log = LogManager.getLogger(DataExportService.class);
	
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
			userID = StringUtils.parse(ctx.getParameter("id"), 0);
		
		LogbookSearchCriteria lsc = new LogbookSearchCriteria("DATE, PR.SUBMITTED", ctx.getDB());
		lsc.setLoadComments(true);
		try {
			Connection con = ctx.getConnection();
			
			// Load aircraft profiles
			GetAircraft acdao = new GetAircraft(con);
			Map<String,Aircraft> acTypes = CollectionUtils.createMap(acdao.getAircraftTypes(), Aircraft::getName);
			
			// Get the Flight Reports for the Pilot
			GetFlightReports frdao = new GetFlightReports(con);
			Collection<FlightReport> pireps = frdao.getByPilot(userID, lsc);
			pireps.removeIf(fr -> !fr.getStatus().getIsComplete());
			frdao.loadCaptEQTypes(userID, pireps, ctx.getDB());
			
			// Loop through the flights
			Collection<FlightJS> jsData = new ArrayList<FlightJS>();
			GetACARSData fidao = new GetACARSData(con);
			for (FlightReport fr : pireps) {
				Aircraft ac = acTypes.get(fr.getEquipmentType());
				
				// Serialize the flight
				JSONObject fo = new JSONObject();
				fo.put("flight", JSONFlightExport.format(ac, fr));
				if (fr.hasAttribute(Attribute.ACARS) && (fr instanceof FDRFlightReport afr)) {
					FlightInfo fi = fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
					if (fi == null) {
						log.warn("No ACARS Flight for Flight Report {} (ACARS ID = {})", Integer.valueOf(fr.getID()), Integer.valueOf(fr.getDatabaseID(DatabaseID.ACARS)));
						continue;
					}
					
					// Load Positions
					GetACARSPositions posdao = new GetACARSPositions(con);
					List<ACARSRouteEntry> pts = posdao.getRouteEntries(userID, fi.getArchived());
					pts.forEach(re -> fo.accumulate("data", JSONFlightExport.format(afr.getFDR(), re)));
					JSONUtils.ensureArrayPresent(fo, "data");
				}
				
				// Load serialized route data -- Move to outsade
				File rf = ArchiveHelper.getRoute(fr.getID());
				if (!StringUtils.isEmpty(fr.getRoute()) && rf.exists()) {
					JSONObject ro = new JSONObject();
					try (InputStream is = new BufferedInputStream(new FileInputStream(rf), 4096)) {
						GetSerializedRoute rtdao = new GetSerializedRoute(is);
						ArchivedRoute rt = rtdao.read();
						ro.put("airac", rt.getAIRACVersion());
						ro.put("text", rt.getRoute());
						rt.getWaypoints().forEach(wp -> ro.accumulate("pts", JSONFlightExport.format(wp)));
						JSONUtils.ensureArrayPresent(ro, "pts");
					} catch (IOException ie) {
						ro.put("error", ie.getMessage());
					} finally {
						fo.put("route", ro);
					}
				}
				
				// Get online positions
				File of = ArchiveHelper.getOnline(fr.getID());
				if (of.exists()) {
					JSONObject oo = new JSONObject();
					try (InputStream is = new BufferedInputStream(new FileInputStream(of), 4096)) {
						GetSerializedOnline otdao = new GetSerializedOnline(is);
						List<PositionData> pts = otdao.read();
						oo.put("network", fr.getNetwork());
						pts.forEach(pd -> oo.accumulate("pts", JSONFlightExport.format(pd)));
						JSONUtils.ensureArrayPresent(oo, "pts");
					} catch (IOException ie) {
						oo.put("error", ie.getMessage());
					} finally {
						fo.put("onlineTrack", oo);
					}
				}

				// Convert to JSON
				String js = fo.toString(2);
				jsData.add(new FlightJS(fr.getID(), js));
			}
			
			// Write to ZIP file
			FileTime now = FileTime.from(Instant.now());
			ByteArrayOutputStream bos = new ByteArrayOutputStream(262144);
			try (ZipOutputStream zout = new ZipOutputStream(bos)) {
				for (Iterator<FlightJS> i = jsData.iterator(); i.hasNext(); ) {
					FlightJS js = i.next();
					ZipEntry ze = new ZipEntry(String.valueOf(js.id()) + ".xml");
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
			ctx.setHeader("Content-disposition", String.format("attachment; filename=FlightData_%s.zip", Integer.toHexString(userID)));
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

	@Override
	public final boolean isSecure() {
		return true;
	}
}
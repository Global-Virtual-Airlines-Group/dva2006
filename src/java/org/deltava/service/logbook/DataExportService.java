// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import java.sql.Connection;
import java.time.Instant;
import java.nio.file.attribute.FileTime;

import org.deltava.beans.Pilot;
import org.deltava.beans.acars.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.Aircraft;

import org.deltava.dao.*;
import org.deltava.service.*;
import org.deltava.util.*;
import org.deltava.util.cache.*;

/**
 * A Web Service to export complete Log Book data. 
 * @author Luke
 * @version 12.4
 * @since 12.4
 */

public class DataExportService extends DownloadService {
	
	private static final Cache<CacheableCollection<FlightReport>> _cache = CacheManager.getCollection(FlightReport.class, "Logbook");
	
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
		
		Pilot p = null;
		Collection<FlightData> work = new ArrayList<FlightData>();
		try {
			Connection con = ctx.getConnection();
			
			// Load the Pilot and aircraft profiles
			GetPilot pdao = new GetPilot(con);
			GetAircraft acdao = new GetAircraft(con);
			p = pdao.get(userID);
			Map<String,Aircraft> acTypes = CollectionUtils.createMap(acdao.getAircraftTypes(), Aircraft::getName);
			
			// Get the Flight Reports for the Pilot
			GetFlightReports frdao = new GetFlightReports(con);
			CacheableCollection<FlightReport> pireps = _cache.get(Integer.valueOf(userID));
			if (pireps == null) {
				LogbookSearchCriteria lsc = new LogbookSearchCriteria("DATE, PR.SUBMITTED", ctx.getDB());
				lsc.setLoadComments(true);
				
				pireps = new CacheableList<FlightReport>(Integer.valueOf(userID));
				pireps.addAll(frdao.getByPilot(userID, lsc));
				_cache.add(pireps);
			}
					
			// Remove flights not completed and scored
			pireps.removeIf(fr -> !fr.getStatus().getIsComplete());
			frdao.loadCaptEQTypes(userID, pireps, ctx.getDB());
			
			// Load flight data
			GetACARSData fidao = new GetACARSData(con);
			GetACARSPositions posdao = new GetACARSPositions(con);
			for (FlightReport fr : pireps) {
				SequencedCollection<RouteEntry> rtData = new ArrayList<RouteEntry>();
				String error = null;
				
				// Deserialize the positions
				if (fr.hasAttribute(Attribute.ACARS)) {
					FlightInfo fi = fidao.getInfo(fr.getDatabaseID(DatabaseID.ACARS));
					if (fi == null)
						error = String.format("No ACARS Flight for Flight Report %d (ACARS ID = %d)", Integer.valueOf(fr.getID()), Integer.valueOf(fr.getDatabaseID(DatabaseID.ACARS)));
					else if (!fi.getArchived())
						rtData.addAll(posdao.getRouteEntries(userID, fi.getArchived()));
				}

				Aircraft ac = acTypes.get(fr.getEquipmentType());
				FlightData fd = new FlightData(fr, ac, rtData, error);
				work.add(fd);
			}
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Abort if no flights
		if (work.isEmpty()) return SC_NOT_FOUND;
		
		// Load the data and write to the ZIP file
		File pth = new File(System.getProperty("java.io.tmpdir"), "export"); pth.mkdirs(); File df = null;
		try {
			df = File.createTempFile("dataExport", "zip", pth);
			try (OutputStream os = new BufferedOutputStream(new FileOutputStream(df), 65536); ZipOutputStream zout = new ZipOutputStream(os)) {
				for (Iterator<FlightData> i = work.iterator(); i.hasNext(); ) {
					FlightData fd = i.next();
					FlightJS js = DataSerializer.serialize(fd);
					ZipEntry ze = new ZipEntry(String.valueOf(js.id()) + ".json");
					ze.setMethod(ZipEntry.DEFLATED);
					ze.setCreationTime(FileTime.from(Instant.now()));
					zout.putNextEntry(ze);
					PrintWriter pw = new PrintWriter(zout);
					pw.print(js.js());
					pw.flush();
					i.remove();
				}
			}

			// Dump to the output stream
			ctx.setHeader("Content-disposition", String.format("attachment; filename=FlightData_%s.zip", p.getPilotCode()));
			ctx.setContentType("application/zip");
			//ctx.setExpiry(1800);
			sendFile(df, ctx.getResponse(), false);
		} catch (IOException ie) {
			throw error(SC_CONFLICT, "I/O Error", false);			
		} finally {
			if (df != null) df.delete();
		}
		
		return SC_OK;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}
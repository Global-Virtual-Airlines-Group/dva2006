// Copyright 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.service.logbook;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import java.util.concurrent.*;

import java.sql.Connection;
import java.time.Instant;
import java.nio.file.attribute.FileTime;

import org.apache.logging.log4j.*;

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
	
	private static final Logger log = LogManager.getLogger(DataExportService.class);
	
	private static final Cache<CacheableCollection<FlightReport>> _cache = CacheManager.getCollection(FlightReport.class, "Logbook");
	
	/*
	 * This is a hack. Ordinarily we would do a map() on a queue, but stream iterators do not mutate their underlying stream and we will
	 * OOM. Therefore, we submit work into a ThreadPoolExecutor which should allow the raw data to be garbage collected post serialization. 
	 */
	private class ExportWork implements Callable<Void> {
		private final FlightData _fd;
		private final Queue<FlightJS> _out;
		
		ExportWork(FlightData fd, Queue<FlightJS> out) {
			super();
			_fd = fd;
			_out= out;
		}

		@Override
		public Void call() throws Exception {
			FlightJS js = DataSerializer.serialize(_fd);
			_out.add(js);
			return null;
		}
	}
	
	/**
	 * Helper method to asynchronously write JSON data to a ZIP file. 
	 */
	private class ZIPWorker implements Runnable {
		private final BlockingQueue<FlightJS> _work;
		private File _f;
		
		ZIPWorker(BlockingQueue<FlightJS> work) {
			super();
			_work = work;
		}
		
		@Override
		public String toString() {
			return "ZIPWorker";
		}
		
		File getFile() {
			return _f;
		}
		
		@Override
		public void run() {
			try {
				_f = File.createTempFile("dataExport", "zip");
				try (OutputStream os = new BufferedOutputStream(new FileOutputStream(_f), 65536); ZipOutputStream zout = new ZipOutputStream(os)) {
					while (!Thread.currentThread().isInterrupted()) {
						FlightJS js = _work.take();
						log.info("Wrote Flight {} to ZIP", Integer.valueOf(js.id()));
						ZipEntry ze = new ZipEntry(String.valueOf(js.id()) + ".json");
						ze.setMethod(ZipEntry.DEFLATED);
						ze.setCreationTime(FileTime.from(Instant.now()));
						zout.putNextEntry(ze);
						PrintWriter pw = new PrintWriter(zout);
						pw.print(js.js());
						pw.flush();
					}
				}
			} catch (IOException ie) {
				log.atError().withThrowable(ie).log("Error writing ZIP file - {}", ie.getMessage());
			} catch (InterruptedException ixe) {
				log.info("ZIPWorker Interrupted");
			}
		}
	}
	
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
		IntervalTaskTimer tt = new IntervalTaskTimer();
		try {
			Connection con = ctx.getConnection();
			
			// Load the Pilot and aircraft profiles
			GetPilot pdao = new GetPilot(con);
			GetAircraft acdao = new GetAircraft(con);
			p = pdao.get(userID);
			Map<String,Aircraft> acTypes = CollectionUtils.createMap(acdao.getAircraftTypes(), Aircraft::getName);
			tt.mark("data");
			
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
			tt.mark("logbook");
			
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
			
			tt.mark("positions");
		} catch (DAOException de) {
			throw error(SC_INTERNAL_SERVER_ERROR, de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Abort if no flights
		if (work.isEmpty()) return SC_NOT_FOUND;
		
		// Build the worker
		BlockingQueue<FlightJS> outWork = new LinkedBlockingQueue<FlightJS>();
		ZIPWorker zw = new ZIPWorker(outWork);
		Thread zwt = new Thread(zw, zw.toString());
		zwt.setDaemon(true);
		
		// Serialize in a multi-threaded fashion
		int poolSize = Runtime.getRuntime().availableProcessors();
		try (ThreadPoolExecutor exec = new ThreadPoolExecutor(poolSize, poolSize, 100, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())) {
			work.stream().map(fd -> new ExportWork(fd, outWork)).forEach(exec::submit);
			tt.mark("submit");
			work.clear();
			zwt.start();
			exec.shutdown();
			exec.awaitTermination(150, TimeUnit.SECONDS);
			zwt.interrupt();
			tt.mark("serialize");
		} catch (InterruptedException ie) {
			log.atError().withThrowable(ie).log("Error executing ThreadPool - {}", ie.getMessage());
		}
		
		// Dump to the output stream
		tt.stop();
		log.error("Timings = {}", tt);
		File df = zw.getFile();
		ctx.setHeader("Content-disposition", String.format("attachment; filename=FlightData_%s.zip", p.getPilotCode()));
		ctx.setHeader("Content-Length", (int)df.length());
		ctx.setContentType("application/zip");
		//ctx.setExpiry(1800);
		sendFile(df, ctx.getResponse());

		return SC_OK;
	}

	@Override
	public final boolean isSecure() {
		return true;
	}
}
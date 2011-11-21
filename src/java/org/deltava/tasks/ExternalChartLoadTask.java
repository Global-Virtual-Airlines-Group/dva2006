// Copyright 2011 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import static java.net.HttpURLConnection.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.taskman.*;

import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to load external approach charts.
 * @author Luke
 * @version 4.1
 * @since 4.0
 */

public class ExternalChartLoadTask extends Task {
	
	/**
	 * Non-fatal error codes
	 */
	protected static final Collection<Integer> NONFATAL_CODES = Arrays.asList(Integer.valueOf(HTTP_BAD_METHOD), Integer.valueOf(HTTP_BAD_REQUEST),
		Integer.valueOf(HTTP_FORBIDDEN), Integer.valueOf(HTTP_MOVED_TEMP), Integer.valueOf(HTTP_MOVED_PERM));
	
	private class ChartInfoWorker extends Thread {
		private final Logger tLog;
		private final Queue<ExternalChart> _work;
		private final Queue<ExternalChart> _results;
		
		ChartInfoWorker(int id, Queue<ExternalChart> work, Queue<ExternalChart> results) {
			super("ChartInfo-" + String.valueOf(id));
			setDaemon(true);
			tLog = Logger.getLogger(ExternalChartLoadTask.class.getPackage().getName() + "." + getName());
			_work = work;
			_results = results;
		}
		
		@Override
		public void run() {
			GetExternalCharts ecdao = new GetExternalCharts();
			ecdao.setConnectTimeout(2500);
			ecdao.setReadTimeout(4000);
			
			ExternalChart ec = _work.poll();
			while (ec != null) {
				String icao = ec.getAirport().getICAO();
				try {
					ecdao.populate(ec);
					tLog.info("Populated " + icao + " " + ec.getName());
					_results.add(ec);
				} catch (HTTPDAOException hde) {
					Integer rc = Integer.valueOf(hde.getStatusCode());
					if (NONFATAL_CODES.contains(rc)) {
						try {
							ecdao.load(ec);
							_results.add(ec);
						} catch (DAOException de) {
							tLog.warn("Error loading " + icao + " " + ec.getName() + " - " + de.getMessage());	
						}
					} else
						tLog.warn("Error populating " + icao + " " + ec.getName() + " - " + hde.getMessage());
				} catch (DAOException de) {
					tLog.warn("Error populating " + icao + " " + ec.getName() + " - " + de.getMessage());
				}
				
				ec = isInterrupted() ? null : _work.poll();
			}
		}
	}

	private class AirportChartWorker extends Thread {
		private final Logger tLog;
		private final Queue<Airport> _work;
		private final Queue<Airport> _loaded;
		private final Queue<ExternalChart> _results;
		
		AirportChartWorker(int id, Queue<Airport> work, Queue<Airport> loaded, Queue<ExternalChart> results) {
			super("AirportChart-" + String.valueOf(id));
			setDaemon(true);
			tLog = Logger.getLogger(ExternalChartLoadTask.class.getPackage().getName() + "." + getName());
			_work = work;
			_loaded = loaded;
			_results = results;
		}
		
		@Override
		public void run() {
			GetAirCharts ecdao = new GetAirCharts();
			ecdao.setConnectTimeout(2500);
			ecdao.setReadTimeout(5000);
			
			Airport a = _work.poll();
			while (a != null) {
				try {
					Collection<ExternalChart> exC = ecdao.getCharts(a);
					_results.addAll(exC);
					_loaded.add(a);
					tLog.info("Loaded " + exC.size() + " charts for " + a);
				} catch (DAOException de) {
					tLog.error("Error loading charts for " + a + " - " + de.getMessage());
				}
				
				a = isInterrupted() ? null : _work.poll();
			}
		}
	}
	
	/**
	 * Initializes the Scheduled Task.
	 */
	public ExternalChartLoadTask() {
		super("External Chart Loader", ExternalChartLoadTask.class);
	}

	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		// Get countries
		Map<?, ?> chartCountries = (Map<?, ?>) SystemData.getObject("schedule.chart.sources");
		if ((chartCountries == null) || chartCountries.isEmpty())
			log.warn("No Chart Sources defined");
		
		int maxAge = SystemData.getInt("schedule.chart.maxAge", 31);
		try {
			String defaultSource = SystemData.get("schedule.chart.default");
			
			// Load AirCharts countries
			GetAirCharts acdao = new GetAirCharts();
			Collection<Country> countries = acdao.getCountries();
			for (Iterator<Country> i = countries.iterator(); i.hasNext(); ) {
				Country c = i.next();
				String src = (String) chartCountries.get(c.getCode().toLowerCase());
				if (src == null)
					src = defaultSource;
				
				if (!"AirCharts".equals(src))
					i.remove();
			}
			
			// Get the airports for each country
			Queue<ExternalChart> extCharts = new LinkedBlockingQueue<ExternalChart>();
			Queue<Airport> loadedAirports = new LinkedBlockingQueue<Airport>();
			for (Iterator<Country> i = countries.iterator(); i.hasNext(); ) {
				Country c = i.next();
				Queue<Airport> ecAirports = new LinkedBlockingQueue<Airport>(acdao.getAirports(c));
				log.info("Loaded " + ecAirports.size() + " Airports for " + c);
				
				// Go through each airport, and check the max age
				Connection con = ctx.getConnection();
				GetChart cdao = new GetChart(con);
				for (Iterator<Airport> ai = ecAirports.iterator(); ai.hasNext(); ) {
					Airport a = ai.next();
					int maxChartAge = cdao.getMaxAge(a);
					if ((maxChartAge != -1) && (maxChartAge < maxAge))
						ai.remove();
				}

				// Release connection
				ctx.release();
				
				// Load the Charts for the airports
				int tpSize = Math.min(4, (ecAirports.size() / 4));
				Collection<Thread> workers = new ArrayList<Thread>();
				for (int x = 1; x <= tpSize; x++) {
					AirportChartWorker wrk = new AirportChartWorker(x, ecAirports, loadedAirports, extCharts);
					wrk.setUncaughtExceptionHandler(this);
					workers.add(wrk);
					wrk.start();
				}
				
				// Wait for the workers to finish
				ThreadUtils.waitOnPool(workers);
			}
			
			// Populate the charts for each airport, using a thread pool
			Queue<ExternalChart> results = new LinkedBlockingQueue<ExternalChart>();
			int tpSize = Math.min(12, (extCharts.size() / 16));
			Collection<Thread> workers = new ArrayList<Thread>();
			for (int x = 1; x <= tpSize; x++) {
				ChartInfoWorker wrk = new ChartInfoWorker(x, extCharts, results);
				wrk.setUncaughtExceptionHandler(this);
				workers.add(wrk);
				wrk.start();
			}
			
			// Wait for the workers to finish
			ThreadUtils.waitOnPool(workers);
			
			// Get a connection and start a transaction
			Connection con = ctx.getConnection();
			ctx.startTX();
			
			// Clear out the loaded airports
			SetChart cwdao = new SetChart(con);
			for (Airport a : loadedAirports)
				cwdao.purge(a);
			
			// Write the charts
			for (ExternalChart ec : results)
				cwdao.writeExternal(ec);
			
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		log.info("Processing Complete");
	}
}
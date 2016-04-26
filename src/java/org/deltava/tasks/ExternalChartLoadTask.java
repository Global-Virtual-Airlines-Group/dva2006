// Copyright 2011, 2012, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import static java.net.HttpURLConnection.*;

import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to load external approach charts.
 * @author Luke
 * @version 7.0
 * @since 4.0
 */

public class ExternalChartLoadTask extends Task {
	
	/**
	 * Non-fatal error codes
	 */
	protected static final Collection<Integer> NONFATAL_CODES = Arrays.asList(Integer.valueOf(HTTP_BAD_METHOD), Integer.valueOf(HTTP_BAD_REQUEST),
		Integer.valueOf(HTTP_FORBIDDEN), Integer.valueOf(HTTP_MOVED_TEMP), Integer.valueOf(HTTP_MOVED_PERM));
	
	private abstract class ChartWork implements Runnable {
		protected Logger tLog;
		
		protected ChartWork() {
			super();
		}
		
		protected void initLogger() {
			String tName = Thread.currentThread().getName();
			String workerID = tName.substring(tName.lastIndexOf('-') + 1);
			tLog = Logger.getLogger(ExternalChartLoadTask.class.getPackage().getName() + ".Worker-" + workerID);
		}
	}
	
	private class CountryAirportWork extends ChartWork {
		private final Country _c;
		private final GetAirCharts _acdao = new GetAirCharts();
		private final Collection<Airport> _results = new ArrayList<Airport>();
		
		CountryAirportWork(Country c) {
			super();
			_c = c;
		}
		
		public Collection<Airport> getResults() {
			return _results;
		}
		
		@Override
		public void run() {
			initLogger();
			try {
				_results.addAll(_acdao.getAirports(_c));
				tLog.info("Loaded " + _results.size() + " Airports for " + _c);
			} catch (DAOException de) {
				tLog.error("Cannot load Airports for " + _c, de);
			}
		}
	}
	
	private class ChartInfoWork extends ChartWork {
		private final ExternalChart _ec;
		
		ChartInfoWork(ExternalChart ec) {
			super();
			_ec = ec;
		}
		
		public ExternalChart getChart() {
			return _ec;
		}
		
		@Override
		public void run() {
			initLogger();
			
			GetExternalCharts ecdao = new GetExternalCharts();
			ecdao.setConnectTimeout(5000);
			ecdao.setReadTimeout(7500);
			
			String icao = _ec.getAirport().getICAO();			
			try {
				ecdao.populate(_ec);
				tLog.info("Populated " + icao + " " + _ec.getName());
			} catch (HTTPDAOException hde) {
				Integer rc = Integer.valueOf(hde.getStatusCode());
				if (NONFATAL_CODES.contains(rc)) {
					try {
						ecdao.load(_ec);
					} catch (DAOException de) {
						tLog.warn("Error loading " + icao + " " + _ec.getName() + " - " + de.getMessage());	
					}
				} else
					tLog.warn("Error populating " + icao + " " + _ec.getName() + " - " + hde.getMessage());
			} catch (DAOException de) {
				tLog.warn("Error populating " + icao + " " + _ec.getName() + " - " + de.getMessage());
			}
		}
	}
	
	private class AirportChartWork extends ChartWork {
		private final Airport _a;
		private final Collection<ExternalChart> _results = new ArrayList<ExternalChart>();
		
		AirportChartWork(Airport a) {
			super();
			_a = a;
		}
		
		public Collection<ExternalChart> getResults() {
			return _results;
		}
		
		@Override
		public void run() {
			initLogger();
			
			GetAirCharts ecdao = new GetAirCharts();
			ecdao.setConnectTimeout(5000);
			ecdao.setReadTimeout(7500);
			
			try {
				_results.addAll(ecdao.getCharts(_a));
				tLog.info("Loaded " + _results.size() + " charts for " + _a);
			} catch (DAOException de) {
				tLog.error("Error loading charts for " + _a + " - " + de.getMessage());
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
		if ((chartCountries == null) || chartCountries.isEmpty()) {
			log.warn("No Chart Sources defined");
			chartCountries = Collections.emptyMap();
		}
		
		int maxAge = SystemData.getInt("schedule.chart.maxAge", 31);
		try {
			String defaultSource = SystemData.get("schedule.chart.default");
			
			// Load AirCharts countries
			GetAirCharts acdao = new GetAirCharts();
			Collection<CountryAirportWork> work = new ArrayList<CountryAirportWork>();
			for (Country c : acdao.getCountries()) {
				String src = (String) chartCountries.get(c.getCode().toLowerCase());
				if (src == null)
					src = defaultSource;
				if ("AirCharts".equals(src))
					work.add(new CountryAirportWork(c));
			}
			
			// Run the thread pool
			int maxThreads = SystemData.getInt("schedule.chart.threads", 12);
			ThreadPoolExecutor exec = new ThreadPoolExecutor(maxThreads, maxThreads, 150, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			exec.allowCoreThreadTimeOut(true);
			for (CountryAirportWork cw : work)
				exec.execute(cw);
			
			// Await shutdown
			exec.shutdown();
			exec.awaitTermination(3, TimeUnit.MINUTES);
			
			// Reset thread pool
			exec = new ThreadPoolExecutor(maxThreads, maxThreads, 150, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			exec.allowCoreThreadTimeOut(true);
			
			// Go through each airport, and check the max age
			Connection con = ctx.getConnection();
			GetChart cdao = new GetChart(con);
			Collection<AirportChartWork> apWork = new ArrayList<AirportChartWork>();
			for (CountryAirportWork cw : work) {
				for (Airport a : cw.getResults()) {
					int maxChartAge = cdao.getMaxAge(a);
					if ((maxChartAge == -1) || (maxChartAge >= maxAge)) {
						AirportChartWork ap = new AirportChartWork(a);
						apWork.add(ap);
						exec.execute(ap);
					}
				}
			}

			// Release connection
			ctx.release();
			
			// Await shutdown
			exec.shutdown();
			exec.awaitTermination(5, TimeUnit.MINUTES);
			
			// Reset thread pool
			exec = new ThreadPoolExecutor(maxThreads, maxThreads, 150, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			exec.allowCoreThreadTimeOut(true);
			
			// Load the chart data for each airport
			Collection<ChartInfoWork> cWork = new ArrayList<ChartInfoWork>();
			for (AirportChartWork aw: apWork) {
				for (ExternalChart ec : aw.getResults()) {
					ChartInfoWork cw = new ChartInfoWork(ec);
					cWork.add(cw);
					exec.execute(cw);
				}
			}
			
			// Get the external chart IDs
			Map<String, ExternalChart> charts = new HashMap<String, ExternalChart>();
			for (ChartInfoWork cw : cWork) {
				ExternalChart ec = cw.getChart();
				if (!StringUtils.isEmpty(ec.getExternalID()))
					charts.put(ec.getExternalID(), ec);
			}
			
			// Await shutdown
			exec.shutdown();
			exec.awaitTermination(5, TimeUnit.MINUTES);
			
			// Get a connection and start a transaction
			con = ctx.getConnection();
			ctx.startTX();
			
			// Determine the external-internal ID mappings
			cdao = new GetChart(con);
			Map<String, Integer> idMap = cdao.getChartIDs(charts.keySet());
			for (Map.Entry<String, Integer> me : idMap.entrySet()) {
				ExternalChart exc = charts.get(me.getKey());
				if (exc != null) {
					log.info("Chart " + exc.getName() + " updating ID " + me.getValue());
					exc.setID(me.getValue().intValue());
				}
			}
			
			// Write the charts
			SetChart cwdao = new SetChart(con);
			for (ExternalChart exc : charts.values())
				cwdao.write(exc);
			
			ctx.commitTX();
		} catch (InterruptedException ie) {
			log.error("Thread pool timeout!");
		} catch (DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		log.info("Processing Complete");
	}
}
// Copyright 2012, 2013, 2015, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import org.deltava.beans.navdata.CycleInfo;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;
import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download FAA approach charts.
 * @author Luke
 * @version 7.0
 * @since 5.0
 */

public class FAAChartLoaderTask extends Task {

	private class ChartLoader implements Runnable {
		private final ExternalChart _ec;
		private final BlockingQueue<ExternalChart> _out;
		
		ChartLoader(BlockingQueue<ExternalChart> cq, ExternalChart ec) {
			super();
			_ec = ec;
			_out = cq;
		}
		
		@Override
		@SuppressWarnings("synthetic-access")
		public void run() {
			GetFAACharts dao = new GetFAACharts();
			try {
				dao.load(_ec);
				_out.add(_ec);
			} catch (Exception e) {
				log.error("Error loading " + _ec.getName(), e);
			}
		}
	}
	
	private class ChartSizer implements Runnable {
		private final ExternalChart _ec;
		private final BlockingQueue<ExternalChart> _out;
		
		ChartSizer(BlockingQueue<ExternalChart> cq, ExternalChart ec) {
			super();
			_ec = ec;
			_out = cq;
		}
		
		@Override
		@SuppressWarnings("synthetic-access")
		public void run() {
			GetFAACharts dao = new GetFAACharts();
			try {
				dao.loadSize(_ec);
				_out.add(_ec);
			} catch (Exception e) {
				log.error("Error loading " + _ec.getName(), e);
			}
		}
	}
	
	/**
	 * Initializes the Task.
	 */
	public FAAChartLoaderTask() {
		super("FAA Chart Loader", FAAChartLoaderTask.class);
	}
	
	/**
	 * Executes the Task.
	 */
	@Override
	protected void execute(TaskContext ctx) {
		
		// Fetch the chart URL with month in it
		ZonedDateTime zdt = ZonedDateTime.now();
		int month = zdt.get(ChronoField.MONTH_OF_YEAR); int year = zdt.get(ChronoField.YEAR);
		try {
			GetNavCycle ncdao = new GetNavCycle(ctx.getConnection());
			CycleInfo cycleInfo = ncdao.getCycle(zdt.toInstant());
			if (cycleInfo != null) {
				year = cycleInfo.getYear();
				month = cycleInfo.getSequence();
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Calculate URL and local file name
		String metaURL = SystemData.get("schedule.chart.url.faa.meta");
		metaURL = metaURL.replace("${YY}", StringUtils.format(year, "00")).replace("${MM}", StringUtils.format(month, "00"));
		String localName = "faaChartMetadata-${YY}${MM}.xml".replace("${MM}", StringUtils.format(month, "00")).replace("${YY}", StringUtils.format(year, "00"));
		boolean noDL = SystemData.getBoolean("schedule.chart.noDownload");

		Map<Airport, AirportCharts<ExternalChart>> newCharts = new TreeMap<Airport, AirportCharts<ExternalChart>>();
		Map<Airport, AirportCharts<Chart>> oldCharts = new HashMap<Airport, AirportCharts<Chart>>();
		try {
			File f = new File(SystemData.get("schedule.cache"), localName);
			GetURL dldao = new GetURL(metaURL, f.getAbsolutePath());
			File ff = dldao.download();
			
			GetFAACharts mddao = new GetFAACharts();
			Map<Airport, AirportCharts<ExternalChart>> nc = CollectionUtils.createMap(mddao.getChartList(ff.toURI().toString()), "airport"); 
			newCharts.putAll(nc);
			
			// Go through the airports and load the charts
			GetChart cdao = new GetChart(ctx.getConnection());
			for (Airport a : newCharts.keySet()) {
				Collection<Chart> charts = cdao.getCharts(a);
				if (!charts.isEmpty())
					oldCharts.put(a, new AirportCharts<Chart>(a, charts));
			}
		} catch (DAOException de) {
			log.error(de.getMessage(), de);
		} finally {
			ctx.release();
		}
		
		// Get base URL
		String baseURL = SystemData.get("schedule.chart.url.faa.chartBase").replace("${YY}", StringUtils.format(year, "00")).replace("${MM}", StringUtils.format(month, "00"));
		
		// Collections of charts to update
		Collection<Integer> chartsToDelete = new HashSet<Integer>();
		BlockingQueue<ExternalChart> chartsToLoad = new LinkedBlockingQueue<ExternalChart>();
		
		// If there are new FAA airports, queue them
		Collection<Airport> newAirports = CollectionUtils.getDelta(newCharts.keySet(), oldCharts.keySet());
		for (Airport a : newAirports) {
			log.info("Loading charts for new Airport " + a);
			Collection<ExternalChart> charts = newCharts.get(a).getCharts();
			chartsToLoad.addAll(charts);
			newCharts.remove(a);
			charts.forEach(ec -> ec.setURL(baseURL + "/" + ec.getExternalID()));
		}
			
		// Go through the remaining FAA airports, adding and removing as needed
		int addCnt = 0; int updCnt = 0; int delCnt = 0;
		for (AirportCharts<ExternalChart> ac : newCharts.values()) {
			log.info("Processing " + ac.getAirport());
			AirportCharts<Chart> oc = oldCharts.get(ac.getAirport());
			for (ExternalChart ec : ac.getCharts()) {
				ec.setURL(baseURL + "/" + ec.getExternalID());
				Chart oec = oc.get(ec.getName());
				if (oec != null) {
					boolean isSame = oec.getIsExternal() && ((ExternalChart)oec).getURL().equals(ec.getURL());
					if (!isSame) {
						ec.setID(oec.getID());
						log.info("Updating chart " + oec.getName());
						oc.getCharts().remove(oec);
						chartsToLoad.add(ec);
						updCnt++;
					}
				} else {
					log.info("Adding chart " + ec.getName());
					chartsToLoad.add(ec);
					addCnt++;
				}
			}
			
			// Find charts that are no longer in the new charts
			for (Chart oec : oc.getCharts()) {
				if (ac.get(oec.getName()) == null) {
					log.info("Deleting chart " + oec.getName());
					chartsToDelete.add(Integer.valueOf(oec.getID()));
					delCnt++;
				}
			}
		}
		
		log.info("Added " + addCnt + ", updated " + updCnt + ", deleted " + delCnt + " charts");
		
		try {
			Connection con = ctx.getConnection();
			SetChart cwdao = new SetChart(con); ctx.startTX();
			for (Integer id : chartsToDelete)
				cwdao.delete(id.intValue());
			
			SetMetadata mddao = new SetMetadata(con);
			mddao.write("charts.cycle.faa", StringUtils.format(year, "00") +  StringUtils.format(month, "00"));
			
			// Commit and release
			ctx.commitTX(); ctx.release();
			
			// Create the thread pool
			int maxSize = SystemData.getInt("schedule.chart.threads", 8);
			BlockingQueue<ExternalChart> work = new LinkedBlockingQueue<ExternalChart>();
			ThreadPoolExecutor exec = new ThreadPoolExecutor(maxSize, maxSize, 250, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			exec.allowCoreThreadTimeOut(true);

			// Queue the charts
			TaskTimer tt = new TaskTimer(); 
			for (ExternalChart ec : chartsToLoad) {
				Runnable wrk = noDL ? new ChartSizer(work, ec) : new ChartLoader(work, ec); 
				exec.execute(wrk);
			}
			
			// Start looping and writing charts
			exec.shutdown(); int totalTime = 0; boolean keepRunning = true;
			while (keepRunning && (totalTime < 600_000)) {
				Thread.sleep(1000); totalTime += 1000; int charts = 0;
				cwdao = new SetChart(ctx.getConnection()); ctx.startTX();
				ExternalChart ec = work.poll(50, TimeUnit.MILLISECONDS);
				while (ec != null) {
					if ((ec.getID() != 0) && ec.isLoaded())
						cwdao.save(ec);
					else
						cwdao.write(ec);
					
					ec.clear(); charts++;
					totalTime += 35;
					ec = work.poll(25, TimeUnit.MILLISECONDS);
				}
				
				keepRunning = !exec.isTerminated() || !work.isEmpty();
				ctx.commitTX(); ctx.release();
				log.info(charts + " charts saved to Database");
			}
			
			// Wait for timeout
			exec.awaitTermination(5, TimeUnit.SECONDS);
			long ms = tt.stop();
			log.info(chartsToLoad + " charts updated in " + StringUtils.format(ms/1000.0, "#0.00") + "s");
		} catch (InterruptedException | DAOException de) {
			ctx.rollbackTX();
			log.error(de.getMessage(), (de instanceof InterruptedException) ? null : de);
		} finally {
			ctx.release();
		}
		
		log.info("Processing Complete");
	}
}
// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.tasks;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;

import org.deltava.beans.schedule.*;

import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.taskman.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Scheduled Task to download FAA approach charts.
 * @author Luke
 * @version 5.0
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
	
	private class ChartWriter extends Thread {
		private final BlockingQueue<ExternalChart> _in;		
		private final SetChart _wdao;
		
		ChartWriter(BlockingQueue<ExternalChart> cq, SetChart wdao) {
			super("FAAChartWriter");
			setDaemon(true);
			_in = cq;
			_wdao = wdao;
		}
		
		@Override
		@SuppressWarnings("synthetic-access")
		public void run() {
			while (!isInterrupted()) {
				try {
					ExternalChart ec = _in.take();
					if ((ec != null) && (ec.getID() != 0) && ec.isLoaded()) {
						_wdao.save(ec);
						ec.clear();
					} else if (ec != null) {
						_wdao.write(ec);
						ec.clear();
					}
				} catch (DAOException de) {
					log.error(de.getMessage(), de);
				} catch (InterruptedException ie) {
					interrupt();
				}
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
		Calendar cld = Calendar.getInstance();
		int month = cld.get(Calendar.MONTH) + ((cld.get(Calendar.DAY_OF_MONTH) > 18) ? 2 : 1);
		int year = cld.get(Calendar.YEAR);
		if (month - cld.get(Calendar.MONTH) > 2) {
			month -= 12;
			year++;
		}
		
		// Calculate URL and local file name
		String baseURL = SystemData.get("schedule.chart.url.faa");
		baseURL = baseURL.replace("${YY}", StringUtils.format(year, "yy")).replace("${MM}", StringUtils.format(month, "00"));
		String localName = "faaChartMetadata-${YY}${MM}.xml".replace("${MM}", StringUtils.format(month, "00")).replace("${YY}", StringUtils.format(year, "00"));
		boolean noDL = SystemData.getBoolean("schedule.chart.noDownload");

		Map<Airport, AirportCharts<ExternalChart>> newCharts = new TreeMap<Airport, AirportCharts<ExternalChart>>();
		Map<Airport, AirportCharts<Chart>> oldCharts = new HashMap<Airport, AirportCharts<Chart>>();
		try {
			File f = new File(SystemData.get("schedule.cache"), localName);
			GetURL dldao = new GetURL(baseURL + "/xml_data/d-TPP_Metafile.xml", f.getAbsolutePath());
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
		
		// Collections of charts to update
		Collection<Integer> chartsToDelete = new HashSet<Integer>();
		BlockingQueue<ExternalChart> chartsToLoad = new LinkedBlockingQueue<ExternalChart>();
		
		// If there are new FAA airports, queue them
		Collection<Airport> newAirports = CollectionUtils.getDelta(newCharts.keySet(), oldCharts.keySet());
		for (Airport a : newAirports) {
			log.info("Loading charts for new Airport " + a);
			AirportCharts<ExternalChart> charts = newCharts.get(a);
			chartsToLoad.addAll(charts.getCharts());
			newCharts.remove(a);
		}
			
		// Go through the remaining FAA airports, adding and removing as needed
		int addCnt = 0; int updCnt = 0; int delCnt = 0;
		for (AirportCharts<ExternalChart> ac : newCharts.values()) {
			log.info("Processing " + ac.getAirport());
			AirportCharts<Chart> oc = oldCharts.get(ac.getAirport());
			for (ExternalChart ec : ac.getCharts()) {
				Chart oec = oc.get(ec.getName());
				if (oec != null) {
					boolean isSame = (oec.getIsExternal() && (((ExternalChart)oec).getExternalID().equals(ec.getExternalID())));
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
		
		ChartWriter cw = null;
		try {
			Connection con = ctx.getConnection();
			ctx.startTX();
			SetChart cwdao = new SetChart(con);
			for (Integer id : chartsToDelete)
				cwdao.delete(id.intValue());
			
			// Start the writer thread
			BlockingQueue<ExternalChart> work = new LinkedBlockingQueue<ExternalChart>();
			cw = new ChartWriter(work, cwdao);
			cw.start();
			
			// Create the thread pool
			int maxSize = SystemData.getInt("schedule.chart.threads", 8);
			ThreadPoolExecutor exec = new ThreadPoolExecutor(maxSize, maxSize, 250, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			exec.allowCoreThreadTimeOut(true);

			// Queue the charts
			TaskTimer tt = new TaskTimer(); 
			for (ExternalChart ec : chartsToLoad) {
				ec.setURL(baseURL + "/" + ec.getExternalID());
				Runnable wrk = noDL ? new ChartSizer(work, ec) : new ChartLoader(work, ec); 
				exec.execute(wrk);
			}
			
			// Wait for timeout
			exec.shutdown();
			exec.awaitTermination(10, TimeUnit.MINUTES);
			ctx.commitTX();
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
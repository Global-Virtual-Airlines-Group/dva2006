// Copyright 2012, 2013, 2015, 2016, 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

import org.apache.log4j.Logger;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.*;
import org.deltava.beans.navdata.CycleInfo;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to manually download FAA approach charts.
 * @author Luke
 * @version 8.0
 * @since 5.0
 */

public class FAAChartDownloadCommand extends AbstractCommand {
	
	private static final String[] MONTH_NAMES = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December", "Extra Cycle" };
	private static final List<ComboAlias> MONTHS = ComboUtils.fromArray(MONTH_NAMES, new String[] {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13"});
	
	private class ChartLoader implements Runnable {
		protected final Logger log = Logger.getLogger(ChartLoader.class);
		private final ExternalChart _ec;
		private final BlockingQueue<ExternalChart> _out;
		
		ChartLoader(BlockingQueue<ExternalChart> cq, ExternalChart ec) {
			super();
			_ec = ec;
			_out = cq;
		}
		
		@Override
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
		protected final Logger log = Logger.getLogger(ChartSizer.class);
		private final ExternalChart _ec;
		private final BlockingQueue<ExternalChart> _out;
		
		ChartSizer(BlockingQueue<ExternalChart> cq, ExternalChart ec) {
			super();
			_ec = ec;
			_out = cq;
		}
		
		@Override
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
	
	private class LogMessage {
		private final String _class;
		private final String _msg;
		
		LogMessage(String msg) {
			this(null, msg);
		}
		
		LogMessage(String c, String msg) {
			super();
			_class = c;
			_msg = msg;
		}
		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder(128);
			if (_class != null) {
				buf.append("<span class=\"");
				buf.append(_class).append("\">");
			}
			
			buf.append(_msg);
			if (_class != null)
				buf.append("</span>");
			
			return buf.toString();
		}
	}
	
    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an unhandled error occurs
     */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Calculate cycle
		CycleInfo cycleInfo = null;
		ZonedDateTime zdt = ZonedDateTime.now();
		try {
			Connection con = ctx.getConnection();
			
			// Get cycle to download
			GetNavCycle ncdao = new GetNavCycle(con);
			cycleInfo = ncdao.getCycle(zdt.toInstant());
			if (cycleInfo == null)
				cycleInfo = CycleInfo.getCurrent();
			
			// Get current cycle
        	GetMetadata mddao = new GetMetadata(con);
        	String chartCycleID = mddao.get("charts.cycle.faa");
        	if (chartCycleID != null)
        		ctx.setAttribute("currentCycle", ncdao.getCycle(chartCycleID), REQUEST);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Set request attributes
		Collection<Integer> yrs = new TreeSet<Integer>();
		yrs.add(Integer.valueOf(zdt.get(ChronoField.YEAR)));
		yrs.add(Integer.valueOf(cycleInfo.getYear()));
		ctx.setAttribute("months", MONTHS, REQUEST);
		ctx.setAttribute("years", yrs, REQUEST);

		// Get comamnd result
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("month") == null) {
			ctx.setAttribute("m", StringUtils.format(cycleInfo.getSequence(), "00"), REQUEST);
			ctx.setAttribute("y", Integer.valueOf(cycleInfo.getYear()), REQUEST);
			
			// Send to the JSP
			result.setURL("/jsp/schedule/faaChartDownload.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the cycle to download and max count
		int y = StringUtils.parse(ctx.getParameter("year"), 0) - 2000;
		String metaURL = SystemData.get("schedule.chart.url.faa.meta");
		metaURL = metaURL.replace("${YY}", StringUtils.format(y, "00")).replace("${MM}", ctx.getParameter("month"));
		boolean noDL = Boolean.valueOf(ctx.getParameter("noDownload")).booleanValue();
		
		// Calculate local file name
		String localName = "faaChartMetadata-${YY}${MM}.xml".replace("${MM}", ctx.getParameter("month")).replace("${YY}", StringUtils.format(y, "00"));

		Map<Airport, AirportCharts<ExternalChart>> newCharts = new TreeMap<Airport, AirportCharts<ExternalChart>>();
		Map<Airport, AirportCharts<Chart>> oldCharts = new HashMap<Airport, AirportCharts<Chart>>();
		try {
			File f = new File(SystemData.get("schedule.cache"), localName);
			GetURL dldao = new GetURL(metaURL, f.getAbsolutePath());
			if (!dldao.isAvailable())
				throw new CommandException(metaURL + " not yet available", false);
				
			File ff = dldao.download();
			GetFAACharts mddao = new GetFAACharts();
			Map<Airport, AirportCharts<ExternalChart>> nc = CollectionUtils.createMap(mddao.getChartList(ff.toURI().toString()), AirportCharts::getAirport); 
			newCharts.putAll(nc);
			
			// Go through the airports and load the charts
			GetChart cdao = new GetChart(ctx.getConnection());
			for (Airport a : newCharts.keySet()) {
				Collection<Chart> charts = cdao.getCharts(a);
				if (!charts.isEmpty())
					oldCharts.put(a, new AirportCharts<Chart>(a, charts));
			}
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Calculate base URL
		final String baseURL = SystemData.get("schedule.chart.url.faa.chartBase").replace("${YY}", StringUtils.format(y, "00")).replace("${MM}", ctx.getParameter("month"));
		
		// Collections of charts to update
		Collection<Integer> chartsToDelete = new LinkedHashSet<Integer>();
		Collection<ExternalChart> chartsToLoad = new ArrayList<ExternalChart>();
		Collection<LogMessage> msgs = new ArrayList<LogMessage>();
		
		// If there are new FAA airports, queue them
		Collection<Airport> newAirports = CollectionUtils.getDelta(newCharts.keySet(), oldCharts.keySet());
		for (Airport a : newAirports) {
			msgs.add(new LogMessage("pri bld", "Processing new Airport " + a));
			Collection<ExternalChart> charts = newCharts.get(a).getCharts();
			chartsToLoad.addAll(charts);
			newCharts.remove(a);
			for (ExternalChart ec : charts) {
				ec.setURL(baseURL + "/" + ec.getExternalID()); 
				msgs.add(new LogMessage("Adding chart " + ec.getName())); 
			}
		}
		
		// Go through the remaining FAA airports, adding and removing as needed
		int addCnt = 0; int updCnt = 0; int delCnt = 0;
		for (AirportCharts<ExternalChart> ac : newCharts.values()) {
			msgs.add(new LogMessage("pri bld", "Processing " + ac.getAirport()));
			AirportCharts<Chart> occ = oldCharts.get(ac.getAirport());
			for (ExternalChart ec : ac.getCharts()) {
				ec.setURL(baseURL + "/" + ec.getExternalID());
				Chart oc = occ.get(ec.getName());
				if (oc != null) {
					boolean isSame = oc.getIsExternal() && ((ExternalChart)oc).getURL().equals(ec.getURL());
					if (!isSame) {
						ec.setID(oc.getID());
						msgs.add(new LogMessage("Updating chart " + oc.getName()));
						occ.getCharts().remove(oc);
						chartsToLoad.add(ec);
						updCnt++;
					}
				} else {
					msgs.add(new LogMessage("Adding chart " + ec.getName()));
					chartsToLoad.add(ec);
					addCnt++;
				}
			}
			
			// Find charts that are no longer in the new charts
			for (Chart oec : occ.getCharts()) {
				if (ac.get(oec.getName()) == null) {
					msgs.add(new LogMessage("sec", "Deleting chart " + oec.getName()));
					chartsToDelete.add(Integer.valueOf(oec.getID()));
					delCnt++;
				}
			}
		}
		
		try {
			Connection con = ctx.getConnection();
			SetChart cwdao = new SetChart(con);
			ctx.startTX();
			for (Integer id : chartsToDelete)
				cwdao.delete(id.intValue());
			
			ctx.commitTX();
			ctx.release();
			
			// Write the cycle
			SetMetadata mddao = new SetMetadata(con);
			mddao.write("charts.cycle.faa", StringUtils.format(y, "00") + ctx.getParameter("month")); 
			
			// Create the thread pool
			BlockingQueue<ExternalChart> work = new LinkedBlockingQueue<ExternalChart>();
			int poolSize = SystemData.getInt("schedule.chart.threads", 8);
			ThreadPoolExecutor exec = new ThreadPoolExecutor(poolSize, poolSize, 200, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			exec.allowCoreThreadTimeOut(true);
			
			// Queue the charts
			int queueSize = 0; TaskTimer tt = new TaskTimer(); 
			for (ExternalChart ec : chartsToLoad) {
				Runnable wrk = noDL ? new ChartSizer(work, ec) : new ChartLoader(work, ec); 
				exec.execute(wrk);
				queueSize++;
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
				msgs.add(new LogMessage(charts + " charts saved to Database"));
			}
			
			exec.awaitTermination(5, TimeUnit.SECONDS);
			long ms = tt.stop();
			msgs.add(new LogMessage("sec bld", queueSize + " charts updated in " + StringUtils.format(ms/1000.0, "#0.00") + "s"));
		} catch (InterruptedException | DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Save status variables
		ctx.setAttribute("doImport", Boolean.TRUE, REQUEST);
		ctx.setAttribute("msgs", msgs, REQUEST);
		ctx.setAttribute("chartsAdded", Integer.valueOf(addCnt), REQUEST);
		ctx.setAttribute("chartsUpdated", Integer.valueOf(updCnt), REQUEST);
		ctx.setAttribute("chartsDeleted", Integer.valueOf(delCnt), REQUEST);
		ctx.setAttribute("m", ctx.getParameter("month"), REQUEST);
		ctx.setAttribute("y", ctx.getParameter("year"), REQUEST);
		
		// Forward to the page
		result.setURL("/jsp/schedule/faaChartDownload.jsp");
		result.setSuccess(true);
	}
}
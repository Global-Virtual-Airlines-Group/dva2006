// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.schedule;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.sql.Connection;

import org.apache.log4j.Logger;

import org.deltava.beans.ComboAlias;
import org.deltava.beans.schedule.*;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.dao.http.*;

import org.deltava.util.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to manually download FAA approach charts.
 * @author Luke
 * @version 5.0
 * @since 5.0
 */

public class FAAChartDownloadCommand extends AbstractCommand {
	
	private static final String[] MONTH_NAMES = {"January", "February", "March", "April", "May", "June", "July", "August",
		"September", "October", "November", "December" };
	private static final List<ComboAlias> MONTHS = ComboUtils.fromArray(MONTH_NAMES, new String[] {"01", "02", "03", "04",
		"05", "06", "07", "08", "09", "10", "11", "12"});
	
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
	
	private class ChartWriter extends Thread {
		protected final Logger log = Logger.getLogger(ChartWriter.class);
		private final BlockingQueue<ExternalChart> _in;		
		private final SetChart _wdao;
		
		ChartWriter(BlockingQueue<ExternalChart> cq, SetChart wdao) {
			super("FAAChartWriter");
			setDaemon(true);
			_in = cq;
			_wdao = wdao;
		}
		
		@Override
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
		Calendar cld = Calendar.getInstance();
		int month = cld.get(Calendar.MONTH) + ((cld.get(Calendar.DAY_OF_MONTH) > 18) ? 2 : 1);
		int year = cld.get(Calendar.YEAR);
		if (month - cld.get(Calendar.MONTH) > 2) {
			month -= 12;
			year++;
		}
		
		// Set request attributes
		Collection<Integer> yrs = new TreeSet<Integer>();
		yrs.add(Integer.valueOf(cld.get(Calendar.YEAR)));
		yrs.add(Integer.valueOf(year));
		ctx.setAttribute("months", MONTHS, REQUEST);
		ctx.setAttribute("years", yrs, REQUEST);

		// Get comamnd result
		CommandResult result = ctx.getResult();
		if (ctx.getParameter("month") == null) {
			ctx.setAttribute("m", StringUtils.format(month, "00"), REQUEST);
			ctx.setAttribute("y", Integer.valueOf(year), REQUEST);
			
			// Send to the JSP
			result.setURL("/jsp/schedule/faaChartDownload.jsp");
			result.setSuccess(true);
			return;
		}
		
		// Get the cycle to download and max count
		int y = StringUtils.parse(ctx.getParameter("year"), 0) - 2000;
		String baseURL = SystemData.get("schedule.chart.url.faa");
		baseURL = baseURL.replace("${YY}", StringUtils.format(y, "00")).replace("${MM}", ctx.getParameter("month"));
		boolean noDL = Boolean.valueOf(ctx.getParameter("noDownload")).booleanValue();
		int maxCharts = StringUtils.parse(ctx.getParameter("maxCharts"), Integer.MAX_VALUE);
		
		// Calculate local file name
		String localName = "faaChartMetadata-${YY}${MM}.xml".replace("${MM}", ctx.getParameter("month")).replace("${YY}", StringUtils.format(y, "00"));

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
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Collections of charts to update
		Collection<Integer> chartsToDelete = new HashSet<Integer>();
		Collection<ExternalChart> chartsToLoad = new ArrayList<ExternalChart>();
		Collection<LogMessage> msgs = new ArrayList<LogMessage>();
		
		// If there are new FAA airports, queue them
		Collection<Airport> newAirports = CollectionUtils.getDelta(newCharts.keySet(), oldCharts.keySet());
		for (Airport a : newAirports) {
			msgs.add(new LogMessage("pri bld", "Loading charts for new Airport " + a));
			AirportCharts<ExternalChart> charts = newCharts.get(a);
			chartsToLoad.addAll(charts.getCharts());
			newCharts.remove(a);
		}
		
		// Go through the remaining FAA airports, adding and removing as needed
		int addCnt = 0; int updCnt = 0; int delCnt = 0;
		for (AirportCharts<ExternalChart> ac : newCharts.values()) {
			msgs.add(new LogMessage("pri bld", "Processing " + ac.getAirport()));
			AirportCharts<Chart> oc = oldCharts.get(ac.getAirport());
			for (ExternalChart ec : ac.getCharts()) {
				Chart oec = oc.get(ec.getName());
				if (oec != null) {
					boolean isSame = (oec.getIsExternal() && (((ExternalChart)oec).getExternalID().equals(ec.getExternalID())));
					if (!isSame) {
						ec.setID(oec.getID());
						msgs.add(new LogMessage("Updating chart " + oec.getName()));
						oc.getCharts().remove(oec);
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
			for (Chart oec : oc.getCharts()) {
				if (ac.get(oec.getName()) == null) {
					msgs.add(new LogMessage("sec", "Deleting chart " + oec.getName()));
					chartsToDelete.add(Integer.valueOf(oec.getID()));
					delCnt++;
				}
			}
		}
		
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
			int maxSize = Math.min(maxCharts, SystemData.getInt("schedule.chart.threads", 8));
			ThreadPoolExecutor exec = new ThreadPoolExecutor(maxSize, maxSize, 250, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			exec.allowCoreThreadTimeOut(true);
			
			// Queue the charts
			int queueSize = 0; TaskTimer tt = new TaskTimer(); 
			for (Iterator<ExternalChart> eci = chartsToLoad.iterator(); eci.hasNext() && (queueSize < maxCharts); ) {
				ExternalChart ec = eci.next();
				ec.setURL(baseURL + "/" + ec.getExternalID());
				Runnable wrk = noDL ? new ChartSizer(work, ec) : new ChartLoader(work, ec); 
				exec.execute(wrk);
				queueSize++;
			}
				
			exec.shutdown();
			exec.awaitTermination(10, TimeUnit.MINUTES);
			ctx.commitTX();
			long ms = tt.stop();
			msgs.add(new LogMessage("sec bld", queueSize + " charts updated in " + StringUtils.format(ms/1000.0, "#0.00") + "s"));
		} catch (InterruptedException | DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			if (cw != null) cw.interrupt();
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
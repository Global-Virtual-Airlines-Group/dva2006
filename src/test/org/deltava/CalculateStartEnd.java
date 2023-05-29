package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.apache.logging.log4j.*;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.acars.RouteEntry;
import org.deltava.beans.schedule.GeoPosition;
import org.deltava.dao.*;
import org.deltava.dao.file.GetSerializedPosition;
import org.deltava.dao.http.GetURL;
import org.deltava.util.TaskTimer;
import org.deltava.util.system.SystemData;

public class CalculateStartEnd extends TestCase {
	
	private static final String URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false";
	private static final String USER = "luke";
	private static final String PWD = "tesst";
	
	private static final String HTTP_USER = "dva043";
	private static final String HTTP_PWD= "test";
	
	private Logger log;
	
	private class StartEndLocations {
		private final int _id;
		private final GeoLocation _start;
		private final GeoLocation _end;
		
		StartEndLocations(int id, GeoLocation start, GeoLocation end) {
			_id = id;
			_start = start;
			_end = end;
		}
	}
	
	private class GetTrackDAO extends GetURL {

		GetTrackDAO(int id) {
			super("https://dev.deltava.org/acars_raw_track.ws?id=" + id, null);
		}
		
		@Override
		protected void init(String url) throws IOException {
			super.init(url);
			setAuthentication(HTTP_USER, HTTP_PWD);
		}
	}
	
	private class ReadWorker implements Runnable {
		private final int _id;
		private final BlockingQueue<StartEndLocations> _out;
		
		ReadWorker(int id, BlockingQueue<StartEndLocations> out) {
			super();
			_id = id;
			_out = out;
		}
		
		@Override
		public void run() {
			boolean isGZIP = false;
			try {
				List<RouteEntry> rte = new ArrayList<RouteEntry>();
				GetTrackDAO dao = new GetTrackDAO(_id);
				byte[] data = dao.load(); int fw = ((data[1] << 8) & 0xFF00) + data[0];
				isGZIP = (fw == GZIPInputStream.GZIP_MAGIC);
				if  (isGZIP) {
					try (InputStream gz = new GZIPInputStream(new ByteArrayInputStream(data))) {
						GetSerializedPosition posdao = new GetSerializedPosition(gz);
						Collection<? extends RouteEntry> entries = posdao.read();
						rte.addAll(entries);
					}
				} else {
					try (InputStream is = new ByteArrayInputStream(data)) {
						GetSerializedPosition posdao = new GetSerializedPosition(is);
						Collection<? extends RouteEntry> entries = posdao.read();
						rte.addAll(entries);
					}
				}
				
				if (rte.size() < 2) return;
				StartEndLocations se = new StartEndLocations(_id, new GeoPosition(rte.get(0)), new GeoPosition(rte.get(rte.size() - 1)));
				_out.add(se);
			} catch (Exception ide) {
				log.error(String.format("%d - %s", Integer.valueOf(_id), ide.getMessage()));
			}
		}
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(CalculateStartEnd.class);
		
		SystemData.init();
		
		// Load the airports/time zones
		Class.forName("com.mysql.cj.jdbc.Driver");
		try (Connection c = DriverManager.getConnection(URL, USER, PWD)) {
			GetTimeZone tzdao = new GetTimeZone(c);
			tzdao.initAll();
			GetAirport apdao = new GetAirport(c);
			SystemData.add("airports", apdao.getAll());
			GetAirline aldao = new GetAirline(c);
			SystemData.add("airlines", aldao.getAll());
		}
	}

	public void testCalculateLocations() throws Exception {
		
		List<Integer> IDs = new ArrayList<Integer>();
		try (Connection c = DriverManager.getConnection(URL, USER, PWD)) {
			try (Statement s = c.createStatement()) {
				s.setFetchSize(5000);
				s.setMaxRows(75000);
				try (ResultSet rs = s.executeQuery("SELECT AP.ACARS_ID FROM acars.ARCHIVE A, ACARS_PIREPS AP LEFT JOIN START_END SE ON (AP.ACARS_ID=SE.ID) WHERE (AP.ACARS_ID=A.ID) AND (A.CNT > 10) AND (SE.START_LAT IS NULL)")) {
					while (rs.next())
						IDs.add(Integer.valueOf(rs.getInt(1)));	
				}
			}
		}
		
		IDs.removeIf(id -> (id.intValue() < 950888));
		log.info("Loaded " + IDs.size() + " flight IDs");
		BlockingQueue<StartEndLocations> outQueue = new LinkedBlockingQueue<StartEndLocations>();
		
		int poolSize = Runtime.getRuntime().availableProcessors(); TaskTimer tt = new TaskTimer();
		poolSize=5;
		try (ThreadPoolExecutor exec = new ThreadPoolExecutor(poolSize, poolSize, 200, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())) {
			exec.allowCoreThreadTimeOut(true);
			for (Integer id : IDs) {
				Runnable rw = new ReadWorker(id.intValue(), outQueue);
				exec.execute(rw);
			}
			
			exec.shutdown();
			exec.awaitTermination(10, TimeUnit.MINUTES);
		}
		
		log.info("Calculated " + outQueue.size() + " start/end positions in " + tt.stop() + "ms");
		
		try (Connection c = DriverManager.getConnection(URL, USER, PWD)) {
			c.setAutoCommit(false);
			try (PreparedStatement ps = c.prepareStatement("INSERT INTO START_END (ID, START_LAT, START_LNG, END_LAT, END_LNG) VALUES (?, ?, ?, ?, ?) AS N ON DUPLICATE KEY UPDATE START_LAT=N.START_LAT, START_LNG=N.START_LNG, END_LAT=N.END_LAT, END_LNG=N.END_LNG")) {
				int cnt = 0;
				for (StartEndLocations se : outQueue) {
					cnt++;
					ps.setInt(1, se._id);
					ps.setDouble(2, se._start.getLatitude());
					ps.setDouble(3, se._start.getLongitude());
					ps.setDouble(4, se._end.getLatitude());
					ps.setDouble(5, se._end.getLongitude());
					ps.addBatch();
					if ((cnt % 1000) == 0) {
						ps.executeBatch();
						log.info("Wrote " + cnt + " entries");
					}
				}
				
				ps.executeBatch();
				log.info("Wrote " + cnt + " entries");
			}
			
			c.commit();
		}
	}
}
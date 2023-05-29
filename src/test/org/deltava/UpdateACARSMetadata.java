package org.deltava;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import junit.framework.TestCase;

import org.apache.logging.log4j.*;

import org.deltava.dao.*;
import org.deltava.dao.http.GetURL;

import org.deltava.util.system.SystemData;

public class UpdateACARSMetadata extends TestCase {
	
	private static final String URL = "jdbc:mysql://sirius.sce.net/afv?useSSL=false";
	private static final String USER = "luke";
	private static final String PWD = "test";
	
	private Logger log;
	
	private class GetTrackDAO extends GetURL {

		GetTrackDAO(int id) {
			super("https://dev.deltava.org/acars_metadata_upd.ws?id=" + id, null);
		}
	}
	
	private class ReadWorker implements Runnable {
		private final int _id;
		
		ReadWorker(int id) {
			super();
			_id = id;
		}
		
		@Override
		public void run() {
			try {
				GetTrackDAO dao = new GetTrackDAO(_id);
				dao.load();
				if (dao.getStatusCode() == 200)
					log.info("Updated data for Flight " + _id);
				
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
		log = LogManager.getLogger(UpdateACARSMetadata.class);
		
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

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testCalculateLocations() throws Exception {
		
		List<Integer> IDs = new ArrayList<Integer>();
		try (Connection c = DriverManager.getConnection(URL, USER, PWD)) {
			try (Statement s = c.createStatement()) {
				s.setFetchSize(5000);
				s.setMaxRows(75000);
				try (ResultSet rs = s.executeQuery("SELECT AP.ACARS_ID FROM acars.ARCHIVE A, ACARS_PIREPS AP WHERE (AP.ACARS_ID=A.ID) AND (A.CNT > 10) AND (A.FMT=-1) ORDER BY A.ID")) {
					while (rs.next())
						IDs.add(Integer.valueOf(rs.getInt(1)));	
				}
			}
		}
		
		log.info("Loaded " + IDs.size() + " flight IDs");
		
		int poolSize = Runtime.getRuntime().availableProcessors();
		try (ThreadPoolExecutor exec = new ThreadPoolExecutor(poolSize, poolSize, 200, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>())) {
			exec.allowCoreThreadTimeOut(true);
			for (Integer id : IDs) {
				Runnable rw = new ReadWorker(id.intValue());
				exec.execute(rw);
			}
			
			exec.shutdown();
			exec.awaitTermination(10, TimeUnit.MINUTES);
		}
	}
}
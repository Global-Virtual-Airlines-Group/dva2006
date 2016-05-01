package org.deltava.acars;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.dao.*;
import org.deltava.util.ThreadUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.tile.TileAddress;

abstract class PlotMap extends TestCase {
	
	protected Logger log;
	protected static final String URL = "jdbc:mysql://sirius.sce.net/acars?user=luke&password=test";
	
	private static final int READ_THREADS = 12;
	private static final int WRITE_THREADS = 10;
	
	protected final Map<Integer, ProjectInfo> _zooms = new HashMap<Integer, ProjectInfo>();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(PlotMap.class);
		
		SystemData.init();
		TileData.init("D:\\temp\\swap");
		
		// Connect to the database
		Class<?> drv = Class.forName("com.mysql.jdbc.Driver");
		assertNotNull(drv);
		Connection c = DriverManager.getConnection(URL);
		assertNotNull(c);
		
		// Load the airports/time zones
		GetTimeZone tzdao = new GetTimeZone(c);
		tzdao.initAll();
		GetAirport apdao = new GetAirport(c);
		SystemData.add("airports", apdao.getAll());
		GetAirline aldao = new GetAirline(c);
		SystemData.add("airlines", aldao.getAll());
		log.info("Loaded System Data");
	}
	
	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}
	
	protected static BlockingQueue<Integer> getIDs(int max) throws Exception {
		try (Connection c = DriverManager.getConnection(URL)) {
			GetFlightIDs iddao = new GetFlightIDs(c);
			if (max > 0)
				iddao.setQueryMax(max);
			
			return iddao.getFlightIDs();
		}
	}
	
	protected static BlockingQueue<Integer> getIDs(int max, Collection<String> aps) throws Exception {
		try (Connection c = DriverManager.getConnection(URL)) {
			GetFlightIDs iddao = new GetFlightIDs(c);
			if (max > 0)
				iddao.setQueryMax(max);
			
			return iddao.getFlightIDs(aps);
		}
	}
	
	protected static void truncateTracks() throws SQLException {
		try (Connection c = DriverManager.getConnection(URL)) {
			try (Statement s = c.createStatement()) {
				s.executeUpdate("TRUNCATE acars.TRACKS");
			}
		}
	}
	
	protected static void truncateTracks(int minZoom) throws SQLException {
		try (Connection c = DriverManager.getConnection(URL)) {
			try (PreparedStatement ps = c.prepareStatement("DELETE FROM acars.TRACKS WHERE (Z>=?)")) {
				ps.setInt(1,  minZoom);
				ps.executeUpdate();
			}
		}
	}

	/**
	 * Load Flight data from the database
	 */
	protected void loadFlights(BlockingQueue<Integer> IDs, SparseGlobalTile gt, RouteEntryFilter f) throws Exception {
		log.info("Loading Flight Data for " + IDs.size() + " flights");
		Collection<Thread> wrks = new ArrayList<Thread>(24);
		for (int x = 0; x < READ_THREADS; x++) {
			Connection c2 = DriverManager.getConnection(URL);
			ReadWorker rw = new ReadWorker(x+1, f, c2, gt, IDs);
			Thread t = new Thread(rw, rw.toString());
			t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
			t.setDaemon(true);
			wrks.add(t);
			t.start();
		}
		
		ThreadUtils.waitOnPool(wrks);
	}
	
	protected SparseGlobalTile scale(SparseGlobalTile gt) throws Exception {
		log.info("Zoom Level " + gt.getZoom() + " size = " + gt.size() + " " + gt.getCacheInfo());
		Collection<TileAddress> addrs = gt.getTiles();
		Collection<TileAddress> parents = new LinkedHashSet<TileAddress>();
		for (TileAddress addr : addrs)
			parents.add(addr.getParent());
		
		BlockingQueue<TileAddress> work = new LinkedBlockingQueue<TileAddress>(parents);
		log.info("Calculated parent tile addresses");
		ProjectInfo ppp = _zooms.get(Integer.valueOf(gt.getZoom()));
		
		// Write and scale the tiles
		Collection<Thread> wrks = new ArrayList<Thread>(24);
		SparseGlobalTile pgt = new SparseGlobalTile(gt.getZoom() - 1 , gt.getMaxSize());
		int tc = Math.max(1, Math.min(WRITE_THREADS, (gt.size() / 6)));
		for (int x = 0; x < tc; x++) {
			Connection c2 = DriverManager.getConnection(URL);
			WriteWorker ww = new WriteWorker(x+1, ppp, c2, work, gt, pgt);
			Thread t = new Thread(ww, ww.toString());
			t.setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority() - 1));
			t.setDaemon(true);
			wrks.add(t);
			t.start();
		}
		
		ThreadUtils.waitOnPool(wrks);
		log.info("Completed writing zoom level " + gt.getZoom());
		return pgt;
	}
}
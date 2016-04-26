package org.deltava.acars;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import org.deltava.beans.schedule.Airport;
import org.deltava.util.system.SystemData;

public class PlotLocalMap extends PlotMap {
	
	private static final int MIN_ZOOM = 10;
	private static final int MAX_ZOOM = 13;
	private static final int MAX_TILES = 325 * 1024;
	
	private static final List<String> _airports = Arrays.asList("ATL", "YYZ", "MIA", "NRT",  "DFW", "IAD", "HKX", "HKG", "HNL", "OGG", "YUL", "LHR", "ICN", "BDA", "YVR", "SYD", "ZRH", "PPT", "ANC", "GVA"); 
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_zooms.put(Integer.valueOf(10), new ProjectInfo(10, 64, 10));
		_zooms.put(Integer.valueOf(11), new ProjectInfo(11, 40, 8));
		_zooms.put(Integer.valueOf(12), new ProjectInfo(12, 28, 6));
		_zooms.put(Integer.valueOf(13), new ProjectInfo(13, 20, 4));
	}
	
	protected static Collection<String> getPopularAirports(int max) throws Exception {
		try (Connection c = DriverManager.getConnection(URL)) {
			c.setAutoCommit(false);
			try (Statement s = c.createStatement()) {
				s.execute("create temporary table AP_COUNT (IATA CHAR(3) NOT NULL, CNT INTEGER UNSIGNED NOT NULL DEFAULT 0, PRIMARY KEY (IATA))");
				s.execute("insert into AP_COUNT (SELECT AIRPORT_D, COUNT(ID) AS ACNT FROM FLIGHTS GROUP BY AIRPORT_D ORDER BY ACNT)");
				s.execute("insert into AP_COUNT (SELECT AIRPORT_A, COUNT(ID) AS ACNT FROM FLIGHTS GROUP BY AIRPORT_A ORDER BY ACNT) on duplicate key update CNT=CNT+VALUES(CNT)");
			}
			
			c.commit();
			Collection<String> aps = new LinkedHashSet<String>();
			try (Statement s = c.createStatement()) {
				StringBuilder sqlBuf = new StringBuilder("SELECT IATA FROM AP_COUNT ORDER BY CNT DESC");
				if (max > 0)
					sqlBuf.append(" LIMIT ").append(max);
				
				try (ResultSet rs = s.executeQuery(sqlBuf.toString())) {
					while (rs.next())
						aps.add(rs.getString(1));
				}
			}
			
			// Write the track airport count
			try (Statement s = c.createStatement()) {
				s.execute("TRUNCATE TRACK_AIRPORTS");
			}
			
			Collection<String> allAP = new LinkedHashSet<String>(aps); allAP.addAll(_airports);
			StringBuilder buf = new StringBuilder("INSERT INTO TRACK_AIRPORTS (SELECT IATA, CNT FROM AP_COUNT WHERE IATA IN (");
			for (Iterator<?> i = allAP.iterator(); i.hasNext(); ) {
				i.next(); buf.append('?');
				if (i.hasNext())
					buf.append(',');
			}
			
			buf.append("))"); 
			try (PreparedStatement ps = c.prepareStatement(buf.toString())) {
				int ofs = 0;
				for (String apCode : allAP)
					ps.setString(++ofs, apCode);
				
				ps.executeUpdate();
			}
			
			c.commit();
			return aps;
		}
	}
	
	public void testPlotMap() throws Exception {
		
		// Init the filter
		Collection<String> apCodes = getPopularAirports(70);
		apCodes.addAll(_airports);

		Collection<Airport> airports = new ArrayList<Airport>();
		for (String iata : apCodes)
			airports.add(SystemData.getAirport(iata));
		
		AirportEntryFilter f = new AirportEntryFilter(MAX_ZOOM, 110, airports);
		f.setMaxAltitude(18500);
		
		// Load flight IDs
		log.info("Loading Flight IDs");
		BlockingQueue<Integer> IDs = getIDs(0, apCodes);
		
		// Load flights - separate thread per flight
		SparseGlobalTile gt = new SparseGlobalTile(MAX_ZOOM, MAX_TILES);
		loadFlights(IDs, gt, f);
		
		// Truncate tracks
		truncateTracks(MIN_ZOOM);
		
		while (gt.getZoom() >= MIN_ZOOM) {
			SparseGlobalTile pgt = scale(gt);
			gt = pgt;
		}
	}
}
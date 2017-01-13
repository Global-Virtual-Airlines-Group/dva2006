package org.deltava.acars;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.deltava.beans.schedule.Airport;
import org.deltava.util.system.SystemData;

public class PlotLocalMap extends PlotMap {
	
	private static final int MIN_ZOOM = 10;
	private static final int MAX_ZOOM = 13;
	private static final int MAX_TILES = 320 * 1024;
	
	PlotLocalMap() {
		_zooms.put(Integer.valueOf(10), new ProjectInfo(10, 64, 10));
		_zooms.put(Integer.valueOf(11), new ProjectInfo(11, 32, 6));
		_zooms.put(Integer.valueOf(12), new ProjectInfo(12, 24, 3));
		_zooms.put(Integer.valueOf(13), new ProjectInfo(13, 16, 2));
	}
	
	protected static Collection<String> getPopularAirports(int cnt, int st) throws Exception {
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
				if (cnt > 0)
					sqlBuf.append(" LIMIT ").append(st).append(',').append(cnt);
				
				try (ResultSet rs = s.executeQuery(sqlBuf.toString())) {
					while (rs.next())
						aps.add(rs.getString(1));
				}
			}
			
			// Write the track airport count
			if (st == 0) {
				try (Statement s = c.createStatement()) {
					s.execute("TRUNCATE TRACK_AIRPORTS");
				}
			}
			
			Collection<String> allAP = new LinkedHashSet<String>(aps);
			StringBuilder buf = new StringBuilder("REPLACE INTO TRACK_AIRPORTS (SELECT IATA, CNT FROM AP_COUNT WHERE IATA IN (");
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
	
	public void plotMap(int start, int count) throws Exception {
		
		// Init the filter
		Collection<String> apCodes = getPopularAirports(count, start);
		Collection<Airport> airports = apCodes.stream().map(code -> SystemData.getAirport(code)).filter(Objects::nonNull).collect(Collectors.toList());
		AirportEntryFilter f = new AirportEntryFilter(MAX_ZOOM, 95, airports);
		f.setMaxAltitude(16900);
		
		// Load flight IDs
		log.info("Loading Flight IDs");
		BlockingQueue<Integer> IDs = getIDs(0, apCodes);
		
		// Load flights - separate thread per flight
		SparseGlobalTile gt = new SparseGlobalTile(MAX_ZOOM, MAX_TILES);
		loadFlights(IDs, gt, f);
		
		// Truncate tracks
		if (start == 0)
			truncateTracks(MIN_ZOOM);
		
		while (gt.getZoom() >= MIN_ZOOM) {
			SparseGlobalTile pgt = scale(gt);
			gt = pgt;
		}
	}
}
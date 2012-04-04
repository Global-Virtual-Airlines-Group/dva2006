package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.PropertyConfigurator;
import org.deltava.beans.acars.*;
import org.deltava.beans.servinfo.*;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.*;
import org.deltava.dao.file.*;
import org.deltava.util.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestConvertArchive extends TestCase {
	
	private static final int WORKERS = 20;
	private static final String URL = "jdbc:mysql://cerberus.deltava.org/acars?user=test&password=test";
	
	protected final Queue<Integer> _IDwork = new ConcurrentLinkedQueue<Integer>();

	private final class ConvertWorker extends Thread {
		private final Connection _c;
		
		ConvertWorker(int id) throws SQLException {
			super("ConvertWorker-" + id);
			setDaemon(true);
			_c = DriverManager.getConnection(URL);
		}
		
		@Override
		public void run() {
			GetACARSPositions adao = new GetACARSPositions(_c);
			GetACARSArchive addao = new GetACARSArchive(_c);
			SetACARSArchive awdao = new SetACARSArchive(_c);
			
			Integer i = _IDwork.poll();
			while (i !=  null) {
				int id = i.intValue();
				try {
					FlightInfo inf = adao.getInfo(id);
					if (inf == null)
						throw new IllegalStateException(getName() + " - " + i + " has no FlightInfo");
					
					Collection<? extends RouteEntry> entries = inf.isXACARS() ? adao.getXACARSEntries(id) : addao.getArchivedEntries(id);
					assertNotNull(entries);
					if (entries.isEmpty())
						throw new IllegalStateException(getName() + " - " + i + " is empty");
					
					// Write data
					byte[] data = null;
					try (ByteArrayOutputStream out = new ByteArrayOutputStream(10240)) {
						SetSerializedPosition pwdao = new SetSerializedPosition(out);
						pwdao.archivePositions(id, entries);
						data = out.toByteArray();
					}
					
					// Validate
					assertTrue(data.length > 0);
					try (InputStream in = new ByteArrayInputStream(data)) {
						GetSerializedPosition prdao = new GetSerializedPosition(in);
						Collection<? extends RouteEntry> positions = prdao.read();
						assertNotNull(positions);
						assertEquals(entries.size(), positions.size());
					}
					
					// Write frame rate
					if (!inf.isXACARS()) {
						int totalFrames = 0;
						for (RouteEntry re : entries)
							totalFrames += ((ACARSRouteEntry) re).getFrameRate();
						
						try (PreparedStatement ps = _c.prepareStatement("REPLACE INTO acars.FR (ID, FRAMERATE) VALUES (?, ?)")) {
							ps.setInt(1, id);
							ps.setInt(2, totalFrames * 10 / entries.size());
							ps.executeUpdate();
						}
					}
					
					// Write to database
					awdao.archive(id, entries);
				} catch (IllegalStateException ise) {
					System.out.println(ise.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				i = _IDwork.poll();
			}
			
			try {
				_c.close();
			} catch (Exception e) { 
				// empty
			}
		}
	}
	
	private class GetACARSArchive extends org.deltava.dao.DAO {
		
		GetACARSArchive(Connection c) {
			super(c);
		}
		
		public Collection<RouteEntry> getArchivedEntries(int flightID) throws DAOException {

			try {
				prepareStatementWithoutLimits("SELECT P.REPORT_TIME, P.TIME_MS, P.LAT, P.LNG, P.B_ALT, P.R_ALT, P.HEADING, "
					+ "P.PITCH, P.BANK, P.ASPEED, P.GSPEED, P.VSPEED, P.MACH, P.N1, P.N2, P.FLAPS, P.WIND_HDG, P.WIND_SPEED, P.FUEL, "
					+ "P.FUELFLOW, P.AOA, P.GFORCE, P.FLAGS, P.FRAMERATE, P.SIM_RATE, P.PHASE, PA.COM1, PA.CALLSIGN, PA.LAT, PA.LNG, "
					+ "PA.NETWORK_ID FROM acars.POSITION_ARCHIVE P LEFT JOIN acars.POSITION_ATC PA ON (PA.FLIGHT_ID=P.FLIGHT_ID) AND "
					+ "(PA.REPORT_TIME=P.REPORT_TIME) AND (PA.TIME_MS=P.TIME_MS) WHERE (P.FLIGHT_ID=?) ORDER BY P.REPORT_TIME, P.TIME_MS");
				_ps.setInt(1, flightID);

				// Execute the query
				List<RouteEntry> results = new ArrayList<RouteEntry>();
				try (ResultSet rs = _ps.executeQuery()) {
					while (rs.next()) {
						java.util.Date dt = new java.util.Date(rs.getTimestamp(1).getTime() + rs.getInt(2));
						ACARSRouteEntry entry = new ACARSRouteEntry(dt, new GeoPosition(rs.getDouble(3), rs.getDouble(4)));
						entry.setFlags(rs.getInt(23));
						entry.setAltitude(rs.getInt(5));
						entry.setRadarAltitude(rs.getInt(6));
						entry.setHeading(rs.getInt(7));
						entry.setPitch(rs.getDouble(8));
						entry.setBank(rs.getDouble(9));
						entry.setAirSpeed(rs.getInt(10));
						entry.setGroundSpeed(rs.getInt(11));
						entry.setVerticalSpeed(rs.getInt(12));
						entry.setMach(rs.getDouble(13));
						entry.setN1(rs.getDouble(14));
						entry.setN2(rs.getDouble(15));
						entry.setFlaps(rs.getInt(16));
						entry.setWindHeading(rs.getInt(17));
						entry.setWindSpeed(rs.getInt(18));
						entry.setFuelRemaining(rs.getInt(19));	
						entry.setFuelFlow(rs.getInt(20));
						entry.setAOA(rs.getDouble(21));
						entry.setG(rs.getDouble(22));
						entry.setFrameRate(rs.getInt(24));
						entry.setSimRate(rs.getInt(25));
						entry.setPhase(rs.getInt(26));
						
						// Load ATC info
						String atcID = rs.getString(28);
						if (!StringUtils.isEmpty(atcID)) {
							entry.setCOM1(rs.getString(27));
							Controller ctr = new Controller(rs.getInt(31));
							ctr.setPosition(rs.getDouble(29), rs.getDouble(30));
							ctr.setCallsign(atcID);
							try {
								ctr.setFacility(Facility.valueOf(atcID.substring(atcID.lastIndexOf('_') + 1)));
							} catch (IllegalArgumentException iae) {
								ctr.setFacility(Facility.CTR);
							} finally {
								entry.setController(ctr);
							}
						}
						
						results.add(entry);
					}
				}

				_ps.close();
				return results;
			} catch (SQLException se) {
				throw new DAOException(se);
			}
		}
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		Class<?> c = Class.forName("com.mysql.jdbc.Driver");
		assertNotNull(c);
		Connection con = DriverManager.getConnection(URL);
		assertNotNull(con);

		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.properties");
		
		SystemData.init();
		GetTimeZone tzdao = new GetTimeZone(con);
		tzdao.initAll();
		GetAirline aldao = new GetAirline(con);
		SystemData.add("airlines", aldao.getAll());

		GetAirport apdao = new GetAirport(con);
		Map<String, Airport> airports = apdao.getAll();
		SystemData.add("airports", airports);
	}

	public void testConvert() throws SQLException {
		
		try (Connection c = DriverManager.getConnection(URL)) {
			c.setReadOnly(true);
			try (Statement s = c.createStatement()) {
				s.setFetchSize(750);
				try (ResultSet rs = s.executeQuery("SELECT ID FROM FLIGHTS WHERE ARCHIVED=1")) {
					while (rs.next())
						_IDwork.add(Integer.valueOf(rs.getInt(1)));
				}
			}
			
			try (Statement s = c.createStatement()) {
				s.setFetchSize(750);
				try (ResultSet rs = s.executeQuery("SELECT ID FROM POS_ARCHIVE")) {
					while (rs.next())
						_IDwork.remove(Integer.valueOf(rs.getInt(1)));
				}
			}
		}
		
		Collection<Thread> workers = new ArrayList<Thread>();
		for (int x = 0; x < WORKERS; x++) {
			ConvertWorker wrk = new ConvertWorker(x+1);
			workers.add(wrk);
			wrk.start();
		}
		
		ThreadUtils.waitOnPool(workers);
	}
}
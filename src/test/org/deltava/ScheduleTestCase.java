package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.TZInfo;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.FileUtils;
import org.deltava.util.cache.CacheManager;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class ScheduleTestCase extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva?useSSL=false&connectionTimezone=SERVER&allowPublicKeyRetrieval=true";
	private static final String JDBC_USER = "luke";
	private static final String JDBC_PWD = "test";

	protected Logger log;
	
	private final File DATADIR = new File("data/sched");
	
	protected final Collection<Aircraft> _acTypes = new ArrayList<Aircraft>();
	protected final Collection<Hub> _hubs = new ArrayList<Hub>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(getClass());
		
		CacheManager.init("TEST");
		SystemData.init();
		
		// Check for existing files
		DATADIR.mkdir();
		if (!FileUtils.exists(DATADIR, "tz.dat")) saveTZ(); else loadTZ();
		if (!FileUtils.exists(DATADIR, "airport.dat")) saveAirports(); else loadAirports();
		if (!FileUtils.exists(DATADIR, "aircraft.dat")) saveAircraft(); else loadAircraft();
		if (!FileUtils.exists(DATADIR, "airline.dat")) saveAirlines(); else loadAirlines();
		if (!FileUtils.exists(DATADIR, "hub.dat")) saveHubs(); else loadHubs();
	}
	
	protected static void validateFlights(Collection<RawScheduleEntry> entries) {
		for (RawScheduleEntry rse : entries) {
			assertNotNull(rse);
			assertEquals(ScheduleSource.AVSTACK, rse.getSource());
			assertNotNull(rse.getAirline());
			assertNotNull(rse.getAirportD());
			assertNotNull(rse.getAirportA());
			assertTrue(rse.getFlightNumber() > 0);
			assertEquals(1, rse.getLeg());
			assertNotNull(rse.getEquipmentType());
			assertNotNull(rse.getTimeD());
			assertNotNull(rse.getTimeA());
			assertTrue(rse.getTimeD().isBefore(rse.getTimeA()));
			assertEquals(1, rse.getDays().size());
		}
	}
	
	protected static Connection getConnection() throws Exception {
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		DriverManager.setLoginTimeout(3);
		Connection c = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PWD);
		assertNotNull(c);

		// Turn off auto-commit
		c.setAutoCommit(false);
		assertFalse(c.getAutoCommit());
		return c;
	}
	
	private void loadAircraft() throws DAOException {
		
		File f = new File(DATADIR, "aircraft.dat");
		try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(f))) {
			int cnt = oi.readInt();
			for (int x = 0; x < cnt; x++) {
				Aircraft a = (Aircraft) oi.readObject();
				_acTypes.add(a);
			}
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	private void saveAircraft() throws DAOException {
		
		// Load from database
		try (Connection c = getConnection()) {
			GetAircraft acdao = new GetAircraft(c);
			_acTypes.addAll(acdao.getAircraftTypes());
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		// Save to disk
		File f = new File(DATADIR, "aircraft.dat");
		try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(f))) {
			oo.writeInt(_acTypes.size());
			for (Aircraft ac : _acTypes)
				oo.writeObject(ac);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	private void loadAirlines() throws DAOException {
		
		File f = new File(DATADIR, "airline.dat");
		Map<String, Airline> alData = new LinkedHashMap<String, Airline>();
		try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(f))) {
			int cnt = oi.readInt();
			for (int x = 0; x < cnt; x++) {
				String key = oi.readUTF();
				Airline al = (Airline) oi.readObject();
				alData.put(key, al);
			}
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		SystemData.add("airlines", alData);
	}
	
	private void saveAirlines() throws DAOException {
		
		// Load from database
		Map<String, Airline> alData = new LinkedHashMap<String, Airline>();
		try (Connection c = getConnection()) {
			GetAirline aldao = new GetAirline(c);
			alData.putAll(aldao.getAll());
			SystemData.add("airlines", alData);
		} catch (Exception e) {
			throw new DAOException(e);
		}

		// Save to disk
		File f = new File(DATADIR, "airline.dat");
		try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(f))) {
			oo.writeInt(alData.size());
			for (Map.Entry<String, Airline> me : alData.entrySet()) {
				oo.writeUTF(me.getKey());
				oo.writeObject(me.getValue());
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	private void loadAirports() throws DAOException {
		
		Map<String, Airport> apData = new LinkedHashMap<String, Airport>();
		File f = new File(DATADIR, "airport.dat");
		try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(f))) {
			int cnt = oi.readInt();
			for (int x = 0; x < cnt; x++) {
				String key = oi.readUTF();
				Airport a = (Airport) oi.readObject();
				apData.put(key, a);
			}
		} catch (Exception e) {
			throw new DAOException(e);
		}

		SystemData.add("airports", apData);
	}
	
	private void saveAirports() throws DAOException {
		
		// Load from database
		Map<String, Airport> apData = new LinkedHashMap<String, Airport>();
		try (Connection c = getConnection()) {
			GetAirport apdao = new GetAirport(c);
			apData.putAll(apdao.getAll());
			SystemData.add("airports", apData);
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		// Save to disk
		File f = new File(DATADIR, "airport.dat");
		try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(f))) {
			oo.writeInt(apData.size());
			for (Map.Entry<String, Airport> me : apData.entrySet()) {
				oo.writeUTF(me.getKey());
				oo.writeObject(me.getValue());
			}
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	private void loadTZ() throws DAOException {
		
		File f = new File(DATADIR, "tz.dat");
		try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(f))) {
			int cnt = oi.readInt();
			for (int x = 0; x < cnt; x++) {
				TZInfo tz = (TZInfo) oi.readObject();
				TZInfo.init(tz.getID(), tz.getName(), tz.getAbbr());
			}
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	private void saveTZ() throws DAOException {
		
		// Load from database
		Collection<TZInfo> tzData = new ArrayList<TZInfo>();
		try (Connection c = getConnection()) {
			GetTimeZone tzdao = new GetTimeZone(c);
			tzdao.initAll();
			tzData.addAll(TZInfo.getAll());
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		// Save to disk
		File f = new File(DATADIR, "tz.dat");
		try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(f))) {
			oo.writeInt(tzData.size());
			for (TZInfo tz : tzData)
				oo.writeObject(tz);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
	
	public void loadHubs() throws DAOException {
	
		File f = new File(DATADIR, "hub.dat");
		try (ObjectInputStream oi = new ObjectInputStream(new FileInputStream(f))) {
			int cnt = oi.readInt();
			for (int x = 0; x < cnt; x++) {
				Hub h = (Hub) oi.readObject();
				_hubs.add(h);
			} 
		} catch (Exception e) {
			throw new DAOException(e);
		}
	}
	
	public void saveHubs() throws DAOException {
		
		// Load from database
		try (Connection c = getConnection()) {
			GetRawScheduleInfo rsdao = new GetRawScheduleInfo(c);
			_hubs.addAll(rsdao.getHubs());
		} catch (Exception e) {
			throw new DAOException(e);
		}
		
		// Save to disk
		File f = new File(DATADIR, "hub.dat");
		try (ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(f))) {
			oo.writeInt(_hubs.size());
			for (Hub h : _hubs)
				oo.writeObject(h);
		} catch (IOException ie) {
			throw new DAOException(ie);
		}
	}
}
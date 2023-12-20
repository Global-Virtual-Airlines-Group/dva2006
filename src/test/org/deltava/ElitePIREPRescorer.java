package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.apache.logging.log4j.*;

import org.deltava.beans.*;
import org.deltava.beans.acars.RunwayDistance;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;

import org.deltava.dao.*;

import org.deltava.util.ConfigLoader;
import org.deltava.util.cache.CacheLoader;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class ElitePIREPRescorer extends TestCase {
	
	private Logger log;
	private Connection _c;
	
	private static final String AIRLINE_CODE = "DVA";
	private static final String JDBC_URL = String.format("jdbc:mysql://sirius.sce.net/%s?connectionTimezone=SERVER&allowPublicKeyRetrieval=true", AIRLINE_CODE.toLowerCase());
	private static final String JDBC_USER = "luke";
	private static final String JDBC_PWD = "test";
	
	private static final int YEAR = 2023;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		System.setProperty("user.timezone", "UTC"); // This is to ensure MySQL doesn't switch time zones on the fly
		System.setProperty("log4j2.configurationFile", new java.io.File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(ElitePIREPRescorer.class);
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PWD);
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
		
		// Initialize System Data
		SystemData.init();
		
		// Initialize caches
		try (InputStream is = ConfigLoader.getStream("/etc/cacheInfo-test.xml")) {
			CacheLoader.load(is);
		} catch(IOException ie) {
			log.warn("Cannot configure caches from code");
		}
		
		// Load Time Zones
		GetTimeZone dao = new GetTimeZone(_c);
		dao.initAll();
		log.info("Loaded Time Zones");
		
		// Load country codes
		log.info("Loading Country codes");
		GetCountry cdao = new GetCountry(_c);
		log.info("Loaded " + cdao.initAll() + " Country codes");

		// Load Database information
		log.info("Loading Cross-Application data");
		GetUserData uddao = new GetUserData(_c);
		SystemData.add("apps", uddao.getAirlines(true));

		// Load active airlines
		log.info("Loading Airline Codes");
		GetAirline aldao = new GetAirline(_c);
		SystemData.add("airlines", aldao.getAll());

		// Load airports
		log.info("Loading Airports");
		GetAirport apdao = new GetAirport(_c);
		SystemData.add("airports", apdao.getAll());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadData() throws Exception {
		
		GetPilot pdao = new GetPilot(_c);
		GetElite eldao = new GetElite(_c);
		GetAircraft acdao = new GetAircraft(_c);
		GetACARSData fidao = new GetACARSData(_c);
		GetFlightReports frdao = new GetFlightReports(_c);
		
		SetElite elwdao = new SetElite(_c);
		SetFlightReport frwdao = new SetFlightReport(_c);
		SetStatusUpdate updwdao = new SetStatusUpdate(_c);
		
		// Load Pilots
		Collection<Integer> IDs = new TreeSet<Integer>();
		try (PreparedStatement ps = _c.prepareStatement("SELECT DISTINCT P.ID FROM PILOTS P, PIREPS PR WHERE (P.ID=PR.PILOT_ID) AND (PR.STATUS=?) AND (PR.DATE>=MAKEDATE(?,1)) AND (PR.DATE<MAKEDATE(?,1))")) {
			ps.setInt(1, FlightStatus.OK.ordinal());	
			ps.setInt(2, YEAR);
			ps.setInt(3, YEAR + 1);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}
		}
		
		log.info("Loaded {} Pilots", Integer.valueOf(IDs.size()));
		
		// Clean PIREP_ELITE
		try (PreparedStatement ps = _c.prepareStatement("DELETE PE.* FROM PIREP_ELITE PE, PIREPS P WHERE (P.ID=PE.ID) AND (YEAR(P.DATE)=?)")) {
			ps.setInt(1, YEAR);
			int cnt = ps.executeUpdate();
			log.info("Deleted {} {} Flight Report entries for {}", Integer.valueOf(cnt), SystemData.getObject("econ.elite.name"), Integer.valueOf(YEAR));
		}
		
		// Clean ELITE_STATUS
		try (PreparedStatement ps = _c.prepareStatement("DELETE FROM ELITE_STATUS WHERE (YR=?) AND (UPD_REASON<>?) AND (UPD_REASON<>?)")) {
			ps.setInt(1, YEAR);
			ps.setInt(2, UpgradeReason.ROLLOVER.ordinal());
			ps.setInt(3, UpgradeReason.DOWNGRADE.ordinal());
			int cnt = ps.executeUpdate();
			log.info("Deleted {} {} Status entries for {}", Integer.valueOf(cnt), SystemData.getObject("econ.elite.name"), Integer.valueOf(YEAR));
		}
	
		// Clean STATUS_UPDATE
		try (PreparedStatement ps = _c.prepareStatement("DELETE FROM STATUS_UPDATES WHERE (TYPE=?) AND (YEAR(CREATED)=?)")) {
			ps.setInt(1, UpdateType.ELITE_QUAL.ordinal());
			ps.setInt(2, YEAR);
			int cnt = ps.executeUpdate();
			log.info("Deleted {} {} Pilot History entries for {}", Integer.valueOf(cnt), SystemData.getObject("econ.elite.name"), Integer.valueOf(YEAR));
		}
		
		TreeSet<EliteLevel> lvls = eldao.getLevels(YEAR);
		for (Integer id : IDs) {
			EliteScorer es = EliteScorer.getInstance();
			Pilot p = pdao.get(id.intValue());
			log.info("Calculating {} status for {} ({}) [{}]", SystemData.getObject("econ.elite.name"), p.getName(), p.getPilotCode(), Integer.valueOf(p.getID()));
			List<FlightReport> pireps = frdao.getEliteFlights(id.intValue(), YEAR);
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();

			// Get pilot's elite status
			List<EliteStatus> pilotStatus = eldao.getAllStatus(p.getID(), YEAR);
			EliteStatus st = pilotStatus.isEmpty() ? null : pilotStatus.getLast();
			if (st == null) {
				st = new EliteStatus(p.getID(), lvls.first());
				st.setEffectiveOn(pireps.get(0).getDate());
				elwdao.write(st);
			}
			
			log.info("{} is {} for {}", p.getName(), st.getLevel().getName(), Integer.valueOf(YEAR));
			YearlyTotal yt = new YearlyTotal(YEAR, p.getID()); EliteLevel nextLevel = lvls.higher(st.getLevel());
			for (FlightReport fr : pireps) {
				FlightEliteScore sc = null;
				if (fr instanceof FDRFlightReport ffr) {
					Aircraft ac = acdao.get(fr.getEquipmentType());
					AircraftPolicyOptions opts = ac.getOptions(AIRLINE_CODE);
					
					// Get the landing runway
					RunwayDistance rwyA = fidao.getLandingRunway(fr.getDatabaseID(DatabaseID.ACARS));
					
					// Create the package
					ScorePackage pkg = new ScorePackage(ac, ffr, null, rwyA, opts);
					sc = es.score(pkg, st.getLevel());
				} else
					sc = es.score(fr, st.getLevel());
				
				// Typically rejected flights
				if (sc == null) continue;
				es.add(fr);

				sc.setAuthorID(fr.getAuthorID());
				//fr.addStatusUpdate(0, HistoryType.ELITE, String.format("Updated %s activity - %d %s (%s)", SystemData.get("econ.elite.name"), Integer.valueOf(sc.getPoints()), SystemData.get("econ.elite.points"), st.getLevel().toString()));
				frwdao.writeElite(sc, AIRLINE_CODE);
				//frwdao.writeHistory(fr.getStatusUpdates(), AIRLINE_CODE);
				
				// Check for upgrade
				UpgradeReason updR = yt.wouldMatch(nextLevel, sc); 
				if (updR != UpgradeReason.NONE) {
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_QUAL);
					upd.setDate(fr.getDisposedOn());
					upd.setAuthorID(p.getID());
					upd.setDescription(String.format("Reached %s for %d ( %s )", nextLevel.getName(), Integer.valueOf(YEAR), updR.getDescription()));
					upds.add(upd);
					
					log.info("{} reaches {} for {} / {}", p.getName(), nextLevel.getName(), Integer.valueOf(YEAR), updR.getDescription());
					st = new EliteStatus(p.getID(), nextLevel);
					st.setEffectiveOn(fr.getDisposedOn());
					st.setUpgradeReason(updR);
					elwdao.write(st);
					nextLevel = lvls.higher(st.getLevel());
				} else if (yt.wouldMatch(st.getLevel(), sc) != UpgradeReason.NONE)
					log.info("{} retains {} for {}", p.getName(), st.getLevel().getName(), Integer.valueOf(YEAR + 1));
				
				yt.add(sc);
			}
			
			updwdao.write(upds);
			_c.commit();
		}
	}
}
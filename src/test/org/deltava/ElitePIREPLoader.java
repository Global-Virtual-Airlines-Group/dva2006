package org.deltava;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.time.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.*;
import org.deltava.beans.econ.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.schedule.*;
import org.deltava.beans.acars.RunwayDistance;

import org.deltava.dao.*;

import org.deltava.util.ConfigLoader;
import org.deltava.util.cache.CacheLoader;
import org.deltava.util.system.SystemData;

public class ElitePIREPLoader extends TestCase {
	
	private Logger log;
	private Connection _c;
	
	private static final int MIN_YEAR = 2022;
	private static final LocalDateTime MIN_DATE = LocalDate.of(MIN_YEAR, 1, 1).atStartOfDay();

	private static final String AIRLINE_CODE = "DVA";
	private static final String JDBC_URL = String.format("jdbc:mysql://sirius.sce.net/%s?connectionTimezone=SERVER&allowPublicKeyRetrieval=true", AIRLINE_CODE.toLowerCase());
	private static final String JDBC_USER = "luke";
	private static final String JDBC_PWD = "test";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		System.setProperty("user.timezone", "UTC"); // This is to ensure MySQL doesn't switch time zones on the fly
		System.setProperty("log4j2.configurationFile", new java.io.File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(EliteLevelLoader.class);

		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PWD);
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
		
		// Initialize System Data
		SystemData.init();
		
		// Load Caches
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
		
		// Load Pilots we've already populated
		Collection<Integer> populatedIDs = new HashSet<Integer>();
		try (Statement s = _c.createStatement(); ResultSet rs = s.executeQuery("SELECT DISTINCT P.PILOT_ID FROM PIREPS P, PIREP_ELITE PE WHERE (PE.ID=P.ID)")) {
			while (rs.next())
				populatedIDs.add(Integer.valueOf(rs.getInt(1)));
		}
		
		Collection<Integer> IDs = new TreeSet<Integer>();
		//IDs.addAll(List.of(604224, 8027, 640163, 10108));
		try (PreparedStatement ps = _c.prepareStatement("SELECT P.ID FROM PILOTS P, PIREPS PR WHERE (P.ID=PR.PILOT_ID) AND (PR.STATUS=?) AND (PR.DATE>?)")) {
			ps.setInt(1, FlightStatus.OK.ordinal());
			ps.setTimestamp(2, new Timestamp(MIN_DATE.toEpochSecond(ZoneOffset.UTC) * 1000));
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next())
					IDs.add(Integer.valueOf(rs.getInt(1)));
			}
		}
		
		int size = IDs.size();
		log.info("Loaded {} Pilots", Integer.valueOf(size));
		IDs.removeIf(id -> populatedIDs.contains(id));
		log.info("Removed {} Pilots with populated data", Integer.valueOf(size - IDs.size()));
		
		LogbookSearchCriteria lsc = new LogbookSearchCriteria("DATE, PR.SUBMITTED", SystemData.get("airline.db"));
		for (Integer id : IDs) {
			Pilot p = pdao.get(id.intValue());
			List<FlightReport> allFlights = frdao.getByPilot(id.intValue(), lsc);
			Collection<Integer> yrs = allFlights.stream().map(fr -> Integer.valueOf(EliteScorer.getStatsYear(fr.getDate()))).filter(y -> y.intValue() >= MIN_YEAR).collect(Collectors.toCollection(TreeSet::new));
			log.info("Calculating {} status for {} ({})", SystemData.getObject("econ.elite.name"), p.getName(), p.getPilotCode());
			Collection<StatusUpdate> upds = new ArrayList<StatusUpdate>();
			
			for (Integer yr : yrs) {
				EliteScorer es = EliteScorer.getInstance();
				final int y = yr.intValue(); TreeSet<EliteLevel> lvls = eldao.getLevels(y);
				List<FlightReport> pireps = allFlights.stream().filter(fr -> (EliteScorer.getStatsYear(fr.getDate()) == y)).collect(Collectors.toList());

				// Get pilot's elite status 
				List<EliteStatus> pilotStatus = eldao.getAllStatus(p.getID(), y);
				EliteStatus st = pilotStatus.isEmpty() ? null : pilotStatus.get(pilotStatus.size() - 1);
				if (st == null) {
					st = new EliteStatus(p.getID(), lvls.first());
					st.setEffectiveOn(pireps.get(0).getDate());
					elwdao.write(st);
					log.info("{} is {} for {}", p.getName(), st.getLevel().getName(), yr);
				}
				
				YearlyTotal yt = new YearlyTotal(y, p.getID()); EliteLevel nextLevel = lvls.higher(st.getLevel());
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
					es.add(fr);
					if (sc == null) continue;
					
					sc.setAuthorID(fr.getAuthorID());
					fr.addStatusUpdate(0, HistoryType.ELITE, String.format("Updated %s activity - %d %s (%s)", SystemData.get("econ.elite.name"), Integer.valueOf(sc.getPoints()), SystemData.get("econ.elite.points"), st.getLevel().toString()));
					frwdao.writeElite(sc, AIRLINE_CODE);
					frwdao.writeHistory(fr.getStatusUpdates(), AIRLINE_CODE);
					
					// Check for upgrade
					UpgradeReason updR = yt.wouldMatch(nextLevel, sc); 
					if (updR != UpgradeReason.NONE) {
						StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_QUAL);
						upd.setDate(fr.getDisposedOn());
						upd.setAuthorID(p.getID());
						upd.setDescription(String.format("Reached %s for %d ( %s )", nextLevel.getName(), yr, updR.getDescription()));
						upds.add(upd);
						
						log.info(p.getName() + " reaches " + nextLevel.getName() + " for " + yr + " / " + updR.getDescription());
						st = new EliteStatus(p.getID(), nextLevel);
						st.setEffectiveOn(fr.getDisposedOn());
						st.setUpgradeReason(updR);
						elwdao.write(st);
						nextLevel = lvls.higher(st.getLevel());
					} else if (yt.wouldMatch(st.getLevel(), sc) != UpgradeReason.NONE)
						log.info("{} retains {} for {}", p.getName(), st.getLevel().getName(), Integer.valueOf(y + 1));
					
					yt.add(sc);
				}
				
				// Calculate end of year status
				EliteLevel eyLevel = yt.matches(lvls);
				if ((eyLevel != null) && (eyLevel.getLegs() > 0) && (eyLevel.getYear() < 2023)) {
					UpgradeReason ur = (eyLevel.compareTo(st.getLevel()) == 0) ? UpgradeReason.ROLLOVER : UpgradeReason.DOWNGRADE;
					
					// Get next year's level
					EliteLevel rl = eldao.get(eyLevel.getName(), y+1, AIRLINE_CODE);
					if (rl == null)
						log.error("No {} level for {}!", eyLevel.getName(), Integer.valueOf(y + 1)); // this will fail next line, but tells us why

					EliteStatus rs = new EliteStatus(p.getID(), rl);
					rs.setEffectiveOn(LocalDateTime.of(y + 1, 2, 1, 12, 0, 0).toInstant(ZoneOffset.UTC));
					rs.setUpgradeReason(ur);
					elwdao.write(rs);
					
					StatusUpdate upd = new StatusUpdate(p.getID(), UpdateType.ELITE_ROLLOVER);
					upd.setDate(LocalDateTime.of(y + 1, 2, 1, 12, 0,0).toInstant(ZoneOffset.UTC));
					upd.setAuthorID(p.getID());
					if (ur == UpgradeReason.ROLLOVER) {
						log.info("{} rolls over {} for {}", p.getName(), eyLevel.getName(), Integer.valueOf(y + 1));
						upd.setDescription(String.format("Rolls over %s for %d", eyLevel.getName(), Integer.valueOf(y + 1)));
					} else {
						log.info("{} downgraded to {} for {}", p.getName(), eyLevel.getName(), Integer.valueOf(y + 1));
						upd.setDescription(String.format("Downgraded to %s for %d", eyLevel.getName(), Integer.valueOf(y + 1)));
					}
					
					upds.add(upd);
				} else if (yt.getYear() < 2023)
					log.info("{} no rollover for {}", p.getName(), Integer.valueOf(y + 1));
			}
			
			updwdao.write(upds);
			_c.commit();
			log.info("Commited data for {}", p.getName());
		}
	}
}
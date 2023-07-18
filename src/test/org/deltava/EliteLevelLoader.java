package org.deltava;

import java.sql.*;
import java.time.*;
import java.util.List;

import org.apache.logging.log4j.*;

import org.deltava.beans.econ.*;
import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class EliteLevelLoader extends TestCase {
	
	private static final String[] LEVEL_NAMES = new String[] {"Silver", "Gold", "Platinum", "Diamond", "360°"};
	private static final String[] LEVEL_COLORS = new String[] {"747578", "936C37", "483048", "57798F", "001028"};
	private static final int[] LEVEL_PCTS = new int[] {60, 80, 90, 95, 99};
	private static final float[] LEVEL_BOOSTS = new float[] {0.1f, 0.25f, 0.5f, 0.8f, 0.95f};
	
	private Logger log;
	private Connection _c;
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/dva";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		System.setProperty("user.timezone", "UTC"); // This is to ensure MySQL doesn't switch time zones on the fly
		System.setProperty("log4j2.configurationFile", new java.io.File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(EliteLevelLoader.class);

		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		super.tearDown();
	}

	public void testLoadLevels() throws DAOException, SQLException {
	
		GetFlightReportStatistics frsdao = new GetFlightReportStatistics(_c);
		SetElite ewdao = new SetElite(_c);
		
		int legRound = SystemData.getInt("econ.elite.round.leg", 5); int dstRound = SystemData.getInt("econ.elite.round.distance", 5000);
		for (int year = EliteLevel.MIN_YEAR; year < 2024; year++) {
			LocalDate sd = LocalDate.of(year - 2, 12, 1);
			List<YearlyTotal> totals = frsdao.getPilotTotals(sd);
			FlightPercentileHelper fHelper = new FlightPercentileHelper(totals, 1);
			PercentileStatsEntry lpse = fHelper.getLegs();
			PercentileStatsEntry dpse = fHelper.getDistance();
			for (int x = 0; x < LEVEL_NAMES.length; x++) {
				EliteLevel lvl = new EliteLevel(year, LEVEL_NAMES[x], SystemData.get("airline.code"));
				lvl.setTargetPercentile(LEVEL_PCTS[x]);
				lvl.setLegs(EliteLevel.round(lpse.getLegs(lvl.getTargetPercentile()), legRound));
				lvl.setDistance(EliteLevel.round(dpse.getDistance(lvl.getTargetPercentile()), dstRound));
				lvl.setColor(Integer.parseInt(LEVEL_COLORS[x], 16));
				lvl.setBonusFactor(LEVEL_BOOSTS[x]);
				lvl.setVisible(x < (LEVEL_NAMES.length - 1));
				lvl.setStatisticsStartDate(sd.atStartOfDay().toInstant(ZoneOffset.UTC));
				log.info("{} for {} = {} legs, {} miles", lvl.getName(), Integer.valueOf(year), Integer.valueOf(lvl.getLegs()), Integer.valueOf(lvl.getDistance()));
				ewdao.write(lvl);
			}
			
			// Write default "member" entry
			EliteLevel lvl = new EliteLevel(year, "Member", SystemData.get("airline.code"));
			lvl.setTargetPercentile(0);
			lvl.setLegs(0); lvl.setDistance(0); lvl.setPoints(0);
			lvl.setBonusFactor(0);
			lvl.setVisible(false);
			lvl.setColor(Integer.parseInt("1a4876", 16));
			lvl.setStatisticsStartDate(sd.atStartOfDay().toInstant(ZoneOffset.UTC));
			ewdao.write(lvl);
		}
		
		_c.commit();
	}
}
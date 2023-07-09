package org.deltava;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;

import org.apache.logging.log4j.*;
import org.deltava.beans.econ.EliteLevel;
import org.deltava.beans.stats.PercentileStatsEntry;

import org.deltava.dao.*;

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
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
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
		
		for (int year = 2004; year < 2023; year++) {
			PercentileStatsEntry pse = frsdao.getFlightPercentiles(LocalDate.of(year - 1, 1, 1), 1, false);
			for (int x = 0; x < LEVEL_NAMES.length; x++) {
				EliteLevel lvl = new EliteLevel(year, LEVEL_NAMES[x]);
				lvl.setTargetPercentile(LEVEL_PCTS[x]);
				lvl.setLegs(round(pse.getLegs(lvl.getTargetPercentile()), 5));
				lvl.setDistance(round(pse.getDistance(lvl.getTargetPercentile()), 10000));
				lvl.setColor(Integer.parseInt(LEVEL_COLORS[x], 16));
				lvl.setBonusFactor(LEVEL_BOOSTS[x]);
				lvl.setVisible(true);
				log.info("{} for {} = {} legs, {} miles", lvl.getName(), Integer.valueOf(year), Integer.valueOf(lvl.getLegs()), Integer.valueOf(lvl.getDistance()));
				ewdao.write(lvl);
			}
		}
		
		_c.commit();
	}
	
	private static int round(int value, int rndTo) {
		return (rndTo == 1) ? value : ((value / rndTo) + 1) * rndTo;
	}
}
package org.deltava;

import java.sql.*;
import java.util.*;

import junit.framework.TestCase;

import org.apache.log4j.*;

import org.deltava.beans.flight.*;
import org.deltava.util.TaskTimer;

public class PIREPScoreLoader extends TestCase {
	
	private static final String JDBC_URL = "jdbc:mysql://sirius.sce.net/afv";
	private Connection _c;
	
	private Logger log;
	
	private class FlightData {
		private int _id;
		private int _vSpeed;
		private int _rwyDistance;
		private double _score;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(SimGateLoader.class);
		
		// Connect to the database
		Class.forName("com.mysql.cj.jdbc.Driver");
		DriverManager.setLoginTimeout(3);
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
	}

	@Override
	protected void tearDown() throws Exception {
		_c.close();
		LogManager.shutdown();
		super.tearDown();
	}

	public void testLoadScores() throws Exception {
		Collection<FlightData> results = new ArrayList<FlightData>();
		TaskTimer tt = new TaskTimer();
		try (PreparedStatement ps = _c.prepareStatement("SELECT AP.ID, AP.LANDING_VSPEED, R.DISTANCE FROM PIREPS P, ACARS_PIREPS AP, acars.RWYDATA R WHERE (P.ID=AP.ID) AND (AP.ACARS_ID=R.ID) AND (AP.LANDING_SCORE<0) AND (R.ISTAKEOFF=0)")) {
			ps.setFetchSize(2500);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					FlightData fd = new FlightData();
					fd._id = rs.getInt(1);
					fd._vSpeed = rs.getInt(2);
					fd._rwyDistance = rs.getInt(3);
					results.add(fd);
				}
			}
		}
		
		log.info(String.format("%d flights loaded in %d ms", Integer.valueOf(results.size()), Long.valueOf(tt.stop())));
		
		// Calculate scores
		tt.start();
		results.parallelStream().forEach(fd -> fd._score = LandingScorer.score(fd._vSpeed, fd._rwyDistance));
		log.info(String.format("%d scores calculated in %d ms", Integer.valueOf(results.size()), Long.valueOf(tt.stop())));

		_c.setAutoCommit(false);
		tt.start(); int cnt = 0;
		try (PreparedStatement ps = _c.prepareStatement("UPDATE ACARS_PIREPS SET LANDING_SCORE=? WHERE (ID=?)")) {
			for (FlightData fd : results) {
				cnt++;
				if (Math.abs(fd._score) > 250)
					log.warn("ID = " + fd._id + " Score = " + fd._score);
				
				ps.setInt(1, (int)Math.round(fd._score * 100));
				ps.setInt(2, fd._id);
				ps.addBatch();
				if ((cnt % 2500) == 0)
					ps.executeBatch();
			}
			
			if ((cnt % 2500) > 0)
				ps.executeBatch();
			
			_c.commit();
		}
		
		log.info(String.format("%d scores written in %d ms", Integer.valueOf(results.size()), Long.valueOf(tt.stop())));
	}
}
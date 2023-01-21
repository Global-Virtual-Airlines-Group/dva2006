package org.deltava.beans.flight;

import org.apache.log4j.*;

import junit.framework.TestCase;

public class TestLandingScorer extends TestCase {
	
	private Logger log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(TestLandingScorer.class);
	}

	@Override
	protected void tearDown() throws Exception {
		LogManager.shutdown();
		super.tearDown();
	}

	public void testVSScoreProgression() {
		for (int vs = 25; vs < 1200; vs += 25) {
			double score = LandingScorer.score(-vs, LandingScorer.OPT_DISTANCE);
			assertTrue(score > -1);
			log.info(String.format("VS = %d, Score = %2$,.2f", Integer.valueOf(-vs), Double.valueOf(score)));
		}
	}
	
	public void testDistanceScoreProgression() {
		for (int dst= 0; dst < 3500; dst += 100) {
			double score = LandingScorer.score(LandingScorer.OPT_VSPEED, dst);
			assertTrue(score > -1);
			log.info(String.format("Dist = %d, Score = %2$,.2f", Integer.valueOf(dst), Double.valueOf(score)));
		}
	}
}
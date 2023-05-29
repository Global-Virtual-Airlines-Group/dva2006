package org.deltava.beans.flight;

import java.io.File;

import org.apache.logging.log4j.*;

import junit.framework.TestCase;

public class TestLandingScorer extends TestCase {
	
	private Logger log;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		// Init Log4j
		System.setProperty("log4j2.configurationFile", new File("etc/log4j2-test.xml").getAbsolutePath());
		log = LogManager.getLogger(TestLandingScorer.class);
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
package org.deltava;

import java.sql.*;
import java.util.*;

import org.apache.log4j.*;

import junit.framework.TestCase;

import org.deltava.beans.*;
import org.deltava.beans.flight.*;
import org.deltava.beans.stats.*;

import org.deltava.dao.*;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

public class AccomplishmentLoader extends TestCase {

	private static Logger log;

	private static final String JDBC_URL = "jdbc:mysql://pollux.gvagroup.org/dva";

	private Connection _c;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Init Log4j
		PropertyConfigurator.configure("etc/log4j.test.properties");
		log = Logger.getLogger(RunwayLoader.class);

		// Connect to the database
		Class.forName("com.mysql.jdbc.Driver");
		_c = DriverManager.getConnection(JDBC_URL, "luke", "test");
		assertNotNull(_c);
		_c.setAutoCommit(false);
		assertFalse(_c.getAutoCommit());

		// Initialize System Data
		SystemData.init();

		// Load Time Zones
		GetTimeZone dao = new GetTimeZone(_c);
		log.info("Loaded " + dao.initAll() + " Time Zones");

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

	public void testLoadAccomplishments() throws Exception {

		// Truncate accomplishments
		try (PreparedStatement ps = _c.prepareStatement("DELETE FROM PILOT_ACCOMPLISHMENTS WHERE (PILOT_ID=?)")) {

			// Load accomplishments
			GetAccomplishment acdao = new GetAccomplishment(_c);
			Collection<Accomplishment> accs = acdao.getAll();

			// Load all pilots
			GetPilot pdao = new GetPilot(_c);
			GetFlightReports frdao = new GetFlightReports(_c);
			SetAccomplishment awdao = new SetAccomplishment(_c);
			// Collection<Pilot> pilots = Collections.singleton(pdao.get(8027));
			Collection<Pilot> pilots = pdao.getPilots();
			for (Pilot p : pilots) {
				log.info("Checking " + p.getName());
				Collection<FlightReport> pireps = frdao.getByPilot(p.getID(), null);
				AccomplishmentHistoryHelper helper = new AccomplishmentHistoryHelper(p);
				for (FlightReport fr : pireps)
					helper.add(fr);

				// Clear accomplishments
				ps.setInt(1, p.getID());
				ps.executeUpdate();

				// Walk through the accomplishments
				boolean hasUpdate = false;
				for (Accomplishment a : accs) {
					java.time.Instant dt = helper.achieved(a);
					if (dt != null) {
						log.info(p.getName() + " achieved " + a.getName() + " on " + StringUtils.format(dt, "MM/dd/yyyy"));
						awdao.achieve(p.getID(), a, dt);
						hasUpdate = true;
					}
				}

				if (hasUpdate)
					_c.commit();
			}
		}
	}
}
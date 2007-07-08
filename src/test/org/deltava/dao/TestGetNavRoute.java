package org.deltava.dao;

import java.util.*;

import org.deltava.beans.navdata.*;

import org.deltava.util.StringUtils;

public class TestGetNavRoute extends AbstractDAOTestCase {
	
	private GetNavRoute _dao;

	protected void setUp() throws Exception {
		super.setUp();
		_dao = new GetNavRoute(_con);
	}

	protected void tearDown() throws Exception {
		_dao = null;
		super.tearDown();
	}

	public void testWaypoints() throws DAOException {
		String route = "KDFW SLOTT MANKI OWING GTH TCC LVS FLYBY BUKKO GUP DILCO MOSBI CUTRO PGS VEEVA KLAS";
		List<String> entries = new ArrayList<String>(StringUtils.split(route, " "));
		for (Iterator<String> i = entries.iterator(); i.hasNext(); ) {
			String wp = i.next();
			NavigationDataMap ndmap = _dao.get(wp);
			assertNotNull(ndmap);
			assertTrue(ndmap.contains(wp));
			NavigationDataBean ndb = ndmap.get(wp);
			assertNotNull(ndb);
			assertEquals(wp, ndb.getCode());
			System.out.println("Loaded " + ndb.getTypeName() + " " + wp);
		}
		
		// Load the entire route
		NavigationDataMap ndmap = _dao.getByID(entries);
		assertNotNull(ndmap);
		for (int x = 0; x < entries.size(); x++) {
			String wp = entries.get(x);
			NavigationDataBean ndb = ndmap.get(wp);
			assertNotNull(ndb);
			assertEquals(wp, ndb.getCode());
		}
	}
	
	
	// KLAX EHF J65 RBL J1 OED J501 YZP TR18 KATCH B327 ODK R341 NATES R220 GOC R211 COMET RJAA
	public void testAirways() throws DAOException {
		String route = "KLAX EHF J65 RBL J1 OED J501 YZP KATCH B327 ODK R341 NATES R220 GOC R211 COMET RJAA";
		List<String> entries = new ArrayList<String>(StringUtils.split(route, " "));
		for (Iterator<String> i = entries.iterator(); i.hasNext(); ) {
			String wp = i.next();
			NavigationDataMap ndmap = _dao.get(wp);
			assertNotNull(ndmap);
			NavigationDataBean ndb = ndmap.get(wp);
			if (ndb == null) {
				Airway a = _dao.getAirway(wp);
				assertNotNull(a);
				assertEquals(wp, a.getCode());
				System.out.println("Loaded Airway " + wp);
			} else {
				assertNotNull(ndb);
				assertEquals(wp, ndb.getCode());
				System.out.println("Loaded " + ndb.getTypeName() + " " + wp);
			}
		}
		
		// Load the entire route
		NavigationDataMap ndmap = _dao.getByID(entries);
		assertNotNull(ndmap);

	}
}
package org.deltava.dao;

import java.util.Map;

public class TestGetAirport extends AbstractDAOTestCase {

    private GetAirport _dao;
    
    protected void setUp() throws Exception {
        super.setUp();
        _dao = new GetAirport(_con);
    }

    protected void tearDown() throws Exception {
        _dao = null;
        super.tearDown();
    }

    public void testAirportMap() throws DAOException {
        Map<String, ?> airports = _dao.getAll();
        
        assertTrue(airports.containsKey("ATL"));
        assertTrue(airports.containsKey("KATL"));
        
        assertEquals(airports.get("ATL"), airports.get("KATL"));
    }
}

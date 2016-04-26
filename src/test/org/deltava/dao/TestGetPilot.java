package org.deltava.dao;

import org.deltava.beans.Pilot;

import org.deltava.util.system.SystemData;

public class TestGetPilot extends AbstractDAOTestCase {

    private GetPilot _dao;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        SystemData.init();
        _dao = new GetPilot(_con);
    }

    @Override
	protected void tearDown() throws Exception {
        _dao = null;
        super.tearDown();
    }

    public void testGetPilot() throws DAOException {
        Pilot p = _dao.get(8027);
        assertNotNull(p);
        assertEquals("Luke", p.getFirstName());
        assertEquals("Kolin", p.getLastName());
        assertTrue(p.hasRating("CV-880"));
        
        Pilot p2 = _dao.getPilotByCode(57, "DVA");
        assertNotNull(p2);
        assertEquals(8402, p2.getID());
        assertEquals(57, p2.getPilotNumber());
        assertEquals("DVA057", p2.getPilotCode());
        assertEquals("Terry", p2.getFirstName());
        assertEquals("Eshenour", p2.getLastName());
    }
}
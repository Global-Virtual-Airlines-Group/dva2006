package org.deltava.dao;

public class TestGetCount extends AbstractDAOTestCase {

    private GetCount _dao;

    protected void setUp() throws Exception {
        super.setUp();
        _dao = new GetCount(_con);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        _dao = null;
    }
    
    public void testCount() throws DAOException {
        int count = _dao.execute("PIREPS");
        assertEquals(66991, count);
        count = _dao.execute("STAFF");
        assertEquals(21, count);
    }
}
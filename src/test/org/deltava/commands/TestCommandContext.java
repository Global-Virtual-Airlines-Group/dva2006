package org.deltava.commands;

import java.sql.Connection;

import junit.framework.Test;
import junit.framework.TestCase;

import org.gvagroup.jdbc.ConnectionPool;
import org.hansel.CoverageDecorator;

import javax.servlet.http.*;
import com.kizna.servletunit.*;

import org.deltava.beans.Pilot;
import org.deltava.jdbc.ConnectionContext.ConnectionPoolException;

import org.deltava.servlet.filter.CustomRequestWrapper;

import org.deltava.util.system.SystemData;

public class TestCommandContext extends TestCase {

    private CommandContext _ctxt;
    
    private HttpServletRequest _req;
    private HttpServletResponse _rsp;
    private HttpServletRequestSimulator _rootReq;
    
    public static Test suite() {
        return new CoverageDecorator(TestCommandContext.class, new Class[] { CommandContext.class } );
    }    
    
    @Override
	protected void setUp() throws Exception {
    	super.setUp();
    	_rootReq = new HttpServletRequestSimulator();
    	_req = new CustomRequestWrapper(_rootReq);
    	_rsp = new HttpServletResponseSimulator();
    	_ctxt = new CommandContext(_req, _rsp);
    }
    
    @Override
	protected void tearDown() throws Exception {
        _ctxt = null;
        _req = null;
        _rootReq = null;
        super.tearDown();
    }

    public void testConstructor() {
        HttpSession s = _req.getSession(true);
        assertEquals(_req, _ctxt.getRequest());
        assertEquals(_rsp, _ctxt.getResponse());
        assertEquals(s, _ctxt.getSession());
    }
    
    public void testDB() {
    	assertNull(_ctxt.getDB());
    	_ctxt.setDB("dva-db");
    	assertEquals("dva-db", _ctxt.getDB());
    }
    
    public void testParameters() throws Exception {
    	assertNull(_ctxt.getCmdParameter(Command.ID, null));
    	assertNull(_ctxt.getCmdParameter(Command.OPERATION, null));
    	assertEquals("ID", _ctxt.getCmdParameter(Command.ID, "ID"));
    	assertEquals("OP", _ctxt.getCmdParameter(Command.OPERATION, "OP"));
    	_rootReq.addParameter("id", "0x21");
    	assertEquals("0x21", _ctxt.getParameter("id"));
    	assertEquals(33, _ctxt.getID());
    	_rootReq.setParameterValue("id", new String[] { "ABC" });
    	assertEquals("ABC", _ctxt.getCmdParameter(Command.ID, null));
    	
    	_rootReq.setParameterValue("id", new String[] { "0xCRAP" });
    	assertEquals("0xCRAP", _ctxt.getCmdParameter(Command.ID, null));
    	try {
    		int id = _ctxt.getID();
    		assertEquals(0, id);
    		fail("CommandException expected");
    	} catch (CommandException ce) {
    		// empty
    	}
    	
    	_rootReq.addParameter("op", "OPERATION");
    	assertEquals("OPERATION", _ctxt.getCmdParameter(Command.OPERATION, null));
    }
    
    public void testMessage() {
    	_ctxt.setMessage("msg");
		assertEquals("msg", _req.getAttribute("system_message"));
    }
    
    public void testConnectionPool() {
    	try {
    		Connection c = _ctxt.getConnection();
    		assertNull(c);
    		fail("ConnectionPoolException expected");
    	} catch (ConnectionPoolException cpe) {
    		// empty
    	}
    	
    	// Make sure we can handle an uninitialized connection pool
    	_ctxt.release();
    	
    	// Intialize a connection pool
    	ConnectionPool pool = new ConnectionPool(1, "test");
    	SystemData.init();
    	SystemData.add(SystemData.JDBC_POOL, pool);
    	
    	try {
    		Connection c = _ctxt.getConnection();
    		assertNull(c);
    		fail("ConnectionPoolException expected");
    	} catch (ConnectionPoolException cpe) { 
    		// empty
    	}
    	
    	// Make sure we can handle an unconnected connection pool
    	_ctxt.release();
    }
    
    public void testAttributes() {
    	_ctxt.setAttribute("req_attr", "REQ", Command.REQUEST);
    	assertEquals("REQ", _ctxt.getRequest().getAttribute("req_attr"));
    	_ctxt.setAttribute("app_attr", "APP", Command.APPLICATION);
    	_ctxt.setAttribute("ses_attr", "SES", Command.SESSION);
    	assertEquals("SES", _ctxt.getSession().getAttribute("ses_attr"));
    }
    
    public void testRemoteUser() {
        Pilot p = new Pilot("John", "Smith");
        
        assertFalse(_ctxt.isAuthenticated());
        assertNotNull(_ctxt.getRoles());
        assertEquals(1, _ctxt.getRoles().size());
        assertTrue(_ctxt.getRoles().contains("Anonymous"));
		
        HttpSession s = _ctxt.getSession();
        s.setAttribute(HTTPContext.USER_ATTR_NAME, p);
        assertTrue(_ctxt.isAuthenticated());
        assertEquals(p, _ctxt.getUser());
        assertEquals(p.getRoles(), _ctxt.getRoles());
    }
    
    /* This really is a test just to convince me that finally blocks do get executed even if their preceeding catch block
     		throws another exception. */
    public void testExceptionHandling() {
        boolean finallyExecuted = false;
        boolean catchExecuted = false;
        try {
            try {
                assertNotNull(_ctxt);
                throw new Exception();
            } catch (Exception e) {
                catchExecuted = true;
                throw new RuntimeException();
            } finally {
                finallyExecuted = true;
            }
        } catch (RuntimeException re) {
            assertTrue(catchExecuted);
            assertTrue(finallyExecuted);
        }
    }
}
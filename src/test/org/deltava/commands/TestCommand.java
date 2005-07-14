package org.deltava.commands;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;
import org.hansel.CoverageDecorator;

import javax.servlet.ServletContext;

import com.kizna.servletunit.*;

public class TestCommand extends TestCase {
    
    private AbstractCommand _cmd;
    
    public static Test suite() {
        return new CoverageDecorator(TestCommand.class, new Class[] { AbstractCommand.class } );
    }    
    
    private class MockCommand extends AbstractCommand {
        
        MockCommand() {
            super();
        }
        
        public void execute(CommandContext ctxt) {
        }
        
        public ServletContext getContext() {
            return _ctx;
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        _cmd = new MockCommand();
    }
    
    protected void tearDown() throws Exception {
        _cmd = null;
        super.tearDown();
    }

    public void testName() throws Exception {
        assertNotNull(_cmd.getRoles());
        _cmd.init("mock", "MockCommand");
        assertEquals("mock", _cmd.getID());
        assertEquals("MockCommand", _cmd.getName());
        List roles = new ArrayList();
        _cmd.setRoles(roles);
        assertEquals(0, _cmd.getRoles().size());
        assertEquals(roles, _cmd.getRoles());
    }
    
    public void testServletContext() {
        ServletContext ctx = new ServletContextSimulator();
        _cmd.setContext(ctx);
        assertEquals(ctx, ((MockCommand) _cmd).getContext());
    }
    
    public void testErrorHandling() throws Exception {
        try {
            _cmd.init(null, null);
            fail("CommandException expected");
        } catch (CommandException ce) {
            assertNull(_cmd.getName());
        }
        
        _cmd.init("mock", "MockCommand");
        try {
            _cmd.init("mock", "MockCommand2");
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) {
            assertEquals("MockCommand", _cmd.getName());
        }
        
        _cmd.setRoles(new ArrayList());
        try {
            _cmd.setRoles(new LinkedList());
            fail("IllegalStateException expected");
        } catch (IllegalStateException ise) { }
    }
}
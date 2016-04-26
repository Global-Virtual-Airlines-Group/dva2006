package org.deltava.commands;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestCommandResult extends AbstractBeanTestCase {
    
    private CommandResult _cr;
    
    public static Test suite() {
        return new CoverageDecorator(TestCommandResult.class, new Class[] { CommandResult.class } );
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _cr = new CommandResult("URL");
        setBean(_cr);
    }
    
    @Override
	protected void tearDown() throws Exception {
        _cr = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("URL", _cr.getURL());
        checkProperty("backEndTime", new Long(123));
        checkProperty("time", Integer.valueOf(12354));
        checkProperty("URL", "URL");
        checkProperty("result", Integer.valueOf(2));
        checkProperty("httpCode", Integer.valueOf(302));
        checkProperty("success", Boolean.valueOf(true));
        _cr.complete();
        assertTrue(_cr.getTime() >= 0);
    }
    
    public void testURLParameters() {
    	_cr.setURL("command", null, "ID");
		assertEquals("/command.do?id=ID", _cr.getURL());
		_cr.setURL("command", "OP", "ID");
		assertEquals("/command.do?id=ID&op=OP", _cr.getURL());
		_cr.setURL("command", null, 33);
		assertEquals("/command.do?id=0x21", _cr.getURL());
    }
    
    public void testValidation() {
        validateInput("time", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("backEndTime", new Long(-1), IllegalArgumentException.class);
        validateInput("result", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("httpCode", Integer.valueOf(302), IllegalStateException.class);
    }
}
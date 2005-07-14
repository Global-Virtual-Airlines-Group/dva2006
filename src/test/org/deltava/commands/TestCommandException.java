package org.deltava.commands;

import junit.framework.Test;
import junit.framework.TestCase;
import org.hansel.CoverageDecorator;

public class TestCommandException extends TestCase {

    private CommandException _ce;

    public static Test suite() {
        return new CoverageDecorator(TestCommandException.class, new Class[] { CommandException.class });
    }

    public void testMessage() {
        _ce = new CommandException("MSG");
        assertEquals("MSG", _ce.getMessage());
    }

    public void testCause() {
        Exception e = new NullPointerException();
        _ce = new CommandException("MSG", e);
        assertEquals("MSG - " + e.getClass().getName(), _ce.getMessage());
        assertEquals(e, _ce.getCause());
    }

    public void testCauseNoMessage() {
        Exception e = new NullPointerException();
        _ce = new CommandException(e);
        assertEquals(e.getMessage() + " - " + e.getClass().getName(), _ce.getMessage());
        assertEquals(e, _ce.getCause());
    }
}
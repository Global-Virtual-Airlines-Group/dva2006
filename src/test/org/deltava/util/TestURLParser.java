package org.deltava.util;

import org.hansel.CoverageDecorator;

import junit.framework.Test;
import junit.framework.TestCase;

public class TestURLParser extends TestCase {

    private URLParser _parser;

    @Override
	protected void tearDown() throws Exception {
        _parser = null;
        super.tearDown();
    }
    
    public static Test suite() {
        return new CoverageDecorator(TestURLParser.class, new Class[] { URLParser.class });
    }

    public void testParsing() {
        _parser = new URLParser("/path1/path2/name.EXT");
        assertEquals("EXT", _parser.getExtension());
        assertEquals("name", _parser.getName());
        assertEquals("name.EXT", _parser.getFileName());
        assertEquals("path2", _parser.getLastPath());
        assertTrue(_parser.containsPath("path1"));
        assertTrue(_parser.containsPath("path2"));
        assertTrue(_parser.containsPath("name"));
        assertFalse(_parser.containsPath("path3"));
        assertFalse(_parser.containsPath("ext"));
        assertEquals(3, _parser.size());
    }

    public void testNoExtension() {
        _parser = new URLParser("/path1/path2/name2");
        assertEquals("", _parser.getExtension());
        assertEquals("name2", _parser.getName());
        assertEquals("path2", _parser.getLastPath());
        assertEquals(3, _parser.size());
    }

    public void testNoPathWithSlash() {
        _parser = new URLParser("/name2.ext");
        assertEquals("ext", _parser.getExtension());
        assertEquals("name2", _parser.getName());
        assertEquals("", _parser.getLastPath());
        assertEquals(1, _parser.size());
    }
    
    public void testNoPath() {
        _parser = new URLParser("name2.ext");
        assertEquals("ext", _parser.getExtension());
        assertEquals("name2", _parser.getName());
        assertEquals("", _parser.getLastPath());
        assertEquals(1, _parser.size());
    }
}
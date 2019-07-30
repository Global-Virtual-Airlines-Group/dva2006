package org.deltava.util;

import java.util.*;

import junit.framework.Test;
import junit.framework.TestCase;
import org.hansel.CoverageDecorator;

@SuppressWarnings("static-method")
public class TestStringUtils extends TestCase {

    public static Test suite() {
        return new CoverageDecorator(TestStringUtils.class, new Class[] { StringUtils.class } );
    }
    
    public void testProperCase() {
        assertEquals("Test", StringUtils.properCase("Test"));
        assertEquals("Test", StringUtils.properCase("TEST"));
        assertEquals("-Test", StringUtils.properCase("-Test"));
        assertEquals("T", StringUtils.properCase("t"));
        assertEquals("Jesse David Hollington", StringUtils.properCase("jesse david hollington"));
        assertEquals("Marc-Andre Fleury", StringUtils.properCase("Marc-andre fleury"));
        try {
            assertNull(StringUtils.properCase(null));
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
        	// empty
        }
    }
    
    public void testStripInllineHTML() {
        assertEquals("&lt;IMG&gt;", StringUtils.stripInlineHTML("<IMG>"));
        assertEquals("&amp; &quot; &#039; &#092;", StringUtils.stripInlineHTML("& \" \' \\"));
        assertEquals("XX<br />\n", StringUtils.stripInlineHTML("XX\n"));
        assertEquals(null, StringUtils.stripInlineHTML(null));
    }
    
    public void testListConcat() {
    	ArrayList<String> l = new ArrayList<String>();
    	l.add("A");
    	l.add("B");
    	l.add("CD");
    	assertEquals("A,B,CD", StringUtils.listConcat(l, ","));
    }
    
    public void testIntParse() {
    	assertEquals(0, StringUtils.parse("0", 1));
    	assertEquals(-2, StringUtils.parse("-2", 0));
    	assertEquals(255, StringUtils.parse("0xFF", 0));
    	assertEquals(254, StringUtils.parse("0xfE", 0));
    	assertEquals(0, StringUtils.parse("FF", 0));
    	assertEquals(-1, StringUtils.parse(null, -1));
    }
    
    public void testLongParse() {
    	assertEquals(0, StringUtils.parse("0", 1, false));
    	assertEquals(-2, StringUtils.parse("-2", 0, false));
    	assertEquals(1, StringUtils.parse("-2", 1, true));
    	assertEquals(0, StringUtils.parse("FF", 0, false));
    	assertEquals(0, StringUtils.parse("0xFFFF", 0, false));
    	assertEquals(-1, StringUtils.parse(null, -1, false));
    }
    
    public void testPropertyName() {
    	assertEquals("getProperty", StringUtils.getPropertyMethod("property"));
    	assertEquals("getProperty", StringUtils.getPropertyMethod("Property"));
    }
    
    public void testHex() {
       assertEquals(32, StringUtils.parseHex("0x20"));
       assertEquals(20, StringUtils.parseHex("20"));
       assertEquals("0x20", StringUtils.formatHex(32));
    }
    
    public void testFilter() {
    	assertEquals("foo", StringUtils.filter("foo", Character::isAlphabetic));
    	assertEquals("", StringUtils.filter("123", Character::isAlphabetic));
    	assertEquals("123", StringUtils.filter("123f", Character::isDigit));
    }
    
    public void testIsEmpty() {
       assertFalse(StringUtils.isEmpty("ASDSAD"));
       assertTrue(StringUtils.isEmpty(""));
       assertTrue(StringUtils.isEmpty(null));
    }
    
    public void testSplit() {
       List<String> values = StringUtils.split("1,B,C,D", ",");
       assertEquals(4, values.size());
       assertEquals("1", values.get(0));
       assertEquals("B", values.get(1));
       assertEquals("C", values.get(2));
       assertEquals("D", values.get(3));
       
       values = StringUtils.split("2,", ",");
       assertEquals(1, values.size());
       assertEquals("2", values.get(0));
       
       values = StringUtils.split("LFPO  D045O  D083Q  LFLC", " ");
       assertEquals(7, values.size());
       List<String> v2 = StringUtils.nullTrim(values);
       assertEquals(4, v2.size());
    }
    
    public void testArrayIndexOf() {
       final String[] testArray = {"1", "B", "C"};
       assertEquals(0, StringUtils.arrayIndexOf(testArray, "1"));
       assertEquals(1, StringUtils.arrayIndexOf(testArray, "B"));
       assertEquals(-1, StringUtils.arrayIndexOf(testArray, "X"));
       assertEquals(-1, StringUtils.arrayIndexOf(testArray, null));
       assertEquals(-1, StringUtils.arrayIndexOf(null, "1"));
    }
    
    public void testFormat() {
       assertEquals("01234", StringUtils.format(1234, "00000"));
    }
    
    public void testStripComma() {
       assertEquals("Normal String", StringUtils.strip("Normal String", ","));
       assertEquals("Normal String", StringUtils.strip(",Normal,,, String,,", ","));
       assertNull(StringUtils.strip(null, "XXX"));
       assertEquals("Normal String", StringUtils.strip("Normal String", null));
    }
}
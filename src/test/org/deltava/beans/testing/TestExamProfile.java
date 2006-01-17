package org.deltava.beans.testing;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestExamProfile extends AbstractBeanTestCase {

    private ExamProfile _exam;
    
    public static Test suite() {
        return new CoverageDecorator(TestExamProfile.class, new Class[] { ExamProfile.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _exam = new ExamProfile("737 Captain");
        setBean(_exam);
    }

    protected void tearDown() throws Exception {
        _exam = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("737 Captain", _exam.getName());
        assertEquals(_exam.getName(), _exam.toString());
        checkProperty("size", new Integer(3));
        checkProperty("stage", new Integer(3));
        checkProperty("equipmentType", "B737-800");
        checkProperty("minStage", new Integer(2));
        checkProperty("time", new Integer(60));
        checkProperty("passScore", new Integer(75));
        assertFalse(_exam.getActive());
        assertEquals("warn", _exam.getRowClassName());
        _exam.setActive(true);
        assertTrue(_exam.getActive());
        assertNull(_exam.getRowClassName());
    }
    
    public void testCacheable() {
        assertEquals(_exam.getName(), _exam.cacheKey());
        assertEquals(_exam.getName().hashCode(), _exam.hashCode());
    }
    
    public void testValidation() {
        validateInput("size", new Integer(0), IllegalArgumentException.class);
        validateInput("stage", new Integer(0), IllegalArgumentException.class);
        validateInput("minStage", new Integer(-1), IllegalArgumentException.class);
        validateInput("time", new Integer(0), IllegalArgumentException.class);
        validateInput("passScore", new Integer(-1), IllegalArgumentException.class);
        validateInput("passScore", new Integer(101), IllegalArgumentException.class);
    }
    
    public void testComparator() {
        _exam.setStage(2);
        _exam.setMinStage(1);
        ExamProfile e2 = new ExamProfile("MD-88 Captain");
        e2.setStage(2);
        e2.setMinStage(2);
        ExamProfile e3 = new ExamProfile("767 Captain");
        e3.setStage(3);
        assertTrue(_exam.compareTo(e2) < 0);
        assertTrue(e2.compareTo(_exam) > 0);
        e2.setMinStage(1);
        assertTrue(_exam.compareTo(e2) < 0);
        assertTrue(_exam.compareTo(e3) < 0);
    }
    
    public void testEquals() {
        ExamProfile e2 = new ExamProfile("MD-88 Captain");
        ExamProfile e3 = new ExamProfile("737 Captain");
        assertFalse(_exam.equals(e2));
        assertTrue(_exam.equals(e3));
        assertFalse(_exam.equals(new Object()));
    }
}
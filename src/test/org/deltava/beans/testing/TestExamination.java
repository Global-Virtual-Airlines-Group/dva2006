package org.deltava.beans.testing;

import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestExamination extends AbstractBeanTestCase {

    private Examination _exam;
    
    public static Test suite() {
        return new CoverageDecorator(TestExamination.class, new Class[] { Examination.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _exam = new Examination("737 First Officer");
        setBean(_exam);
    }
    
    protected void tearDown() throws Exception {
        _exam = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("737 First Officer", _exam.getName());
        assertEquals(org.deltava.beans.testing.Test.EXAM, _exam.getType());
        checkProperty("ID", new Integer(123));
        checkProperty("pilotID", new Integer(123));
        checkProperty("scorerID", new Integer(123));
        checkProperty("firstName", "John");
        checkProperty("lastName", "Smith");
        checkProperty("size", new Integer(20));
        checkProperty("score", new Integer(0));
        checkProperty("stage", new Integer(3));
        checkProperty("date", new Date());
        checkProperty("expiryDate", new Date());
        checkProperty("submittedOn", new Date());
        checkProperty("scoredOn", new Date());
        checkProperty("passFail", new Boolean (true));
    }
    
    public void testQuestions() {
        assertNotNull(_exam.getQuestions());
        assertEquals(0, _exam.getQuestions().size());
        
        Question q1 = new Question("Why?");
        Question q2 = new Question("Why Not?");
        Question q4 = new Question("Out of Sequence");
        q4.setNumber(4);
        _exam.addQuestion(q1);
        _exam.addQuestion(q2);
        _exam.addQuestion(q4);
        
        List<Question> qs = new ArrayList<Question>(_exam.getQuestions());
        assertEquals(3, qs.size());
        assertEquals(qs.size(), _exam.getSize());
        
        assertSame(q1, qs.get(0));
        assertSame(q1, _exam.getQuestion(1));
        assertSame(q2, qs.get(1));
        assertSame(q2, _exam.getQuestion(2));
        assertSame(q4, qs.get(2));
        assertSame(q4, _exam.getQuestion(4));
        assertEquals(1, q1.getNumber());
        assertEquals(2, q2.getNumber());
        assertEquals(4, q4.getNumber());
    }
    
    public void testValidation() {
        validateInput("ID", new Integer(0), IllegalArgumentException.class);
        validateInput("ID", new Integer(-1), IllegalArgumentException.class);
        validateInput("pilotID", new Integer(0), IllegalArgumentException.class);
        validateInput("pilotID", new Integer(-1), IllegalArgumentException.class);
        validateInput("scorerID", new Integer(-1), IllegalArgumentException.class);
        validateInput("score", new Integer(-1), IllegalArgumentException.class);
        validateInput("score", new Integer(101), IllegalArgumentException.class);
        validateInput("stage", new Integer(-1), IllegalArgumentException.class);
        validateInput("size", new Integer(0), IllegalArgumentException.class);
        try {
            Examination e2 = new Examination(null);
            fail("NullPointerException expected");
            assertNull(e2);
        } catch (NullPointerException npe) {
        	// empty
        }
        
        _exam.addQuestion(new Question("Why?"));
        assertEquals(1, _exam.getSize());
        validateInput("size", new Integer(2), IllegalStateException.class);
    }
    
    public void testComparator() {
        long now = System.currentTimeMillis();
        _exam.setDate(new Date(now));
        
        Examination e2 = new Examination("737 Captain");
        e2.setDate(new Date(now - 864000));
        
        assertTrue(_exam.compareTo(e2) > 0);
        assertTrue(e2.compareTo(_exam) < 0);
    }
}
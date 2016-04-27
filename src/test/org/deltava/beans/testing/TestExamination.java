package org.deltava.beans.testing;

import java.time.Instant;
import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestExamination extends AbstractBeanTestCase {

    private Examination _exam;
    
    public static Test suite() {
        return new CoverageDecorator(TestExamination.class, new Class[] { Examination.class } );
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _exam = new Examination("737 First Officer");
        setBean(_exam);
    }
    
    @Override
	protected void tearDown() throws Exception {
        _exam = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("737 First Officer", _exam.getName());
        checkProperty("ID", Integer.valueOf(123));
        checkProperty("pilotID", Integer.valueOf(123));
        checkProperty("scorerID", Integer.valueOf(123));
        checkProperty("firstName", "John");
        checkProperty("lastName", "Smith");
        checkProperty("size", Integer.valueOf(20));
        checkProperty("score", Integer.valueOf(0));
        checkProperty("stage", Integer.valueOf(3));
        checkProperty("date", Instant.now());
        checkProperty("expiryDate", Instant.now());
        checkProperty("submittedOn", Instant.now());
        checkProperty("scoredOn", Instant.now());
        checkProperty("passFail", Boolean.TRUE);
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
        validateInput("ID", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("ID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("pilotID", Integer.valueOf(0), IllegalArgumentException.class);
        validateInput("pilotID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("scorerID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("score", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("score", Integer.valueOf(101), IllegalArgumentException.class);
        validateInput("stage", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("size", Integer.valueOf(0), IllegalArgumentException.class);
        try {
            Examination e2 = new Examination(null);
            assertNull(e2);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
        	// empty
        }
        
        _exam.addQuestion(new Question("Why?"));
        assertEquals(1, _exam.getSize());
        validateInput("size", Integer.valueOf(2), IllegalStateException.class);
    }
    
    public void testComparator() {
        _exam.setDate(Instant.now());
        
        Examination e2 = new Examination("737 Captain");
        e2.setDate(_exam.getDate().minusSeconds(86400));
        
        assertTrue(_exam.compareTo(e2) > 0);
        assertTrue(e2.compareTo(_exam) < 0);
    }
}
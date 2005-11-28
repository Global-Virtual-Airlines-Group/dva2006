package org.deltava.beans.testing;

import java.util.*;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestQuestionProfile extends AbstractBeanTestCase {

    private QuestionProfile _q;
    
    public static Test suite() {
        return new CoverageDecorator(TestQuestionProfile.class, new Class[] { QuestionProfile.class } );
    }

    protected void setUp() throws Exception {
        super.setUp();
        _q = new QuestionProfile("Why?");
        setBean(_q);
    }

    protected void tearDown() throws Exception {
        _q = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("Why?", _q.getQuestion());
        checkProperty("correctAnswer", "Because!");
        checkProperty("answer", "Because!");
        checkProperty("question", "Why Not?");
        checkProperty("totalAnswers", new Integer(25));
        checkProperty("correctAnswers", new Integer(23));
        
        assertFalse(_q.getActive());
        assertEquals("warn", _q.getRowClassName());
        _q.setActive(true);
        assertTrue(_q.getActive());
        assertNull(_q.getRowClassName());
    }
    
    public void testExams() {
        assertNotNull(_q.getExamNames());
        assertEquals(0, _q.getExamNames().size());
        
        _q.addExam("737 First Officer");
        _q.addExam("737 Captain");
        _q.addExam("737 First Officer");
        _q.addExam("CRJ First Officer");
        
        List<String> exams = new ArrayList<String>(_q.getExamNames());
        assertEquals(3, exams.size());
        assertTrue(_q.getExamNames().contains("737 First Officer"));
        assertTrue(_q.getExamNames().contains("737 Captain"));
        assertTrue(_q.getExamNames().contains("CRJ First Officer"));
        
        exams.add("757 First Officer");
        _q.setExams(exams);
        assertEquals(4, exams.size());
        assertTrue(_q.getExamNames().contains("757 First Officer"));
    }
    
    public void testValidation() {
       validateInput("totalAnswers", new Integer(-1), IllegalArgumentException.class);
       validateInput("correctAnswers", new Integer(-1), IllegalArgumentException.class);
        validateInput("number", new Integer(0), UnsupportedOperationException.class);
        try {
            QuestionProfile qp2 = new QuestionProfile(null);
            fail("NullPointerException expected");
            assertNotNull(qp2);
        } catch (NullPointerException npe) { }
    }
    
    public void testComparator() {
        QuestionProfile qp2 = new QuestionProfile("Why not?");
        assertTrue(_q.compareTo(qp2) > 0);
        assertTrue(qp2.compareTo(_q) < 0);
    }
}


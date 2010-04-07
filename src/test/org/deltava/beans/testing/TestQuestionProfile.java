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
        checkProperty("totalAnswers", Integer.valueOf(25));
        checkProperty("correctAnswers", Integer.valueOf(23));
        
        assertFalse(_q.getActive());
        assertEquals("warn", _q.getRowClassName());
        _q.setActive(true);
        assertTrue(_q.getActive());
        assertNull(_q.getRowClassName());
    }
    
    public void testExams() {
        assertNotNull(_q.getPools());
        assertEquals(0, _q.getPools().size());
        
        _q.addPool(new ExamSubPool("737 First Officer", "ALL"));
        _q.addPool(new ExamSubPool("737 Captain", "ALL"));
        _q.addPool(new ExamSubPool("737 First Officer", "ALL"));
        _q.addPool(new ExamSubPool("CRJ First Officer", "ALL"));
        
        List<ExamSubPool> exams = new ArrayList<ExamSubPool>(_q.getPools());
        assertEquals(3, exams.size());
        assertTrue(_q.getPools().contains("737 First Officer"));
        assertTrue(_q.getPools().contains("737 Captain"));
        assertTrue(_q.getPools().contains("CRJ First Officer"));
        
        exams.add(new ExamSubPool("757 First Officer", "ALL"));
        _q.setPools(exams);
        assertEquals(4, exams.size());
        assertTrue(_q.getPools().contains(exams.get(3)));
    }
    
    public void testValidation() {
       validateInput("totalAnswers", Integer.valueOf(-1), IllegalArgumentException.class);
       validateInput("correctAnswers", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("number", Integer.valueOf(0), UnsupportedOperationException.class);
        try {
            QuestionProfile qp2 = new QuestionProfile(null);
            assertNotNull(qp2);
            fail("NullPointerException expected");
        } catch (NullPointerException npe) {
        	// empty
        }
    }
    
    public void testComparator() {
        QuestionProfile qp2 = new QuestionProfile("Why not?");
        assertTrue(_q.compareTo(qp2) > 0);
        assertTrue(qp2.compareTo(_q) < 0);
    }
}
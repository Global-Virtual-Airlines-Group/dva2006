package org.deltava.beans.testing;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestQuestion extends AbstractBeanTestCase {

    private Question _q;
    
    public static Test suite() {
        return new CoverageDecorator(TestQuestion.class, new Class[] { Question.class } );
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        _q = new Question("Why?");
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
        checkProperty("number", new Integer(1));
        assertFalse(_q.isCorrect());
        _q.setCorrect(true);
        assertTrue(_q.isCorrect());
   }
    
    public void testQPConstructor() {
       QuestionProfile qp = new QuestionProfile("Why?");
       qp.setID(2345);
       qp.setCorrectAnswer("Why Not?");
       
       Question q2 = qp.toQuestion();
       assertEquals(qp.getQuestion(), q2.getQuestion());
       assertEquals(qp.getID(), q2.getID());
       assertEquals(qp.getCorrectAnswer(), q2.getCorrectAnswer());
    }
    
    public void testComparator() {
        _q.setNumber(1);
        Question q2 = new Question("Why not?");
        q2.setNumber(2);
        assertTrue(_q.compareTo(q2) < 0);
        assertTrue(q2.compareTo(_q) > 0);
    }
    
    public void testValidation() {
        validateInput("number", new Integer(0), IllegalArgumentException.class);
        try {
        	String s = null;
            Question q2 = new Question(s);
            fail("NullPointerException expected");
            assertNotNull(q2);
        } catch (NullPointerException npe) {
        	// empty
        }
    }
}
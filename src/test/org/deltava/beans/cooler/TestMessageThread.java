package org.deltava.beans.cooler;

import java.util.Date;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;

public class TestMessageThread extends AbstractBeanTestCase {

    private MessageThread _t;
    
    public static Test suite() {
        return new CoverageDecorator(TestMessageThread.class, new Class[] { MessageThread.class } );
    }

    protected void setUp() throws Exception {
        super.setUp();
        _t = new MessageThread("Subject");
        setBean(_t);
    }

    protected void tearDown() throws Exception {
        _t = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("Subject", _t.getSubject());
        checkProperty("channel", "ChannelName");
        checkProperty("lastUpdatedOn", new Date());
        checkProperty("stickyUntil", new Date(System.currentTimeMillis() + 50));
        checkProperty("ID", new Integer(1212));
        checkProperty("authorID", new Integer(1213));
        checkProperty("lastUpdateID", new Integer(1215));
        checkProperty("views", new Integer(12));
        checkProperty("postCount", new Integer(12));
        checkProperty("image", new Integer(234));
        checkProperty("locked", Boolean.valueOf(true));
        checkProperty("hidden", Boolean.valueOf(true));
        checkProperty("stickyInChannelOnly", Boolean.valueOf(true));
        
        // Reset the image ID to 0
        assertTrue(_t.getImage() != 0);
        _t.setImage(0);
        assertEquals(0, _t.getImage());
    }
    
    public void testSticky() {
       assertNull(_t.getStickyUntil());
       assertFalse(_t.getStickyInChannelOnly());
       _t.setStickyInChannelOnly(true);
       assertFalse(_t.getStickyInChannelOnly());
       _t.setStickyUntil(new Date(System.currentTimeMillis() + 50));
       assertTrue(_t.getStickyInChannelOnly());
       _t.setStickyUntil(new Date());
       assertNull(_t.getStickyUntil());
    }
    
    public void testValidation() {
        validateInput("views", new Integer(-1), IllegalArgumentException.class);
        validateInput("postCount", new Integer(-1), IllegalArgumentException.class);
        validateInput("image", new Integer(-1), IllegalArgumentException.class);
        
        _t.addPost(new Message(123));
        validateInput("postCount", new Integer(1), IllegalStateException.class);
        
        _t.setLastUpdatedOn(null);
        _t.setStickyUntil(null);
    }
    
    public void testPosts() {
        long now = System.currentTimeMillis();
        assertNotNull(_t.getPosts());
        assertEquals(0, _t.getPostCount());
        assertEquals(_t.getPostCount(), _t.getPosts().size());
        assertNull(_t.getLastUpdatedOn());
        
        Message msg1 = new Message(123);
        msg1.setCreatedOn(new Date(now));
        _t.addPost(msg1);
        assertEquals(1, _t.getPostCount());
        assertEquals(_t.getPostCount(), _t.getPosts().size());
        assertSame(msg1, _t.getPosts().get(0));
        assertEquals(msg1.getCreatedOn(), _t.getLastUpdatedOn());
        
        Message msg2 = new Message(123);
        msg2.setCreatedOn(new Date(now + 3));
        _t.addPost(msg2);
        assertEquals(2, _t.getPostCount());
        assertEquals(_t.getPostCount(), _t.getPosts().size());
        assertEquals(msg2.getCreatedOn(), _t.getLastUpdatedOn());
        
        assertSame(msg1, _t.getPosts().get(0));
        assertSame(msg2, _t.getPosts().get(1));
        
        Message msg3 = new Message(123);
        msg3.setCreatedOn(new Date(now + 2));
        _t.addPost(msg3);
        assertEquals(3, _t.getPostCount());
        assertEquals(_t.getPostCount(), _t.getPosts().size());
        assertEquals(msg2.getCreatedOn(), _t.getLastUpdatedOn()); // won't change
        
        Message msg4 = new Message(123);
        msg4.setCreatedOn(new Date(now - 2));
        _t.addPost(msg4);
        assertEquals(4, _t.getPostCount());
        assertEquals(_t.getPostCount(), _t.getPosts().size());
        assertEquals(msg2.getCreatedOn(), _t.getLastUpdatedOn()); // won't change

        assertSame(msg4, _t.getPosts().get(0));
        assertSame(msg1, _t.getPosts().get(1));
        assertSame(msg3, _t.getPosts().get(2));
        assertSame(msg2, _t.getPosts().get(3));
    }
    
    public void testComparator() {
        long now = System.currentTimeMillis();
        _t.setLastUpdatedOn(new Date(now));
        
        MessageThread t2 = new MessageThread("AAA Recent subject");
        t2.setLastUpdatedOn(new Date(now + 3));
        
        assertTrue(_t.compareTo(t2) < 0);
        assertTrue(t2.compareTo(_t) > 0);
    }
    
    public void testViewEntry() {
       assertFalse(_t.getHidden());
       assertNull(_t.getRowClassName());
       _t.setHidden(true);
       assertEquals("warn", _t.getRowClassName());
    }
}
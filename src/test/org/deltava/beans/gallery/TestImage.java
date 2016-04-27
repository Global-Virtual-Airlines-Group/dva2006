package org.deltava.beans.gallery;

import java.io.*;
import java.time.Instant;

import junit.framework.Test;
import org.hansel.CoverageDecorator;

import org.deltava.beans.AbstractBeanTestCase;
import org.deltava.beans.Pilot;
import org.deltava.util.ImageInfo;

public class TestImage extends AbstractBeanTestCase {

    private Image _img;

    public static Test suite() {
        return new CoverageDecorator(TestImage.class, new Class[] { Image.class });
    }

    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _img = new Image("testImage", "description");
        setBean(_img);
    }

    @Override
	protected void tearDown() throws Exception {
        _img = null;
        super.tearDown();
    }

    public void testProperties() {
        assertEquals("testImage", _img.getName());
        assertEquals("description", _img.getDescription());
        checkProperty("name", "NewName");
        checkProperty("description", "NewDesc");
        checkProperty("size", Integer.valueOf(131246));
        checkProperty("width", Integer.valueOf(1280));
        checkProperty("height", Integer.valueOf(1024));
        checkProperty("type", Integer.valueOf(1));
        checkProperty("ID", Integer.valueOf(116));
        checkProperty("authorID", Integer.valueOf(117));
        checkProperty("createdOn", Instant.now());
        checkProperty("fleet", Boolean.valueOf(true));
        checkProperty("score", new Double(3.1));
        checkProperty("voteCount", Integer.valueOf(16));
        assertFalse(_img.hasLiked(null));
    }

    public void testComboAlias() {
        assertEquals(_img.getName(), _img.getComboName());
        assertEquals(Integer.toHexString(_img.getID()), _img.getComboAlias());
    }
    
    public void testValidation() throws IOException {
        // Test parameter checking
        validateInput("size", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("width", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("height", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("ID", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("type", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("type", Integer.valueOf(21), IllegalArgumentException.class);
        validateInput("voteCount", Integer.valueOf(-1), IllegalArgumentException.class);
        validateInput("score", new Double(-1), IllegalArgumentException.class);
        validateInput("score", new Double(10.1), IllegalArgumentException.class);

        // Set properties for state checking
        _img.setSize(10235);
        _img.setType(1);

        // Test state checking
        validateInput("size", Integer.valueOf(1235), IllegalStateException.class);
        validateInput("type", Integer.valueOf(0), IllegalStateException.class);

        // See what happens if we get the stream when the buffer is empty
        InputStream is = _img.getInputStream();
        assertNotNull(is);
        assertEquals(0, is.available());
        
        // Test exceptions if votes have been populated
        _img.addLike(1);
        _img.addLike(2);
        validateInput("voteCount", Integer.valueOf(123), IllegalStateException.class);
        validateInput("score", new Double(1.2), IllegalStateException.class);
    }

    public void testGIF() throws IOException {
        assertEquals("testImage", _img.getName());
        File f = new File("data/testImage.gif");
        assertTrue(f.exists());
        InputStream is = new FileInputStream(f);
        _img.load(is);
        is.close();

        // Validate the image data is correct
        assertEquals(ImageInfo.FORMAT_GIF, _img.getType());
        assertEquals(f.length(), _img.getSize());
        assertEquals(320, _img.getWidth());
        assertEquals(160, _img.getHeight());

        InputStream imgS = _img.getInputStream();
        assertNotNull(imgS);
        assertTrue(imgS.available() == _img.getSize());
    }

    public void testJPEG() throws IOException {
        assertEquals("testImage", _img.getName());
        File f = new File("data/testImage.jpg");
        assertTrue(f.exists());
        InputStream is = new FileInputStream(f);
        _img.load(is);
        is.close();

        // Validate the image data is correct
        assertEquals(ImageInfo.FORMAT_JPEG, _img.getType());
        assertEquals(f.length(), _img.getSize());
        assertEquals(320, _img.getWidth());
        assertEquals(160, _img.getHeight());

        InputStream imgS = _img.getInputStream();
        assertNotNull(imgS);
        assertEquals(_img.getSize(), imgS.available());
    }
    
    public void testPNG() throws IOException {
       assertEquals("testImage", _img.getName());
       File f = new File("data/testImage.png");
       assertTrue(f.exists());
       InputStream is = new FileInputStream(f);
       _img.load(is);
       is.close();

       // Validate the image data is correct
       assertEquals(ImageInfo.FORMAT_PNG, _img.getType());
       assertEquals(f.length(), _img.getSize());
       assertEquals(320, _img.getWidth());
       assertEquals(160, _img.getHeight());

       InputStream imgS = _img.getInputStream();
       assertNotNull(imgS);
       assertEquals(_img.getSize(), imgS.available());
    }

    public void testInvalidFile() {
        assertEquals("testImage", _img.getName());
        File f = new File("data/users.txt");
        assertTrue(f.exists());
        try {
            InputStream is = new FileInputStream(f);
            _img.load(is);
            is.close();
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException upe) {
        	// empty
        } catch (IOException ie) {
            fail("I/O Error");
        }
    }

    public void testVotes() {
        assertEquals("testImage", _img.getName());
        _img.setID(123);
        assertNotNull(_img.getLikes());
        assertEquals(0, _img.getLikes().size());

        Pilot p = new Pilot("John", "Smith");
        p.setID(234);
        
        assertFalse(_img.hasLiked(p));

        _img.addLike(p.getID());
        assertEquals(1, _img.getLikes().size());
        assertEquals(1, _img.getLikeCount());
        assertTrue(_img.hasLiked(p));
    }
}
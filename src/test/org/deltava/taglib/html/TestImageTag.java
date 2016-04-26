package org.deltava.taglib.html;

import org.deltava.taglib.AbstractTagTestCase;

public class TestImageTag extends AbstractTagTestCase {

    private ImageTag _tag;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _tag = new ImageTag();
        _tag.setPageContext(_ctx);
    }

    @Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }

    public void testImage() throws Exception {
        _tag.setSrc("image.gif");
        _tag.setX(400);
        _tag.setY(300);
        _tag.setCaption("Caption");
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("img", getElementName());
        assertEquals(5, getAttrCount());
        
        assertAttr("400", "width");
        assertAttr("300", "height");
        assertAttr("/imgs/image.gif", "src");
        assertAttr("Caption", "alt");
        
        _jspOut.clearBuffer();
        _tag.setCaption("Caption2");
        _tag.setSrc("image2.jpg");
        
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        
        parseOutput();
        assertEquals("img", getElementName());
        assertEquals(2, getAttrCount());

        assertAttr("/imgs/image2.jpg", "src");
        assertAttr("Caption2", "alt");
    }
}
package org.deltava.taglib.format;

import javax.servlet.http.*;

import org.deltava.beans.Pilot;
import org.deltava.commands.HTTPContext;
import org.deltava.servlet.filter.CustomRequestWrapper;

import org.deltava.taglib.AbstractTagTestCase;

public class TestDecimalFormatTag extends AbstractTagTestCase {

    private DecimalFormatTag _tag;
    
    protected void setUp() throws Exception {
        super.setUp();
        _tag = new DecimalFormatTag();
    }

    protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }
    
    public void testPersonProperties() throws Exception {
        Pilot p = new Pilot("John", "Smith");
        p.setNumberFormat("##00.0");
        HttpServletRequest hreq = _req;
        HttpSession s = hreq.getSession(true);
        s.setAttribute(HTTPContext.USER_ATTR_NAME, p);
        _ctx.initialize(null, new CustomRequestWrapper(hreq), _rsp, "", false, 8192, false);
        
        _tag.setPageContext(_ctx);
        _tag.setValue(new Integer(1));
        assertEvalPage(_tag.doEndTag());
        assertEquals("01.0", _jspOut.toString());
    }
    
    public void testTagProperties() throws Exception {
        _tag.setPageContext(_ctx);
        _tag.setFmt("#00.0");
        _tag.setValue(new Long(2));
        assertEvalPage(_tag.doEndTag());
        assertEquals("02.0", _jspOut.toString());
    }
}
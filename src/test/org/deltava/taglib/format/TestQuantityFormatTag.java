package org.deltava.taglib.format;

import javax.servlet.http.*;

import org.deltava.beans.Pilot;
import org.deltava.servlet.filter.CustomRequestWrapper;

import org.deltava.taglib.AbstractTagTestCase;

public class TestQuantityFormatTag extends AbstractTagTestCase {

	private QuantityFormatTag _tag;

    protected void setUp() throws Exception {
        super.setUp();
        _tag = new QuantityFormatTag();
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
        setUser(p);
        _ctx.initialize(null, new CustomRequestWrapper(hreq), _rsp, "", false, 8192, false);
        
        _tag.setPageContext(_ctx);
        _tag.setValue(new Integer(1));
        _tag.setSingle("widget");
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("01 widget", _jspOut.toString());
    }
    
    public void testTagProperties() throws Exception {
        _tag.setPageContext(_ctx);
        _tag.setFmt("#00.0");
        _tag.setValue(new Long(2));
        _tag.setSingle("bauble");
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("02 baubles", _jspOut.toString());
    }
    
    public void testPlural() throws Exception {
        _tag.setPageContext(_ctx);
        _tag.setFmt("#0");
        _tag.setValue(new Long(2));
        _tag.setSingle("bauble");
        _tag.setPlural("baublez");
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("2 baublez", _jspOut.toString());
    }
    
    public void testZero() throws Exception {
        _tag.setPageContext(_ctx);
        _tag.setFmt("#0");
        _tag.setValue(new Long(0));
        _tag.setSingle("bauble");
        _tag.setPlural("baublez");
        _tag.setZero("baubles");
        assertSkipBody(_tag.doStartTag());
        assertEvalPage(_tag.doEndTag());
        assertEquals("0 baubles", _jspOut.toString());
    }
}
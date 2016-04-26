package org.deltava.taglib.format;

import java.time.Instant;

import javax.servlet.http.*;

import org.deltava.beans.Pilot;
import org.deltava.beans.TZInfo;

import org.deltava.servlet.filter.CustomRequestWrapper;

import org.deltava.taglib.AbstractTagTestCase;
import org.deltava.util.StringUtils;

public class TestDateFormatTag extends AbstractTagTestCase {

    private DateFormatTag _tag;
    private Instant _d;
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        _d = StringUtils.parseInstant("12/23/1972 09:38:25", "MM/dd/yyyy hh:mm:ss");
        _tag = new DateFormatTag();
    }

    @Override
	protected void tearDown() throws Exception {
        _tag.release();
        _tag = null;
        super.tearDown();
    }

    public void testPersonProperties() throws Exception {
        Pilot p = new Pilot("John", "Smith");
        p.setDateFormat("yy MM dd");
        p.setTimeFormat("hh:mm");
        p.setTZ(TZInfo.init("US/Eastern", null, null));
        HttpServletRequest hreq = _req;
        setUser(p);
        _ctx.initialize(null, new CustomRequestWrapper(hreq), _rsp, "", false, 8192, false);

        _tag.setPageContext(_ctx);
        _tag.setDate(_d);
        assertEvalPage(_tag.doEndTag());
        assertEquals("72 12 23 09:38", _jspOut.toString());
    }
    
    public void testTagProperties() throws Exception {
        _tag.setPageContext(_ctx);
        _tag.setDate(_d);
        _tag.setD("yyyy-MM-dd");
        _tag.setT("hh mm ss");
        assertEvalPage(_tag.doEndTag());
        assertEquals("1972-12-23 09 38 25", _jspOut.toString());
    }
    
    public void testTimeOnly() throws Exception {
        _tag.setPageContext(_ctx);
        _tag.setDate(_d);
        _tag.setD("yyyy-MM-dd");
        _tag.setT("hh mm ss");
        _tag.setFmt("t");
        
        assertEvalPage(_tag.doEndTag());
        assertEquals("09 38 25", _jspOut.toString());
    }
    
    public void testDateOnly() throws Exception {
        _tag.setPageContext(_ctx);
        _tag.setDate(_d);
        _tag.setD("yyyy-MM-dd");
        _tag.setT("hh mm ss");
        _tag.setFmt("d");
        
        assertEvalPage(_tag.doEndTag());
        assertEquals("1972-12-23", _jspOut.toString());
    }
}
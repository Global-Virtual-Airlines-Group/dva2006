package org.deltava.mail;

import java.util.Map;

import org.deltava.beans.DistanceUnit;
import org.deltava.beans.Pilot;
import org.deltava.beans.TZInfo;
import org.deltava.beans.schedule.Airport;
import org.deltava.beans.system.AirlineInformation;
import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

import junit.framework.TestCase;

public class TestMessageContext extends TestCase {

    private MessageContext _ctxt;
    
    protected final class ContextObject {
        
        public static final int INT_FIELD = 1;
        
        public String getClassName() {
            return getClass().getName();
        }
        
        public Object barf() {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        
        AirlineInformation ai = new AirlineInformation("DVA", "Delta Virtual Airlines");
        ai.setDomain("deltava.org");
        SystemData.add("apps", Map.of(ai.getCode(), ai));
        SystemData.add("airline.code", ai.getCode());
        
        _ctxt = new MessageContext();
    }

    @Override
	protected void tearDown() throws Exception {
        _ctxt = null;
        super.tearDown();
    }
    
    public void testObject() {
        _ctxt.addData("name", "ContextName");
        assertTrue(_ctxt.hasData("name"));
        assertEquals("ContextName", _ctxt.execute("name"));
        assertEquals("ContextName", _ctxt.execute("name.toString"));
        assertEquals("", _ctxt.execute("invalidAttribute"));
        assertEquals("", _ctxt.execute("name.invalidMethod"));
        
        _ctxt.addData("obj", new ContextObject());
        assertTrue(_ctxt.hasData("obj"));
        assertEquals("1", _ctxt.execute("obj.INT_FIELD"));
        assertEquals("", _ctxt.execute("obj.barf"));
    }

    public void testNestedObject() {
        _ctxt.addData("name", "ContextName");
        assertTrue(_ctxt.hasData("name"));
        assertEquals("ContextName", _ctxt.execute("name"));
        assertEquals("java.lang.String", _ctxt.execute("name.getClass.getName"));
    }
    
    public void testFormatting() {
    	Pilot p = new Pilot("Test", "User");
    	p.setDateFormat("MM%dd%yyyy");
    	p.setTimeFormat("HH$mm:ss");
    	p.setNumberFormat("#,##0.0000");
    	p.setDistanceType(DistanceUnit.KM);
    	p.setTZ(TZInfo.UTC);
    	p.setAirportCodeType(Airport.Code.ICAO);
    	
    	_ctxt.setRecipient(p);
    	_ctxt.addData("name", "ContextName");
    	_ctxt.addData("birthday", StringUtils.parseInstant("12/29/1902 23:59:01", "MM/dd/yyyy HH:mm:ss"));
    	_ctxt.addData("distance", Integer.valueOf(300));
    	_ctxt.addData("longNumber", Long.valueOf(3008123));
    	_ctxt.addData("pi", Double.valueOf(Math.PI));
    	_ctxt.addData("ap", new Airport("ATL", "KATL", "Atlanta GA"));
    	
    	// Test raw and inferred formatting
    	assertEquals("ContextName", _ctxt.execute("name"));
        assertEquals("java.lang.String", _ctxt.execute("name.getClass.getName"));
        assertEquals("300", _ctxt.execute("distance"));
        assertEquals("java.lang.Integer", _ctxt.execute("distance.getClass.getName"));
        assertEquals("3,008,123", _ctxt.execute("longNumber"));
        assertEquals("java.lang.Long", _ctxt.execute("longNumber.getClass.getName"));
        assertEquals("3.1416", _ctxt.execute("pi"));
        assertEquals("java.lang.Double", _ctxt.execute("pi.getClass.getName"));
        assertEquals("KATL", _ctxt.execute("ap"));
        assertEquals("org.deltava.beans.schedule.Airport", _ctxt.execute("ap.getClass.getName"));
        assertEquals("12%30%1902 04$59:01 UTC", _ctxt.execute("birthday"));
        assertEquals("java.util.Date", _ctxt.execute("birthday.getClass.getName"));
        
        // Test explicit formatting
        assertEquals("483 Kilometers", _ctxt.execute("distance$L"));
        assertEquals("300", _ctxt.execute("distance$!"));
        assertEquals("12%30%1902", _ctxt.execute("birthday$D"));
        assertEquals("04$59:01 UTC", _ctxt.execute("birthday$T"));
        assertEquals("12%30%1902 04$59:01 UTC", _ctxt.execute("birthday$DT"));
        assertEquals("12%30%1902 04$59:01 UTC", _ctxt.execute("birthday$!"));
    }
}
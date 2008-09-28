// Copyright 2008 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans;

import junit.framework.TestCase;

public class TestOnlineNetwork extends TestCase {

	public void testValue() {
		assertEquals(0, OnlineNetwork.VATSIM.getValue());
		assertEquals(OnlineNetwork.VATSIM, OnlineNetwork.valueOf("VATSIM"));
		assertEquals(OnlineNetwork.VATSIM, OnlineNetwork.values()[OnlineNetwork.VATSIM.getValue()]);
		assertEquals(OnlineNetwork.IVAO, OnlineNetwork.valueOf("IVAO"));
		assertEquals(OnlineNetwork.IVAO, OnlineNetwork.values()[OnlineNetwork.IVAO.getValue()]);
		
		assertEquals("VATSIM", OnlineNetwork.VATSIM.toString());
	}
}
// Copyright 2017 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.testing;

import junit.framework.TestCase;

public class TestEquipmentRideScriptKey extends TestCase {

	@SuppressWarnings("static-method")
	public void testCurrencyKeyParse() {
		String id = "A320!!A320-C";
		EquipmentRideScriptKey key = EquipmentRideScriptKey.parse(id);
		assertEquals("A320", key.getProgramName());
		assertEquals("A320", key.getEquipmentType());
		assertTrue(key.isCurrency());
	}
	
	@SuppressWarnings("static-method")
	public void testKeyParse() {
		String id = "B737-800!!B737-200";
		EquipmentRideScriptKey key = EquipmentRideScriptKey.parse(id);
		assertEquals("B737-800", key.getProgramName());
		assertEquals("B737-200", key.getEquipmentType());
		assertFalse(key.isCurrency());
	}
}
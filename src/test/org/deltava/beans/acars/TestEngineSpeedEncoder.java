package org.deltava.beans.acars;

import junit.framework.TestCase;

public class TestEngineSpeedEncoder extends TestCase {

	@SuppressWarnings("static-method")
	public void testEncodeDecode() {
		
		double[] nx = new double[] {92.2, 92.1};
		byte[] data = EngineSpeedEncoder.encode(nx.length, nx);
		assertNotNull(data);
		
		double[] nx2 = EngineSpeedEncoder.decode(data);
		assertNotNull(nx2);
		assertEquals(nx.length, nx2.length);
		for (int eng = 0; eng < nx.length; eng++)
			assertEquals(nx[eng], nx2[eng], 0.0001);
	}
	
	@SuppressWarnings("static-method")
	public void testEngineShutdown() {
		
		double[] nx = new double[] {92.2, 92.1, 0.1, 92.3};
		byte[] data = EngineSpeedEncoder.encode(nx.length, nx);
		assertNotNull(data);
		
		double[] nx2 = EngineSpeedEncoder.decode(data);
		assertNotNull(nx2);
		assertEquals(nx.length, nx2.length);
		for (int eng = 0; eng < nx.length; eng++)
			assertEquals(nx[eng], nx2[eng], 0.0001);
	}
}
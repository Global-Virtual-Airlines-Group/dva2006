// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.util.tile;

import java.io.*;

import junit.framework.TestCase;

import org.gvagroup.tile.*;

public class TestPNGTile extends TestCase {

	@SuppressWarnings("static-method")
	public void testSerialization() throws Exception {
		
		PNGTile pt = new PNGTile(new TileAddress(1, 1, 2));
		assertNotNull(pt.getAddress());
		pt.setImage(new byte[1024]);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream(512);
		try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
			oos.writeObject(pt);
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		try (ObjectInputStream ios = new ObjectInputStream(in)) {
			PNGTile pt2 = (PNGTile) ios.readObject();
			assertNotNull(pt2);
		}
	}
}
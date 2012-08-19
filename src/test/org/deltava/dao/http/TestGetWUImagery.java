// Copyright 2012 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.dao.http;

import java.io.*;
import javax.imageio.ImageIO;

import junit.framework.TestCase;

import org.deltava.beans.GeoLocation;
import org.deltava.beans.schedule.GeoPosition;

import org.deltava.dao.DAOException;

import org.deltava.util.tile.*;

public class TestGetWUImagery extends TestCase {
	
	public void testBounds() {
		
		GeoLocation nw = new GeoPosition(48.65, -127.3);
		GeoLocation se = new GeoPosition(21.86, -63.4);
		
		Projection mp = new MercatorProjection(8);
		TileAddress nwAddr = mp.getAddress(nw);
		TileAddress seAddr = mp.getAddress(se);
		assertNotNull(nwAddr);
		assertNotNull(seAddr);
		
		System.out.println("CONUS is " + (seAddr.getX() - nwAddr.getX()) + " tiles wide, " +
				(seAddr.getY() - nwAddr.getY()) + " tiles high");
	}

	public void testLoadSuperTile() throws IOException, DAOException {
		
		GeoLocation pos = new GeoPosition(48.65, -127.3);
		
		GetWUImagery dao = new GetWUImagery();
		dao.setConnectTimeout(2000);
		dao.setConnectTimeout(5000);
		SuperTile st = dao.getRadar(pos, 1280, 1024, 8);
		assertNotNull(st);
		
		ImageIO.write(st.getImage(), "png", new File("c:\\temp\\st.png"));
	}
}
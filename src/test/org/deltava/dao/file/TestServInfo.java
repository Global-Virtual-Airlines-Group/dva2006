package org.deltava.dao.file;

import java.util.*;

import org.deltava.beans.servinfo.NetworkInfo;
import org.deltava.dao.file.GetServInfo;
import org.deltava.util.system.SystemData;

public class TestServInfo extends AbstractURLConnectionTestCase {
	
	private GetServInfo _dao;

	protected void setUp() throws Exception {
		super.setUp("data/satnet-data.txt");
		SystemData.init("org.deltava.util.system.TagTestSystemDataLoader", true);
		SystemData.add("airports", new HashMap());
		_dao = new GetServInfo(_is);
	}

	protected void tearDown() throws Exception {
		_dao = null;
		super.tearDown();
	}

	public void testRead() throws Exception {
		NetworkInfo info = _dao.getInfo("VATSIM");
		Collection p = info.getPilots();
		assertTrue(p.size() > 58);
	}
}
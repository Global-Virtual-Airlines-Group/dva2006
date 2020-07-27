// Copyright 2014, 2015, 2016, 2020 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.io.*;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.dao.file.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A helper class to encapsulate fetching online network data. 
 * @author Luke
 * @version 9.1
 * @since 5.4
 */

@Helper(NetworkInfo.class)
public class ServInfoHelper {
	
	private static final Logger log = Logger.getLogger(ServInfoHelper.class);
	private static final Cache<NetworkInfo> _iCache = CacheManager.get(NetworkInfo.class, "ServInfoData");	
	
	// singleton
	private ServInfoHelper() {
		super();
	}

	/**
	 * Fetches online network information.
	 * @param net the OnlineNetwork
	 * @return a NetworkInfo bean
	 */
	public static NetworkInfo getInfo(OnlineNetwork net) {
		
		// Fetch from cache if possible
		NetworkInfo info = _iCache.get(net);
		if (info != null)
			return info;
		
		String url = SystemData.get("online." + net.toString().toLowerCase() + ".status_url");
		boolean isJSON = (url != null) && url.endsWith(".json");
		try (FileInputStream fi = new FileInputStream(SystemData.get("online." + net.toString().toLowerCase() + ".local.info"))) {
			OnlineNetworkDAO dao = isJSON ? new GetVATSIMInfo(fi) : new GetServInfo(fi, net);
			info = dao.getInfo();
			if ((info != null) && (info.getValidDate() != null))
				_iCache.add(info);	
		} catch (Exception e) {
			log.error("Cannot load " + net + " ServInfo feed - " + e.getMessage(), e);
			info = new NetworkInfo(net);
			info.setValidDate(Instant.now());
		}
		
		return info;
	}
}
// Copyright 2014, 2015, 2016, 2020, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.servinfo;

import java.io.*;
import java.util.*;
import java.time.Instant;

import org.apache.log4j.Logger;

import org.deltava.beans.*;

import org.deltava.dao.DAOException;
import org.deltava.dao.file.*;

import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A helper class to encapsulate fetching online network data. 
 * @author Luke
 * @version 10.0
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
		
		// Load the data
		String url = SystemData.get("online." + net.toString().toLowerCase() + ".status_url");
		boolean isJSON = (url != null) && url.endsWith(".json");
		Collection<RadioPosition> positions = new ArrayList<RadioPosition>();
		if (isJSON && (net == OnlineNetwork.VATSIM)) {
			try (InputStream tfi = new BufferedInputStream(new FileInputStream(SystemData.get("online.vatsim.local.transceiver")), 65536)) {
				GetVATSIMTransceivers tdao = new GetVATSIMTransceivers(tfi);
				positions.addAll(tdao.load());
			} catch (DAOException | IOException ie) {
				log.warn("Cannot load VATSIM radio data - " + ie.getMessage());
			}
		}
		
		try (InputStream fi = new BufferedInputStream(new FileInputStream(SystemData.get("online." + net.toString().toLowerCase() + ".local.info")), 131072)) {
			OnlineNetworkDAO dao = isJSON ? new GetVATSIMInfo(fi) : new GetServInfo(fi, net);
			info = dao.getInfo();
			if ((info != null) && (info.getValidDate() != null)) {
				info.merge(positions);
				_iCache.add(info);
			}
		} catch (Exception e) {
			log.error("Cannot load " + net + " ServInfo feed - " + e.getMessage(), e);
			info = new NetworkInfo(net);
			info.setValidDate(Instant.now());
		}
		
		return info;
	}
}
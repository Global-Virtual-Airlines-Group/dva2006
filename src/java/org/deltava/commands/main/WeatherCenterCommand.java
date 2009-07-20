// Copyright 2008, 2009 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.main;

import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;

import org.deltava.beans.ComboAlias;
import org.deltava.commands.*;

import org.deltava.util.ComboUtils;
import org.deltava.util.cache.*;
import org.deltava.util.system.SystemData;

/**
 * A Web Site Command to centralize weather information. 
 * @author Luke
 * @version 2.6
 * @since 2.2
 */

public class WeatherCenterCommand extends AbstractCommand {
	
	private static Logger log = Logger.getLogger(WeatherCenterCommand.class);
	
	private final Collection<ComboAlias> _mapTypes = new ArrayList<ComboAlias>();
	private static final Cache<CacheableList<ComboAlias>> _imgCache = 
		new ExpiringCache<CacheableList<ComboAlias>>(8, 7200);

	/**
	 * Initializes the Command.
	 */
	public void init(String id, String cmdName) throws CommandException {
		super.init(id, cmdName);
		
		// Get the image types
		Map<?, ?> mapTypes = (Map<?, ?>) SystemData.getObject("weather.jetstream.types");
		for (Iterator<?> i = mapTypes.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry<?, ?> entry = (Map.Entry<?, ?>) i.next();
			_mapTypes.add(ComboUtils.fromString(entry.getValue().toString(), entry.getKey().toString()));
		}
	}
	
    /**
     * Executes the command.
     * @param ctx the Command context
     * @throws CommandException if an error occurs
     */
	public void execute(CommandContext ctx) throws CommandException {
		
		// Check the img cache
		Map<String, Collection<ComboAlias>> jetStreamImgs = new HashMap<String, Collection<ComboAlias>>();
		for (ComboAlias ca : _mapTypes) {
			CacheableList<ComboAlias> imgs = _imgCache.get(ca.getComboAlias());
			if (imgs == null) {
				log.info("Validating " + ca.getComboName() + " Jet Stream images");
				imgs = new CacheableList<ComboAlias>(ca.getComboAlias());
				String baseURL = SystemData.get("weather.jetstream.url." + ca.getComboAlias());
				for (int x = 0; x <= 120; x += 12) {
					try {
						URL url = new URL(baseURL.replace("%H", (x == 0) ? "anal" : ("h" + String.valueOf(x))));
						HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
						urlcon.setRequestMethod("HEAD");
						urlcon.setConnectTimeout(1250);
						urlcon.setReadTimeout(2500);
						urlcon.connect();
						if (urlcon.getResponseCode() == HttpURLConnection.HTTP_OK) {
							String name = (x == 0) ? "Current Analysis" : (String.valueOf(x) + " hour forecast"); 
							imgs.add(ComboUtils.fromString(name, url.toExternalForm()));
						}
					} catch (Exception e) {
						log.warn("Error validating " + ca.getComboName() + "/" + x + " image - " + e.getMessage());
					}
				}

				_imgCache.add(imgs);
			}
			
			// Save the images
			if (imgs.size() > 0)
				jetStreamImgs.put(ca.getComboAlias(), imgs);
		}

		// Get the pilot's home airport
		ctx.setAttribute("homeAirport", SystemData.getAirport(ctx.getUser().getHomeAirport()), REQUEST);
		
		// Save the Jet Stream images
		ctx.setAttribute("jetStreamTypes", _mapTypes, REQUEST);
		ctx.setAttribute("jetStreamImgs", jetStreamImgs, REQUEST);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("/jsp/pilot/wxCenter.jsp");
		result.setSuccess(true);
	}
}
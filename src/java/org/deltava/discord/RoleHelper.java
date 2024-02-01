// Copyright 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;
import java.util.stream.Collectors;

import org.javacord.api.entity.permission.Role;

import org.deltava.beans.*;

import org.deltava.util.system.SystemData;

/**
 * A utility class to calculate Discord roles for a given Pilot. 
 * @author Luke
 * @version 11.2
 * @since 11.1
 */

class RoleHelper {

	// static class
	private RoleHelper() {
		super();
	}
	
	/**
	 * Returns all Discord Roles a Pilot should be a member of. This does not include any server-specific Roles. 
	 * @param p a Pilot
	 * @return a Collection of Roles
	 */
	static Collection<Role> calculateRoles(Pilot p) {
		
		List<Role> results = new ArrayList<Role>();
		if (p.getNoVoice() || !p.getStatus().isActive())
			return results;
		
		results.add(Bot.findRole(SystemData.get("discord.role.default")));
        if (p.getRoles().contains("HR"))
        	results.add(Bot.findRole(SystemData.get("discord.role.hr")));
        if (p.getRoles().contains("PIREP"))
        	results.add(Bot.findRole(SystemData.get("discord.role.pirep")));	
        
        // Filter nulls/duplicates
        return results.stream().filter(Objects::nonNull).collect(Collectors.toSet()); 
	}

	/**
	 * Returns all Discord Roles managed via Golgotha.
	 * @return a Collection of Roles
	 */
	static Collection<Role> getManagedRoles() {
		
		List<Role> results = new ArrayList<Role>();
		results.add(Bot.findRole(SystemData.get("discord.role.default")));
		results.add(Bot.findRole(SystemData.get("discord.role.hr")));
		results.add(Bot.findRole(SystemData.get("discord.role.pirep")));
		
        // Filter nulls/duplicates
        return results.stream().filter(Objects::nonNull).collect(Collectors.toSet()); 
	}
}
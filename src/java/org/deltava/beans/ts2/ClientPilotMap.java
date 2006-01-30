// Copyright (c) 2006 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.ts2;

import java.util.*;

import org.deltava.beans.Pilot;

/**
 * A bean to store TeamSpeak 2 to Pilot mappings.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ClientPilotMap {

	private Map<String, Client> _clients = new HashMap<String, Client>();
	private Map<String, Pilot> _pilots = new HashMap<String, Pilot>();

	public void add(Pilot p, Client usr) {
		if (p != null) {
			_pilots.put(p.getPilotCode(), p);
			_clients.put(p.getPilotCode(), usr);
		}
	}
	
	public boolean contains(String pCode) {
		return _pilots.containsKey(pCode);
	}
	
	public Collection<String> getPilotCodes() {
		return _pilots.keySet();
	}
	
	public Client getClient(String pCode) {
		return _clients.get(pCode);
	}

	public Collection<Client> getCliients() {
		return new LinkedHashSet<Client>(_clients.values());
	}
	
	public Collection<Client> getClients(Collection<String> pCodes) {
		Collection<Client> results = new HashSet<Client>();
		for (Iterator<String> i = pCodes.iterator(); i.hasNext(); ) {
			Client usr = _clients.get(i.next());
			if (usr != null)
				results.add(usr);
		}
		
		return results;
	}
	
	public Pilot getPilot(String pCode) {
		return _pilots.get(pCode);
	}
	
	public Collection<Pilot> getPilots() {
		return new LinkedHashSet<Pilot>(_pilots.values());
	}
	
	public Collection<Pilot> getPilots(Collection<String> pCodes) {
		Collection<Pilot> results = new HashSet<Pilot>();
		for (Iterator<String> i = pCodes.iterator(); i.hasNext(); ) {
			Pilot p = _pilots.get(i.next());
			if (p != null)
				results.add(p);
		}
		
		return results;
	}
	
	public void remove(String pCode) {
		if (contains(pCode)) {
			_pilots.remove(pCode);
			_clients.remove(pCode);
		}
	}
	
	public int size() {
		return _pilots.size();
	}
}
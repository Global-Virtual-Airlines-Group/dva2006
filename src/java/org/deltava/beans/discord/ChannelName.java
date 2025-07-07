// Copyright 2023, 2025 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.beans.discord;

/**
 * An enumeration of Discord Channels.
 * @author danielw
 * @version 12.1
 * @since 11.0
 */

public enum ChannelName {
	WELCOME("waiting-room"), ALERTS("bot-alerts"), MOD_ALERTS("moderator-alerts"), MOD_ARCHIVE("moderator-archive"), INTERACTIONS("bot-interactions"), LOG("bot-log"), TESTING("bot-testing"), FLY_WITH_ME("fly-with-me"), FLIGHTS("flight-reports"); 
	
	private final String _name;

	ChannelName(String name) {
		_name = name;
	}
	
	public String getName() {
		return _name;
	}
}
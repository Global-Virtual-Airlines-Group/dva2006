// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;
import java.sql.*;

import org.apache.logging.log4j.*;

import org.javacord.api.*;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.*;

import org.deltava.beans.discord.Channel;

import org.deltava.dao.*;

import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.ConnectionPool;
import org.gvagroup.jdbc.ConnectionPoolException;

/**
 * The Discord Bot.
 * @author danielw
 * @author luke
 * @version 11.0
 * @since 11.0
 */

public class Bot {

    private static ConnectionPool _jdbcPool;
    
    private static Server _srv;
    private static final ContentFilter _filter = new ContentFilter();
    private static final Map<String, Long> _channelIDs = new HashMap<String, Long>();

    private static final Logger log = LogManager.getLogger(Bot.class);
    
    // static class
    private Bot() {
    	super();
    }

    public static void init() {

        _jdbcPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
        
        DiscordApi api = new DiscordApiBuilder().setToken(SystemData.get("security.key.discord")).setAllIntents().login().join();
        log.info("API Connected");

        log.info("Initializing Content Filter");
        try (Connection con = _jdbcPool.getConnection()) {
        	GetFilterData dao = new GetFilterData(con);
        	_filter.init(dao.getKeywords(), dao.getSafewords());
        } catch (ConnectionPoolException | DAOException | SQLException de) {
        	log.error("Error initializing Content Filter - " + de.getMessage(), de);
        }
        
        log.info("Generating Commands");
        api.bulkOverwriteGlobalApplicationCommands(Collections.emptySet());
        SlashCommand.with("addkey", "Adds an auto-mod bot keyword", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "keyword", "The keyword to add", true))).createGlobal(api).join();
        SlashCommand.with("dropkey", "Deletes a key by keyword or phrase. Must match exact spelling, case insensitive", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "keyphrase", "The keyword or phrase to delete", true))).createGlobal(api).join();
        SlashCommand.with("addsafe", "Adds a word to the safe list for the auto-mod bot", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "safeword", "The safe word to add."))).createGlobal(api).join();
        SlashCommand.with("dropsafe", "Removes a word from the safe list for the auto-mod bot", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "safeword", "The safe word to delete."))).createGlobal(api).join();

        log.info("Adding Listeners");
        api.addListener(new MessageReceivedListener());
        api.addListener(new CommandListener());
        api.addListener(new ModalListener());
        api.addListener(new MessageReplyListener());

        _srv = api.getServersByName(SystemData.get("discord.serverAddr")).iterator().next();
    }
    
    static ServerTextChannel findChannel(Channel c) {
    	Long id = _channelIDs.get(c.getName());
    	if (id == null) {
    		ServerTextChannel ch = null;
    		List<ServerTextChannel> channels = _srv.getTextChannelsByName(c.getName());
    		if (!channels.isEmpty()) {
    			ch = channels.get(0);
    			_channelIDs.put(c.getName(), Long.valueOf(ch.getId()));
    		} else
    			log.warn(String.format("Unknown Discord channel - %s", c.getName()));
    		
    		return ch;
    	}
    	
    	return _srv.getTextChannelById(id.longValue()).orElse(null);
    }
    
    static void send(Channel c, String msg) {
    	ServerTextChannel sc = findChannel(c);
    	if (sc == null) return;
    	sc.sendMessage(msg);
    }
    
    static void send(Channel c, EmbedBuilder b) {
    	ServerTextChannel sc = findChannel(c);
    	if (sc == null) return;
    	sc.sendMessage(b);
    }
    
    static Connection getConnection() throws ConnectionPoolException {
    	return _jdbcPool.getConnection();
    }
    
    static ContentFilter getFilter() {
    	return _filter;
    }
}
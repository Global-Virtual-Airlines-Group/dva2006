// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;
import java.sql.*;
import java.lang.reflect.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.*;

import org.javacord.api.*;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;

import okhttp3.OkHttpClient;

import org.deltava.beans.*;
import org.deltava.beans.discord.ChannelName;

import org.deltava.dao.*;

import org.deltava.util.TaskTimer;
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

    private static Server _srv;
    private static final ContentFilter _filter = new ContentFilter();
    private static final Map<String, Long> _channelIDs = new HashMap<String, Long>();

    private static final Logger log = LogManager.getLogger(Bot.class);
    
    // static class
    private Bot() {
    	super();
    }

    /**
     * Initializes the Discord bot.
     */
    public static void init() {

    	DiscordApi api;
    	try {
    		DiscordApiBuilder b = new DiscordApiBuilder().setToken(SystemData.get("security.key.discord")).setAllIntents().setShutdownHookRegistrationEnabled(true);
    		api = b.login().orTimeout(3500, TimeUnit.MILLISECONDS).join();
    		log.info("API Connected");
    	} catch (CompletionException ce) {
    		log.error("Error connecting to Discord API - " + ce.getMessage(), ce);
    		return;
    	}
        
        log.info("Initializing Content Filter");
        try (Connection con = getConnection()) {
        	GetFilterData dao = new GetFilterData(con);
        	_filter.init(dao.getKeywords(false), dao.getKeywords(true));
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

        Collection<Server> srvs = api.getServersByName(SystemData.get("discord.serverAddr"));
        if (srvs.isEmpty())
        	log.error("Cannot find Discord Server!");
        else
        	_srv = srvs.iterator().next();
    }
    
    /**
     * Disconnects the Bot from the Discord API.
     */
    public static void disconnect() {
    	if (_srv == null) {
    		log.warn("Not initialized!");
    		return;
    	}

    	// Wait for discord to shut down
    	try {
    		TaskTimer tt = new TaskTimer();
    		DiscordApi api = _srv.getApi();
    		try {
    			Field f = api.getClass().getDeclaredField("httpClient");
    			f.setAccessible(true);
    			OkHttpClient httpClient = (OkHttpClient) f.get(api);
    			if (httpClient != null) {
    				log.info("Shutting down HTTP Client");
    				httpClient.dispatcher().executorService().shutdownNow();
    				httpClient.connectionPool().evictAll();
    			}
    		} catch (NoSuchFieldException | IllegalAccessException nfe) {
    			log.error(nfe.getClass().getSimpleName() + " obtaining Discord HTTP Client - " + nfe.getMessage());	
    		}
    		
    		log.info("Disconnecting from Discord API");
    		CompletableFuture<Void> cf = api.disconnect().orTimeout(4500, TimeUnit.MILLISECONDS);
    		cf.join();
    		log.info("Shutdown in " + tt.stop() + "ms");
    		
    		// Wait for threads to shut down
    		Thread.sleep(58000);
    	} catch (CompletionException ce) {
    		log.error("Error disconnecting from Discord API - " + ce.getMessage(), ce);
    	} catch (InterruptedException ie) {
    		log.warn("Interrupted waiting for threads to terminate");
    	}
    }
    
    static ServerTextChannel findChannel(ChannelName c) {
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
    
    static void send(ChannelName c, String msg) {
    	ServerTextChannel sc = findChannel(c);
    	if (sc == null) return;
    	sc.sendMessage(msg);
    }
    
    static void send(ChannelName c, EmbedBuilder b) {
    	ServerTextChannel sc = findChannel(c);
    	if (sc == null) return;
    	sc.sendMessage(b);
    }
    
    static Role findRole(String roleName) {
    	List<Role> roles = _srv.getRolesByName(roleName);
    	return roles.isEmpty() ? null : roles.get(0);
    }
    
    static Connection getConnection() throws ConnectionPoolException {
    	ConnectionPool jdbcPool = (ConnectionPool) SystemData.getObject(SystemData.JDBC_POOL);
    	return jdbcPool.getConnection();
    }
    
    static ContentFilter getFilter() {
    	return _filter;
    }
    
    /**
     * Removes a Pilot's Discord roles.
     * @param p the Pilot
     */
    public static void clearRoles(Pilot p) {
    	if (!p.hasID(ExternalID.DISCORD)) return;
    	
    	// Lookup user
    	try {
    		User usr = _srv.getApi().getUserById(p.getExternalID(ExternalID.DISCORD)).get();
    		if (usr == null) {
    			log.warn(String.format("User %s (%s) not found with ID %s", p.getName(), p.getPilotCode(), p.getExternalID(ExternalID.DISCORD)));
    			return;
    		}
    	
    		// Remove roles
    		List<Role> roles = usr.getRoles(_srv);
    		roles.forEach(usr::removeRole);
    		log.info(String.format("Removed Discord roles %s from %s (%s)", roles, p.getName(), p.getPilotCode()));
    	} catch (ExecutionException ee) {
    		log.error(ee.getMessage(), ee);
    	} catch (InterruptedException ie) {
    		log.warn(String.format("Interrupted removing Discord roles from %s (%s)", p.getName(), p.getPilotCode()));
    	}
    }
}
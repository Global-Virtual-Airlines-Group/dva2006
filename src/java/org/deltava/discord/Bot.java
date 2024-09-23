// Copyright 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.util.*;
import java.sql.*;
import java.lang.reflect.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.*;

import org.javacord.api.*;
import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.interaction.*;

import com.newrelic.api.agent.NewRelic;

import okhttp3.OkHttpClient;

import org.deltava.beans.*;
import org.deltava.beans.discord.ChannelName;

import org.deltava.dao.*;

import org.deltava.util.TaskTimer;
import org.deltava.util.system.SystemData;

import org.gvagroup.pool.*;

/**
 * The Discord Bot.
 * @author danielw
 * @author luke
 * @version 11.3
 * @since 11.0
 */

public class Bot {

    private static Server _srv;
    private static final ContentFilter _filter = new ContentFilter();
    private static final Map<String, Long> _channelIDs = new HashMap<String, Long>();
    
    private static final Semaphore _disconnectLock = new Semaphore(1);

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
    		log.atError().withThrowable(ce).log("Error connecting to Discord API - {}", ce.getMessage());
    		return;
    	}
        
        log.info("Finding Server");
        long id = SystemData.getLong("discord.id", 0);
        if (id == 0) {
        	Collection<Server> srvs = api.getServersByName(SystemData.get("discord.serverAddr"));
        	if (!srvs.isEmpty()) {
        		_srv = srvs.iterator().next();
        		log.warn("Found Server {}", Long.valueOf(_srv.getId()));
        	}
        } else
        	_srv = api.getServerById(id).orElse(null);
        
        if (_srv == null) {
        	log.error("Cannot find Discord Server!");
        	return;
        }
        
        log.info("Generating Commands");
        Set<SlashCommandBuilder> cmds = new LinkedHashSet<SlashCommandBuilder>();
        cmds.add(SlashCommand.with("addkey", "Adds a moderation keyword", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "keyword", "The keyword to add", true))));
        cmds.add(SlashCommand.with("dropkey", "Deletes a moderation keyword", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "keyword", "The keyword to delete", true))));
        cmds.add(SlashCommand.with("addsafe", "Adds a permitted word", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "safeword", "The keyword to add"))));
        cmds.add(SlashCommand.with("dropsafe", "Removes a permitted word", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "safeword", "The keyword to delete"))));
        cmds.add(SlashCommand.with("allkeys", "Displays keyword lists"));
        cmds.add(SlashCommand.with("reloadkeys", "Reloads keyword lists"));
        cmds.forEach(cb -> cb.setDefaultEnabledForPermissions(PermissionType.BAN_MEMBERS, PermissionType.KICK_MEMBERS));
        api.bulkOverwriteServerApplicationCommands(_srv, cmds);
        SlashCommandBuilder scb = SlashCommand.with("warn", "Send Content Warning", List.of(SlashCommandOption.create(SlashCommandOptionType.STRING, "msg", "Additional Information")));
        scb.setDefaultEnabledForEveryone().createForServer(_srv).join();
        
        log.info("Initializing Content Filter");
        Connection con = null;
        try {
        	con = getConnection();
        	GetFilterData dao = new GetFilterData(con);
        	_filter.init(dao.getKeywords(false), dao.getKeywords(true));
        } catch (ConnectionPoolException | DAOException de) {
        	log.atError().withThrowable(de).log("Error initializing Content Filter - {}", de.getMessage());
        } finally {
        	Bot.release(con);
        }
        
        log.info("Adding Listeners");
        api.addListener(new MessageReceivedListener());
        api.addListener(new CommandListener());
        api.addListener(new ModalListener());
        api.addListener(new MessageReplyListener());
    }
    
    /**
     * Disconnects the Bot from the Discord API.
     */
    public static void disconnect() {
    	if (_srv == null) {
    		log.warn("Not initialized!");
    		return;
    	} else if (!_disconnectLock.tryAcquire()) {
    		log.warn("Already shutting down");
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
    			log.error("{} obtaining Discord HTTP Client - {}",nfe.getClass().getSimpleName(), nfe.getMessage());	
    		}
    		
    		log.info("Disconnecting from Discord API");
    		CompletableFuture<Void> cf = api.disconnect().orTimeout(4500, TimeUnit.MILLISECONDS);
    		cf.join();
    		log.warn("Shutdown in {}ms", Long.valueOf(tt.stop()));
    		
    		// Wait for threads to shut down
    		Thread.sleep(8000);
    	} catch (CompletionException ce) {
    		log.atError().withThrowable(ce).log("Error disconnecting from Discord API - {}", ce.getMessage());
    	} catch (InterruptedException ie) {
    		log.warn("Interrupted waiting for threads to terminate");
    	} finally {
    		_srv = null;
    		_disconnectLock.release();
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
    			log.warn("Unknown Discord channel - {}", c.getName());
    		
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
    	ConnectionPool<Connection> jdbcPool = SystemData.getJDBCPool();
    	return jdbcPool.getConnection();
    }
    
    static void release(Connection c) {
    	ConnectionPool<Connection> jdbcPool = SystemData.getJDBCPool();
    	jdbcPool.release(c);
    }
    
    static ContentFilter getFilter() {
    	return _filter;
    }
    
    /**
     * Checks if the Discord bot has been initialized.
     * @return TRUE if initialized and connected to the Discord server, otherwise FALSE
     */
    public static boolean isInitialized() {
    	return (_srv != null);
    }
    
    /**
     * Removes a Pilot's Discord roles.
     * @param p the Pilot
     */
    public static void resetRoles(Pilot p) {
    	if (!p.hasID(ExternalID.DISCORD)) return;
    	if (!isInitialized()) {
    		log.error("Bot not initialized - Cannot remove roles from %s", p.getName());
    		return;
    	}
    	
    	// Lookup user
    	try {
    		User usr = _srv.getApi().getUserById(p.getExternalID(ExternalID.DISCORD)).get();
    		if (usr == null) {
    			log.warn("User {} ({}) not found with ID {}", p.getName(), p.getPilotCode(), p.getExternalID(ExternalID.DISCORD));
    			return;
    		}
    	
    		// Remove roles
    		Collection<Role> roles = RoleHelper.getManagedRoles();
    		roles.forEach(usr::removeRole);
    		log.info("Removed Discord roles {} from {} ({})", roles, p.getName(), p.getPilotCode());
    		
    		// Add new roles
    		roles = RoleHelper.calculateRoles(p);
    		roles.forEach(usr::addRole);
    		log.info("Added Discord roles {} to {} ({})", roles, p.getName(), p.getPilotCode());
    	} catch (ExecutionException ee) {
    		log.atError().withThrowable(ee).log(ee.getMessage());
    		NewRelic.noticeError(ee, false);
    	} catch (InterruptedException ie) {
    		log.warn("Interrupted removing Discord roles from {} ({})", p.getName(), p.getPilotCode());
    	}
    }
}
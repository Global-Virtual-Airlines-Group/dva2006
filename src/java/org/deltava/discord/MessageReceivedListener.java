// Copyright 2023, 2024, 2025, 2026 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import org.apache.logging.log4j.*;

import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

import org.deltava.beans.*;
import org.deltava.beans.discord.ChannelName;

import org.deltava.util.*;
import org.deltava.util.log.*;
import org.deltava.util.system.SystemData;

/**
 * A Discord message receiver.
 * @author Luke
 * @author danielw
 * @version 12.4
 * @since 11.0
 */

public class MessageReceivedListener implements MessageCreateListener {

    private static final Logger log = LogManager.getLogger(MessageReceivedListener.class);

    @Override
    @Trace(dispatcher=true)
    public void onMessageCreate(MessageCreateEvent e) {
    	
    	NewRelic.setTransactionName("Discord", "msgCreate");
        NewRelic.setRequestAndResponse(new SyntheticRequest("msgCreate", "Discord"), new SyntheticResponse());

    	String msg = e.getMessageContent();
    	Server srv = e.getServer().orElse(null);
    	if (srv == null) // bot-generated messages
    		return;

    	Optional<ServerChannel> sch = e.getChannel().asServerChannel();
    	String channelName = sch.isPresent() ? sch.get().getName() : "UNKNOWN";
    	try {
    		// Check for bot
    		boolean isBot = (e.getMessageAuthor().isBotUser() || e.getMessageAuthor().isBotOwner());
    		User usr = e.getMessageAuthor().asUser().orElse(null);
    		if (!isBot && (usr != null))
    			isBot = usr.getRoles(srv).stream().anyMatch(r -> r.getName().equalsIgnoreCase("Bot"));
    		
    		if (!sch.isPresent() || channelName.equals(ChannelName.INTERACTIONS.getName()) || isBot) 
    			return;

    		// Check if this is a command in the Welcome channge
    		if (channelName.equals(ChannelName.WELCOME.getName())) {
    			User u = e.getMessageAuthor().asUser().orElse(null);
    			String msgText = e.getMessageContent();
    			
    			// Delete message
    			e.getMessage().delete("Auto delete interaction message").get();
    			log.info("Delete Welcome channel message from {}", (u == null) ? "Anonymous" : u.getName());
    			
    			if (msgText.startsWith("!") && (u != null)) {
    				int pos = msgText.indexOf(' ');
    				String cmdName = msgText.substring(1, (pos == -1) ? msgText.length() : pos).toLowerCase();
    				switch (cmdName) {
    				case "link":
    					long UUID = u.getId();
    					log.info("Registration request received [ UUID = {}, Name = {} ]", Long.toHexString(UUID), e.getMessageAuthor().getName());
    					Pilot p = Bot.getPilot(u.getIdAsString());
    					if (p != null)
    						u.sendMessage("You have already linked your Discord and web stie accounts.");
    					else
    						u.sendMessage(EmbedGenerator.register(UUID));
    					
    					break;
    					
    				case "roles":
    					assignRoles(e, u);
    					break;
    				
    				case "status":
    					p = Bot.getPilot(u.getIdAsString());
    					log.info("Registration status request [ Name = {}, UUID = {}, Server = {} ]", u.getName(), Long.toHexString(u.getId()), Long.valueOf(srv.getId()), (p != null) ? p.getName() : "UNLINKED");
    					u.sendMessage(EmbedGenerator.createStatus(e, p, u.getRoles(srv)));				
    					break;
    				
    				case "help":
    					u.sendMessage(EmbedGenerator.welcome(e));
    					break;
    				
    				default:
    					log.warn("Unknown Discord command - {}", cmdName);
    					u.sendMessage(EmbedGenerator.welcome(e));
    				}
    				
    				return;
    			}
    		}
    		
    		// Check content
    		FilterResults fr = Bot.getFilter().search(msg);
    		if (!fr.isOK()) {
    			log.warn("Content warning from {} - {} [{}]", e.getMessageAuthor().getDisplayName(), msg, fr.getFlaggedResults());
    			Bot.send(ChannelName.MOD_ALERTS, EmbedGenerator.createWarning(e, fr.getFlaggedResults()));
    		}
    	} catch (Exception ex) {
    		log.atError().withThrowable(ex).log("Error on MessageReceive - {}", ex.getMessage());
    		NewRelic.noticeError(ex, false);
    		ChannelName ch = EnumUtils.parse(ChannelName.class, channelName, ChannelName.LOG);
    		Bot.send(ch, EmbedGenerator.createError(e.getMessageAuthor().getDisplayName(), "Registration", ex));
    	}
    }
    
    private static void assignRoles(MessageCreateEvent e, User msgAuth) {

        Server srv = e.getServer().orElse(null);
        log.info("User requested access [ Name = {}, UUID = {}, Server = {} ]", msgAuth.getName(), Long.toHexString(msgAuth.getId()), srv);
        
        Pilot p = Bot.getPilot(msgAuth.getIdAsString()); 
        if (p == null) {
        	log.warn("Cannot find Discord ID {}", msgAuth.getIdAsString());
        	return;
        } else if ((p.getStatus() != PilotStatus.ACTIVE) || p.getNoVoice()) {
        	log.warn("{} ({}) Status = {}, NoVoice = {}", p.getName(), p.getPilotCode(), p.getStatus(), Boolean.valueOf(p.getNoVoice()));
        	msgAuth.sendMessage(EmbedGenerator.createInsufficientAccess(e));
        	return;
        }
        
        // Check that user meets base roles
        Collection<?> reqRoles = (Collection<?>) SystemData.getObject("discord.requiredRoles");
        boolean hasReqRoles = p.getRoles().stream().anyMatch(reqRoles::contains);
        if (!hasReqRoles) {
        	log.warn("User {} ({}) not in required Roles {} [ roles = {} ]", p.getName(), p.getPilotCode(), reqRoles, p.getRoles());
        	msgAuth.sendMessage(EmbedGenerator.createInsufficientAccess(e));
        	return;
        }
        
        // Set roles
        Collection<Role> roles = RoleHelper.calculateRoles(p);
        IntervalTaskTimer tt = new IntervalTaskTimer();
        CompletableFuture<?>[] fs = roles.stream().map(msgAuth::addRole).collect(Collectors.toList()).toArray(new CompletableFuture[roles.size()]);
        CompletableFuture.allOf(fs).join();
        tt.mark("roles");
        
        // Set nickname
        Bot.assignNickname(p, roles);
       	tt.mark("nickname");
        
        // Everything went well
        long ms = tt.stop();
        msgAuth.sendMessage(String.format("Your %s account (%s) has been located and linked to this Discord user profile", SystemData.get("airline.name"), p.getPilotCode()));
        msgAuth.sendMessage(String.format("If you feel that a mistake has been made, submit a ticket here: https://%s/helpdesk.do", SystemData.get("airline.url")));
        log.log((ms > 1500) ? Level.WARN : Level.INFO, "Registered User [ Name = {}, UUID = {} ] - {}", p.getName(), Long.toHexString(e.getMessageAuthor().getId()), tt);
    }
}
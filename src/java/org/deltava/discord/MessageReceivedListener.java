// Copyright 2023, 2024 Global Virtual Airlines Group. All Rights Reserved.
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
import java.sql.Connection;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

import org.deltava.beans.*;
import org.deltava.beans.discord.ChannelName;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.log.*;
import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.ConnectionPoolException;

/**
 * A Discord message receiver.
 * @author Luke
 * @author danielw
 * @version 11.3
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
    		
    		if ("done".equalsIgnoreCase(msg) && sch.isPresent()) {
    			e.getMessage().delete("Auto delete interaction message");
    			assignRoles(e);
    			return;
    		} else if (!sch.isPresent() || channelName.equals(ChannelName.INTERACTIONS.getName()) || isBot) 
    			return;

    		// Check if this is a message requesting roles
    		if (channelName.equals(ChannelName.WELCOME.getName())) {
    			if (!e.getMessageContent().equalsIgnoreCase("!roles")) {
    				e.getMessage().delete("Message not allowed in " + ChannelName.WELCOME);
                    Optional<User> uo = e.getMessageAuthor().asUser();
                    if (uo.isPresent()) {
                    	User u = uo.get();
                    	if (u.getRoles(srv).isEmpty())
                    		u.sendMessage(EmbedGenerator.welcome(e));
                    }
    			} else {
    				register(e);
    				return;
    			}
    		}
    		
    		// Check content
    		FilterResults fr = Bot.getFilter().search(msg);
    		if (!fr.isOK()) {
    			log.warn("Bot = {}, {}, {}", Boolean.valueOf(e.getMessageAuthor().isBotOwner()), Boolean.valueOf(e.getMessageAuthor().isBotUser()), Boolean.valueOf(isBot));
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

    private static void register(MessageCreateEvent e) {
        long UUID = e.getMessageAuthor().getId();
        log.info("Registration request received [ UUID = {}, Name = {} ]", Long.toHexString(UUID), e.getMessageAuthor().getName());
        if (e.getMessageAuthor().asUser().isPresent())
            e.getMessageAuthor().asUser().get().sendMessage(EmbedGenerator.register(UUID));

        e.getMessage().delete("Auto-delete role request message");
    }

    private static void assignRoles(MessageCreateEvent e) {

    	// Make sure user is stil here
    	if (e.getMessageAuthor().asUser().isEmpty()) return;
        User msgAuth = e.getMessageAuthor().asUser().get();
        Server srv = e.getServer().orElse(null);
        log.info("User requested access [ Name = {}, UUID = {}, Server = {} ]", msgAuth.getName(), Long.toHexString(msgAuth.getId()), srv);
        
        Pilot p = null; Connection con = null;
        try {
        	con = Bot.getConnection();
        	GetPilotDirectory pdao = new GetPilotDirectory(con);
        	p = pdao.getByIMAddress(msgAuth.getIdAsString());
        } catch (ConnectionPoolException cpe) {
        	log.error("Connection Pool Full, Aborting");
        } catch (DAOException de) {
        	log.atError().withThrowable(de).log(de.getMessage());
        } finally {
        	Bot.release(con);
        }
        
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
        
        // Set the nickname and roles
        String nickname = String.format("%s (%s)", p.getName(), p.getPilotCode());
        Collection<Role> roles = RoleHelper.calculateRoles(p);
        IntervalTaskTimer tt = new IntervalTaskTimer();
        CompletableFuture<?>[] fs = roles.stream().map(msgAuth::addRole).collect(Collectors.toList()).toArray(new CompletableFuture[roles.size()]);
        CompletableFuture.allOf(fs).join();
        tt.mark("roles");
        
        // Unable to do nicknames longer than 32 chars
        if (nickname.length() <= 32) {
        	msgAuth.updateNickname(srv, nickname).join();
        	tt.mark("nickname");
        	Bot.send(ChannelName.ALERTS, EmbedGenerator.createNick(e, p, roles, nickname));
    	} else {
    		log.warn("Cannot set nickname {} to {} ({})", nickname, msgAuth.getDisplayName(srv), Integer.valueOf(nickname.length()));
        	Bot.send(ChannelName.ALERTS, EmbedGenerator.createNicknameError(e, p, roles));
    	}
        
        // Everything went well
        long ms = tt.stop();
        msgAuth.sendMessage(String.format("Your %s account (%s) has been located and linked to this Discord user profile", SystemData.get("airline.name"), p.getPilotCode()));
        msgAuth.sendMessage(String.format("If you feel that a mistake has been made, submit a ticket here: https://%s/helpdesk.do", SystemData.get("airline.url")));
        log.log((ms > 1500) ? Level.WARN : Level.INFO, "Registered User [ Name = {}, UUID = {} ] - {}", p.getName(), Long.toHexString(e.getMessageAuthor().getId()), tt);
    }
}
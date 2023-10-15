// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import org.apache.logging.log4j.*;

import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.sql.*;
import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.discord.ChannelName;

import org.deltava.dao.*;
import org.deltava.util.EnumUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.ConnectionPoolException;

/**
 * A Discord message receiver.
 * @author Luke
 * @author danielw
 * @version 11.1
 * @since 11.0
 */

public class MessageReceivedListener implements MessageCreateListener {

    private static final Logger log = LogManager.getLogger(MessageReceivedListener.class);

    @Override
    public void onMessageCreate(MessageCreateEvent e) {

    	String msg = e.getMessageContent();
    	Optional<ServerChannel> sch = e.getChannel().asServerChannel();
    	String channelName = sch.isPresent() ? sch.get().getName() : "UNKNOWN";
    	try {
    		if (msg.equalsIgnoreCase("done")) {
    			e.getMessage().delete("Auto delete interaction message");
    			assignRoles(e);
    			return;
    		}
        
    		// Ignore everything in #bot-interactions or sent by a bot
    		if (channelName.equals(ChannelName.INTERACTIONS.getName()) || e.getMessageAuthor().isBotUser()) return;

    		// Check if this is a message requesting roles
    		if (channelName.equals(ChannelName.WELCOME.getName())) {
    			if (!e.getMessageContent().equalsIgnoreCase("!roles")) {
    				e.getMessage().delete("Message not allowed in " + ChannelName.WELCOME);
                    Optional<User> uo = e.getMessageAuthor().asUser();
                    if (uo.isPresent()) {
                    	User u = uo.get();
                    	if (u.getRoles(e.getServer().get()).isEmpty())
                    		u.sendMessage(EmbedGenerator.welcome(e));
                    }
    			} else
    				registerDVA(e);
    		}
        
    		// Check content
    		FilterResults fr = Bot.getFilter().search(msg);
    		if (!fr.isOK())
    			Bot.send(ChannelName.MOD_ALERTS, EmbedGenerator.createKeyword(e, fr.getFlaggedResults()));
    	} catch (Exception ex) {
    		log.atError().withThrowable(ex).log("Error on MessageReceive - {}", ex.getMessage());
    		ChannelName ch = EnumUtils.parse(ChannelName.class, channelName, ChannelName.LOG);
    		Bot.send(ch, EmbedGenerator.createError(e.getMessageAuthor().getDisplayName(), "Registration", ex));
    	}
    }

    public static void registerDVA(MessageCreateEvent e) {
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
        log.info("User requested access [ Name = {}, UUID = {} ]", msgAuth.getName(), Long.toHexString(msgAuth.getId()));
        
        Pilot p = null;
        try (Connection con = Bot.getConnection()) {
        	GetPilotDirectory pdao = new GetPilotDirectory(con);
        	p = pdao.getByIMAddress(msgAuth.getIdAsString());
        } catch (ConnectionPoolException cpe) {
        	log.error("Connection Pool Full, Aborting");
        } catch (DAOException | SQLException de) {
        	log.atError().withThrowable(de).log(de.getMessage());
        }
        
        if (p == null) {
        	log.warn("Cannot find Discord ID {}", msgAuth.getIdAsString());
        	return;
        } else if (p.getStatus() != PilotStatus.ACTIVE) {
        	log.warn("{} ({}) Status = {}", p.getName(), p.getPilotCode(), p.getStatus());
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
        
        // Determine roles as appropriate
        Role r = null;
        if (p.getRoles().contains("HR"))
        	r = Bot.findRole(SystemData.get("discord.role.hr"));
        else if (p.getRoles().contains("PIREP") || p.getRoles().contains("Operations"))
        	r = Bot.findRole(SystemData.get("discord.role.pirep"));
        if (r == null)
        	r = Bot.findRole(SystemData.get("discord.role.default"));

        // Set the nickname and role
        String nickname = String.format("%s (%s)", p.getName(), p.getPilotCode());
        msgAuth.addRole(r);

        // Unable to do nicknames longer than 32 chars or less than 1
        if (nickname.length() <= 32) {
        	msgAuth.updateNickname(e.getServer().get(), nickname);
        	Bot.send(ChannelName.ALERTS, EmbedGenerator.createNick(e, p, r.getName(), nickname));
    	} else
        	Bot.send(ChannelName.ALERTS, EmbedGenerator.createNicknameError(e, p, r.getName())); 

        // Everything went well
        msgAuth.sendMessage(String.format("Your %s account (%s) has been located and linked to this Discord user profile", SystemData.get("airline.code"), p.getPilotCode()));
        msgAuth.sendMessage(String.format("If you feel that a mistake has been made, submit a ticket here: https://%s/helpdesk.do", SystemData.get("airline.url")));
        log.info(String.format("Registered User [ Name = %s, UUID = %s ]", p.getName(), Long.toHexString(e.getMessageAuthor().getId())));
    }
}
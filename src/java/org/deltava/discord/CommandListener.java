// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.sql.Connection;
import java.util.Optional;

import org.apache.logging.log4j.*;

import org.javacord.api.interaction.*;
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.message.component.*;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;

import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;

import org.deltava.beans.discord.*;

import org.deltava.dao.*;

import org.deltava.util.*;
import org.deltava.util.log.*;
import org.deltava.util.system.SystemData;

/**
 * A class to listen for Discord commands.
 * @author Danielw
 * @author Luke
 * @version 11.1
 * @since 11.0
 */

public class CommandListener implements org.javacord.api.listener.interaction.SlashCommandCreateListener {
	
	private static final Logger log = LogManager.getLogger(CommandListener.class); 

    @Override
    @Trace(dispatcher=true)
    public void onSlashCommandCreate(SlashCommandCreateEvent e) {
        SlashCommandInteraction sci = e.getSlashCommandInteraction();
        String cmdName = sci.getCommandName().toLowerCase();
        NewRelic.setProductName(SystemData.get("airline.code"));
        NewRelic.setTransactionName("Discord", cmdName);
        NewRelic.setRequestAndResponse(new SyntheticRequest(cmdName, "Discord"), new SyntheticResponse());

        TaskTimer tt = new TaskTimer();
        try {
        	switch (cmdName) {
        		case "allkeys" -> showKeys();
        		case "reloadkeys" -> reloadKeys(e);
        		case "addkey" -> addWord(e, false);
        		case "dropkey" -> dropWord(e, false);
        		case "flywithme" -> newFlyWithMeRequest(e);
        		case "addsafe" -> addWord(e, true);
        		case "dropsafe" -> dropWord(e, true);
        		case "warn" -> sendWarning(e);
        		default -> log.info("Ignored command - {}", cmdName);
        	}
        } finally {
        	NewRelic.recordResponseTimeMetric(cmdName, tt.stop());
        }
    }

	private static void addWord(SlashCommandCreateEvent e, boolean isSafe) {

        SlashCommandInteraction sci = e.getSlashCommandInteraction();
        String keyType = isSafe ? "safe" : "key";
        
        // Get the keyword
    	String key = getOption(sci, keyType + "word");
    	if (StringUtils.isEmpty(key)) {
    		createResponse(sci, String.format("No %s word specified", keyType), true).respond();
    		return;
    	}
    	
        // Add the keyword
        boolean isAdded = Bot.getFilter().add(key, isSafe);
        if (!isAdded) {
        	createResponse(sci, String.format("%s word does not exist", keyType), true).respond();
            return;
        }
        
        // Write to the database
        try (Connection con = Bot.getConnection()){
        	SetFilterData wdao = new SetFilterData(con);
        	wdao.add(key, isSafe);
        	Bot.getFilter().add(key, isSafe);
        	createResponse(sci, String.format("%s word %s added", keyType, key), true).respond();
            Bot.send(ChannelName.ALERTS, EmbedGenerator.wordAdded(isSafe, key, sci.getUser().getDisplayName(sci.getServer().get())));
        } catch (Exception ex) {
        	log.atError().withThrowable(ex).log("Error adding {} word - {}", keyType, ex.getMessage());
        	NewRelic.noticeError(ex, false);
        	Bot.send(ChannelName.LOG, EmbedGenerator.createError(sci.getUser().getDisplayName(sci.getServer().get()), String.format("Add %s word", keyType), ex));
        }
    }

    private static void dropWord(SlashCommandCreateEvent e, boolean isSafe) {

    	SlashCommandInteraction sci = e.getSlashCommandInteraction();
    	String keyType = isSafe ? "safe" : "key";
    	
    	String key = getOption(sci, keyType + "word");
    	if (StringUtils.isEmpty(key)) {
    		createResponse(sci, String.format("No %s word specified", keyType), true).respond();
    		return;
    	}
    	
    	// Remove the keyword
    	boolean isDropped = Bot.getFilter().delete(key, isSafe);
    	if (!isDropped) {
    		createResponse(sci, String.format("%s word does not exist", keyType), true).respond();
            return;
        }
    	
    	try (Connection con = Bot.getConnection()) {
    		SetFilterData wdao = new SetFilterData(con);
    		wdao.delete(key, isSafe);
    		Bot.getFilter().delete(key, isSafe);
    		createResponse(sci, String.format("%s word %s removed", keyType, key), true).respond();
           	Bot.send(ChannelName.ALERTS, EmbedGenerator.wordDeleted(isSafe, key, sci.getUser().getDisplayName(sci.getServer().get())));
    	} catch (Exception ex) {
    		log.atError().withThrowable(ex).log("Error removing {} word - {}", keyType, ex.getMessage());
    		NewRelic.noticeError(ex, false);
        	Bot.send(ChannelName.LOG, EmbedGenerator.createError(sci.getUser().getDisplayName(sci.getServer().get()), String.format("Remove %s word", keyType), ex));
    	}
    }
    
    private static void showKeys() {
    	try {
    		ContentFilter cf = Bot.getFilter();
    		Bot.send(ChannelName.ALERTS, EmbedGenerator.showKeys(false, cf.getKeywords()));
    		Bot.send(ChannelName.ALERTS, EmbedGenerator.showKeys(true, cf.getSafewords()));
    	} catch (Exception ex) {
    		log.atError().withThrowable(ex).log("Error displaying keywords - {}", ex.getMessage());
    		NewRelic.noticeError(ex, false);
    	}
    }
    
    private static void reloadKeys(SlashCommandCreateEvent e) {
    	
    	SlashCommandInteraction sci = e.getSlashCommandInteraction();
    	try (Connection con = Bot.getConnection()) {
    		ContentFilter cf = Bot.getFilter();
    		GetFilterData dao = new GetFilterData(con);
    		cf.init(dao.getKeywords(false), dao.getKeywords(true));
    		createResponse(sci, "Keyword list reloaded", true).respond();
    	} catch (Exception ex) {
    		log.atError().withThrowable(ex).log("Error reloading keywords - {}", ex.getMessage());
    		NewRelic.noticeError(ex, false);
    		Bot.send(ChannelName.LOG, EmbedGenerator.createError(sci.getUser().getDisplayName(sci.getServer().get()), "Reload keyword list", ex));
    	}
    }
    
    private static void sendWarning(SlashCommandCreateEvent e) {
    	
    	SlashCommandInteraction sci = e.getSlashCommandInteraction();
    	String msg = getOption(sci, "msg");
    	if (StringUtils.isEmpty(msg)) {
    		createResponse(sci, "No Message specified", true).respond();
    		return;
    	}
    	
    	// Get channel name
    	Optional<TextChannel> tc = sci.getChannel();
    	String channelName = tc.isPresent() ? tc.get().asServerChannel().get().getName() : "UNKNOWN";
    	Bot.send(ChannelName.MOD_ALERTS, EmbedGenerator.createWarning(sci.getUser().getDisplayName(sci.getServer().get()), channelName, msg));
    	createResponse(sci, "Warning Sent", true).respond();
    }
    
    private static void newFlyWithMeRequest(SlashCommandCreateEvent e) {
    	SlashCommandInteraction sci = e.getSlashCommandInteraction();
        sci.respondWithModal("fwm_modal", "Create Fly-With-Me Request", ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_dep", "Departure Field")),
        		ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_arr", "Arrival Field")), ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_net", "Requested Network")));
    }
    
    private static String getOption(SlashCommandInteraction ci, String name) {
    	SlashCommandInteractionOption opt = ci.getOptionByName(name).orElse(null);
    	return (opt == null) ? null : opt.getStringValue().orElse(null);
    }
    
    private static InteractionImmediateResponseBuilder createResponse(SlashCommandInteraction ci, String msg, boolean isEphemeral) {
    	InteractionImmediateResponseBuilder rsp = ci.createImmediateResponder();
    	rsp.setContent(msg);
    	if (isEphemeral)
    		rsp.setFlags(MessageFlag.EPHEMERAL);
    	
    	return rsp;
    }
}
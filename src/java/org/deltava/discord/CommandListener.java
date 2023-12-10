// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import java.sql.Connection;

import org.apache.logging.log4j.*;

import org.javacord.api.entity.message.component.*;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.*;

import org.deltava.beans.discord.*;

import org.deltava.dao.SetFilterData;
import org.deltava.util.StringUtils;

/**
 * A class to listen for Discord commands.
 * @author danielw
 * @author luke
 * @version 11.0
 * @since 11.0
 */

public class CommandListener implements org.javacord.api.listener.interaction.SlashCommandCreateListener {
	
	private static final Logger log = LogManager.getLogger(CommandListener.class); 

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent e) {
        SlashCommandInteraction sci = e.getSlashCommandInteraction();
        String cmdName = sci.getCommandName().toLowerCase();

        switch (cmdName) {
            case "addkey" -> addWord(e, false);
            case "dropkey" -> dropWord(e, false);
            case "flywithme" -> newFlyWithMeRequest(e);
            case "addsafe" -> addWord(e, true);
            case "dropsafe" -> dropWord(e, true);
            default -> log.info("Ignored command - {}", cmdName);
        }
    }

    @SuppressWarnings("static-method")
	private void addWord(SlashCommandCreateEvent e, boolean isSafe) {

        SlashCommandInteraction sci = e.getSlashCommandInteraction();
        String keyType = isSafe ? "safe" : "key";
        
        // Get the keyword
    	String key = getOption(sci, keyType + "word");
    	if (StringUtils.isEmpty(key)) {
    		sci.createImmediateResponder().setContent(String.format("You must supply a %s word to add", keyType)).respond();
    		return;
    	}
    	
        // Add the keyword
        boolean isAdded = Bot.getFilter().add(key, isSafe);
        if (!isAdded) {
        	sci.createImmediateResponder().setContent(String.format("%s word already exists", keyType)).respond();
            return;
        }
        
        // Write to the database
        try (Connection con = Bot.getConnection()){
        	SetFilterData wdao = new SetFilterData(con);
        	wdao.add(key, isSafe);
        	sci.createImmediateResponder().setContent(String.format("%s word %s added", keyType, key)).respond();
            Bot.send(ChannelName.ALERTS, EmbedGenerator.wordAdded(isSafe, key, sci.getUser().getDisplayName(sci.getServer().get())));
        } catch (Exception ex) {
        	log.atError().withThrowable(ex).log("Error adding {} word - {}", keyType, ex.getMessage());
        	Bot.send(ChannelName.LOG, EmbedGenerator.createError(sci.getUser().getDisplayName(sci.getServer().get()), String.format("Add %s word", keyType), ex));
        }
    }

    private static void dropWord(SlashCommandCreateEvent e, boolean isSafe) {

    	SlashCommandInteraction sci = e.getSlashCommandInteraction();
    	String keyType = isSafe ? "safe" : "key";
    	
    	String key = getOption(sci, keyType + "word");
    	if (StringUtils.isEmpty(key)) {
    		sci.createImmediateResponder().setContent(String.format("No %s word specified", keyType)).respond();
    		return;
    	}
    	
    	// Remove the keyword
    	boolean isDropped = Bot.getFilter().delete(key, isSafe);
    	if (!isDropped) {
        	sci.createImmediateResponder().setContent(String.format("%s word does not exist", keyType)).respond();
            return;
        }
    	
    	try (Connection con = Bot.getConnection()) {
    		SetFilterData wdao = new SetFilterData(con);
    		wdao.delete(key, isSafe);
        	sci.createImmediateResponder().setContent(String.format("%s word %s removed", keyType, key)).respond();
           	Bot.send(ChannelName.ALERTS, EmbedGenerator.wordDeleted(isSafe, key, sci.getUser().getDisplayName(sci.getServer().get())));
    	} catch (Exception ex) {
    		log.atError().withThrowable(ex).log("Error removing {} word - {}", keyType, ex.getMessage());
        	Bot.send(ChannelName.LOG, EmbedGenerator.createError(sci.getUser().getDisplayName(sci.getServer().get()), String.format("Remove %s word", keyType), ex));
    	}
    }

    private static void newFlyWithMeRequest(SlashCommandCreateEvent e) {
    	SlashCommandInteraction sci = e.getSlashCommandInteraction();
        sci.respondWithModal("fwm_modal", "Create Fly-With-Me Request",ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_dep", "Departure Field")),
                ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_arr", "Arrival Field")), ActionRow.of(TextInput.create(TextInputStyle.SHORT, "fwm_net", "Requested Network")));
    }
    
    private static String getOption(SlashCommandInteraction ci, String name) {
    	SlashCommandInteractionOption opt = ci.getOptionByName(name).orElse(null);
    	return (opt == null) ? null : opt.getStringValue().orElse(null);
    }
}
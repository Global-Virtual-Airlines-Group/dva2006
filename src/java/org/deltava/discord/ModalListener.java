package org.deltava.discord;

import java.time.Instant;

import org.apache.logging.log4j.*;

import org.deltava.beans.discord.ChannelName;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.interaction.ModalSubmitEvent;
import org.javacord.api.interaction.ModalInteraction;
import org.javacord.api.listener.interaction.ModalSubmitListener;

public class ModalListener implements ModalSubmitListener {
	
	private static final Logger log = LogManager.getLogger(ModalListener.class);
	
    @Override
    public void onModalSubmit(ModalSubmitEvent event) {
        ModalInteraction interaction = event.getModalInteraction();
        switch (interaction.getCustomId()) {
            case "fwm_modal" -> flyWithMeModalResponder(event);
            default -> log.info(String.format("Ignored modal - %s", interaction.getCustomId()));
        }
    }

    public static void flyWithMeModalResponder(ModalSubmitEvent event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(":airplane: New Fly With Me Request")
                .setDescription(event.getModalInteraction().getUser().getDisplayName(event.getModalInteraction().getServer().get()) + " is looking for someone to fly with them! Check out the details below.")
                .addInlineField("Departure Field", event.getModalInteraction().getTextInputValueByCustomId("fwm_dep").get())
                .addInlineField("Arrival Field", event.getModalInteraction().getTextInputValueByCustomId("fwm_arr").get())
                .addInlineField("Requested Network", event.getModalInteraction().getTextInputValueByCustomId("fwm_net").get())
                .setFooter("Fly-With-Me")
                .setTimestamp(Instant.now());
        
        Bot.send(ChannelName.FLY_WITH_ME, embed);
        event.getModalInteraction().createImmediateResponder().setContent("Request submitted!").respond();
    }
}
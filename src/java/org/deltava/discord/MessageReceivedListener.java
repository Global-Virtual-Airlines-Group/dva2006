// Copyright 2023 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.discord;

import org.apache.logging.log4j.*;

import org.javacord.api.entity.channel.*;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.sql.*;
import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.Pilot;
import org.deltava.beans.PilotStatus;
import org.deltava.beans.discord.ChannelName;

import org.deltava.dao.*;

import org.deltava.util.StringUtils;
import org.deltava.util.system.SystemData;

import org.gvagroup.jdbc.ConnectionPoolException;

public class MessageReceivedListener implements MessageCreateListener {

    private static final Logger log = LogManager.getLogger(MessageReceivedListener.class);

    @Override
    public void onMessageCreate(MessageCreateEvent e) {

    	String msg = e.getMessageContent();
    	Optional<ServerChannel> sch = e.getChannel().asServerChannel();
    	String channelName = sch.isPresent() ? sch.get().getName() : "UNKNOWN";
        if (msg.equalsIgnoreCase("done")) {
            e.getMessage().delete("Auto delete interaction message.");
            assignRoles(e);
            return;
        }
        
        // Ignore everything in #bot-interactions or sent by a bot
        if (channelName.equals(ChannelName.INTERACTIONS.getName()) || e.getMessageAuthor().isBotUser()) return;

        // Check if this is a message requesting roles
        if (channelName.equals(ChannelName.WELCOME.getName())) {
                if (e.getMessageContent().equalsIgnoreCase("!roles"))
                    registerDVA(e);
                else
                    e.getMessage().delete("Message not allowed in " + ChannelName.WELCOME);
        }
        
        // Check content
        FilterResults fr = Bot.getFilter().search(msg);
        if (!fr.isOK())
       		Bot.send(ChannelName.MOD_ALERTS, createKeywordEmbed(e, fr.getFlaggedResults()));
    }

    public static void registerDVA(MessageCreateEvent e) {
        long UUID = e.getMessageAuthor().getId();
        log.info(String.format("Registration request received [UUID = %s, Name = %s]", Long.toHexString(UUID), e.getMessageAuthor().getName()));
        if (e.getMessageAuthor().asUser().isPresent())
            e.getMessageAuthor().asUser().get().sendMessage(registerDVAEmbedBuilder(UUID));

        e.getMessage().delete("Auto-delete role request message");
    }

    public static EmbedBuilder registerDVAEmbedBuilder(long id) {
        return new EmbedBuilder()
                .setTitle(":wave: Welcome Aboard!")
                .setDescription("Welcome to the Delta Virtual Airlines Discord Server. To gain access, you must be an active member of Delta Virtual Airlines at www.deltava.org. We are a non-profit organization dedicated to flight simulation and education and are not affiliated with the real Delta Air Lines in any way. To complete your discord registration with us, follow the steps below.")
                .setThumbnail("https://www.deltava.org/img/favicon/v852/favicon-32x32.png")
                .setImage("https://www.deltava.org/img/DeltaBanner_delta_2007.png")
                .setFooter(String.format("%s Discord New Member Registration", SystemData.get("airline.code")))
                .setTimestampToNow()
                .setColor(new Color(1, 0, 100))
                .addField("Step 1: Link your Discord and DVA User Accounts", "To associate your discord account with your DVA pilot ID and receive access to the rest of the server, follow this personalized link and sign into your DVA account:\n\nhttps://www.deltava.org/discordreg.do?id=" + id)
                .addField("Step 2: Request your Roles", "Return to the #" + ChannelName.WELCOME.getName() + " channel and send \"done\" when you've completed linking your accounts and your roles will be assigned.")
                .addField("Didn't work?", "If you follow the above process and are still not able to gain access, open a help desk ticket here: https://www.deltava.org/helpdesk.do");
    }

    public static void assignRoles(MessageCreateEvent e) {

    	// Make sure user is stil here
    	if (e.getMessageAuthor().asUser().isEmpty()) return;
        User msgAuth = e.getMessageAuthor().asUser().get();
        log.info(String.format("User requested roles. [Name = %s, UUID = %s ]", msgAuth.getName(), Long.toHexString(msgAuth.getId())));
        
        Pilot p = null;
        try (Connection con = Bot.getConnection()) {
        	GetPilotDirectory pdao = new GetPilotDirectory(con);
        	p = pdao.getByIMAddress(msgAuth.getIdAsString());
        } catch (ConnectionPoolException cpe) {
        	log.error("Connection Pool Full, Aborting");
        } catch (DAOException | SQLException de) {
        	log.error(de.getMessage(), de);
        }
        
        if (p == null) {
        	log.warn(String.format("Cannot find Discord ID %s", msgAuth.getIdAsString()));
        	return;
        } else if (p.getStatus() != PilotStatus.ACTIVE) {
        	log.warn(String.format("%s (%s) Status = %s", p.getName(), p.getPilotCode(), p.getStatus()));
        	return;
        }
        
        // Check that user meets base roles
        Collection<?> reqRoles = (Collection<?>) SystemData.getObject("discord.requiredRoles");
        boolean hasReqRoles = p.getRoles().stream().anyMatch(r -> reqRoles.contains(r));
        if (!hasReqRoles) {
        	log.warn(String.format("User %s (%s) not in required Roles %s [ roles = %s ]", p.getName(), p.getPilotCode(), reqRoles, p.getRoles()));
        	return;
        }
        
        // Determine roles as appropriate
        Role r;
        if (p.getRoles().contains("HR"))
        	r = Bot.findRole(SystemData.get("discord.role.hr"));
        else if (p.getRoles().contains("PIREP"))
        	r = Bot.findRole(SystemData.get("discord.role.pirep"));
        else
        	r = Bot.findRole(SystemData.get("discord.role.default"));

        //Set the nickname and role
        String nickname = String.format("%s (%s)", p.getName(), p.getPilotCode());
        msgAuth.addRole(r);

        //Unable to do nicknames longer than 32 chars or less than 1
        if (nickname.length() > 32) {
        	Bot.send(ChannelName.ALERTS, new EmbedBuilder()
                            .setColor(Color.RED)
                            .setTitle("Unable to Assign Nickname")
                            .setFooter("User Creation Error")
                            .setTimestampToNow()
                            .setDescription(Bot.findRole("administrator").getMentionTag() + " I ran into an error when attempting to assign a nickname to the following user. Please have an administrator update the user's nickname by hand in accordance with DVA policy.")
                            .addInlineField("User", e.getMessageAuthor().getDiscriminatedName())
                            .addInlineField("Name", p.getName())
                            .addInlineField("Pilot ID", p.getPilotCode())
                            .addInlineField("Role", r.getName()));
        } else { //Nickname is valid
            msgAuth.updateNickname(e.getServer().get(), nickname);

            //Temp staff nickname notification
            Bot.send(ChannelName.ALERTS, new EmbedBuilder().setColor(Color.BLUE)
                            .setFooter("Temporary Nickname Assignment")
                            .setTitle(":exclamation: Temporary Nickname Assigned")
                            .setDescription(Bot.findRole("administrator").getMentionTag() + " I've assigned a temporary nickname to the following staff member.")
                            .addInlineField("Name", e.getMessageAuthor().getDisplayName())
                            .addInlineField("Permissions Level", r.getName())
                            .setTimestampToNow());
        }

        // Everything went well
        msgAuth.sendMessage(String.format("Your DVA account (%s) has been located and linked to this Discord user profile", p.getPilotCode()));
        msgAuth.sendMessage("If you feel that a mistake has been made, submit a ticket here: https://www.deltava.org/helpdesk.do");
        log.info(String.format("Registered User [ Name = %s, UUID = %s ]", p.getName(), Long.toHexString(e.getMessageAuthor().getId())));
    }

    public static EmbedBuilder createKeywordEmbed(MessageCreateEvent e, Collection<String> keywords) {

    	java.util.List<Role> roles = e.getMessageAuthor().asUser().get().getRoles(e.getServer().get());
        boolean isStaff = roles.stream().anyMatch(r -> r.getName().toLowerCase().contains("staff"));

        return new EmbedBuilder()
                .setTitle(":warning: Auto-Mod Keyword Detected")
                .setDescription(Bot.findRole("HR").getMentionTag() + " A possible match for the following prohibited word(s) or phrase(s) was detected in the below message: " + StringUtils.listConcat(keywords, ", "))
                .addField("Message Content", e.getMessageContent())
                .addField("In channel", "#" + e.getChannel().asServerChannel().get().getName())
                .addInlineField("User", e.getMessageAuthor().getDisplayName())
                .setColor(Color.RED)
                .setFooter("Prohibited Remarks Detected")
                .addInlineField("Role", isStaff ? "Staff" : "Pilot")
                .setTimestamp(Instant.now());
    }
}
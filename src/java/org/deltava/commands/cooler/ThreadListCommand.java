// Copyright (c) 2005 Luke J. Kolin. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;
import java.sql.Connection;

import org.deltava.beans.*;
import org.deltava.beans.cooler.*;
import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.CoolerChannelAccessControl;
import org.deltava.security.command.CoolerThreadAccessControl;

import org.deltava.util.system.SystemData;

/**
 * A web site command to display Message Threads in a Water Cooler channel.
 * @author Luke
 * @version 1.0
 * @since 1.0
 */

public class ThreadListCommand extends AbstractViewCommand {

   /**
    * Executes the command.
    * @param ctx the Command context
    * @throws CommandException if an unhandled error occurs
    */
   public void execute(CommandContext ctx) throws CommandException {

      // Get the user for the channel list
      Person p = ctx.getUser();

      // Get the default airline
      AirlineInformation airline = SystemData.getApp(SystemData.get("airline.code"));

      // Check if we want to display image threads
      boolean showImgThreads = (p instanceof Pilot) ? ((Pilot) p).getShowSSThreads() : true;

      // Get/set start/count parameters and channel name
      ViewContext vc = initView(ctx);
      String cName = (String) ctx.getCmdParameter(ID, "General Discussion");

      try {
         Connection con = ctx.getConnection();

         // Get the Pilot's airline
         GetUserData uddao = new GetUserData(con);
         if (p != null) {
            UserData usrData = uddao.get(p.getID());
            if (usrData != null)
               airline = SystemData.getApp(usrData.getAirlineCode());
         }

         // Get the channel DAO and the list of channels
         GetCoolerChannels dao = new GetCoolerChannels(con);
         Channel ch = dao.get(cName);
         dao.setQueryMax(0);
         ctx.setAttribute("channel", ch, REQUEST);
         ctx.setAttribute("channels", dao.getChannels(airline, ctx.getRoles()), REQUEST);

         // Initialize the channel access controller
         CoolerChannelAccessControl cAccess = new CoolerChannelAccessControl(ctx, ch);
         cAccess.validate();
         ctx.setAttribute("channelAccess", cAccess, REQUEST);

         // Get the Message Threads for this channel - add by 10% for filtering
         GetCoolerThreads dao2 = new GetCoolerThreads(con);
         dao2.setQueryStart(vc.getStart());
         dao2.setQueryMax(Math.round(vc.getCount() * 1.25f));

         // Initialize the access controller and the set to store pilot IDs
         CoolerThreadAccessControl ac = new CoolerThreadAccessControl(ctx);
         
         // Save the channel name if showing all
         if (ch == null)
            ctx.setAttribute("channelName", "All Discussions", REQUEST);

         // Get either by channel or all; now filter by role
         Set<Integer> pilotIDs = new HashSet<Integer>();
         List<MessageThread> threads = (ch == null) ? dao2.getAll(showImgThreads) : dao2.getByChannel(cName, showImgThreads);
         for (Iterator<MessageThread> i = threads.iterator(); i.hasNext();) {
            MessageThread thread = i.next();

            // Get this thread's channel and see if we can read it
            Channel c = dao.get(thread.getChannel());
            ac.updateContext(thread, c);
            ac.validate();

            // If we cannot read the thread, remove it from the results, otherwise load the pilot profiles
            if (!ac.getCanRead()) {
               i.remove();
            } else {
               pilotIDs.add(new Integer(thread.getAuthorID()));
               pilotIDs.add(new Integer(thread.getLastUpdateID()));
            }
         }

         // Get the location of all the Pilots
         UserDataMap udm = uddao.get(pilotIDs);
         ctx.setAttribute("userData", udm, REQUEST);

         // Get the authors for the last post in each channel
         Map<Integer, Person> authors = new HashMap<Integer, Person>();
         GetPilot pdao = new GetPilot(con);
         GetApplicant adao = new GetApplicant(con);
         for (Iterator<String> i = udm.getTableNames().iterator(); i.hasNext();) {
            String tableName = i.next();
            if (tableName.endsWith("APPLICANTS")) {
               authors.putAll(adao.getByID(udm.getByTable(tableName), tableName));
            } else {
               authors.putAll(pdao.getByID(udm.getByTable(tableName), tableName));
            }
         }

         // Get the pilot IDs in the returned threads
         ctx.setAttribute("pilots", authors, REQUEST);

         // Save in the view context - If threads is still larger than the max, then cut it off
         vc.setResults((threads.size() > vc.getCount()) ? threads.subList(0, vc.getCount()) : threads);
      } catch (DAOException de) {
         throw new CommandException(de);
      } finally {
         ctx.release();
      }

      // Forward to JSP
      CommandResult result = ctx.getResult();
      result.setURL("/jsp/cooler/threadList.jsp");
      result.setSuccess(true);
   }
}
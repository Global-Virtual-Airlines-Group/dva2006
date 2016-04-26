// Copyright 2006, 2007, 2010, 2011, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.academy;

import java.util.*;
import java.sql.Connection;
import java.time.Instant;

import org.deltava.beans.*;
import org.deltava.beans.academy.*;
import org.deltava.beans.system.AirlineInformation;

import org.deltava.commands.*;
import org.deltava.dao.*;
import org.deltava.mail.*;

import org.deltava.security.command.CourseAccessControl;

import org.deltava.util.CollectionUtils;

/**
 * A Web Site Command to post comments in a Flight Academy Course.
 * @author Luke
 * @version 7.0
 * @since 1.0
 */

public class CourseCommentCommand extends AbstractCommand {
	
	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {
		
		// Create the messaging context
		MessageContext mctxt = new MessageContext();
		mctxt.addData("user", ctx.getUser());

		Collection<Pilot> addrs = null;
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the Course
			GetAcademyCourses dao = new GetAcademyCourses(con);
			Course c = dao.get(ctx.getID());
			if (c == null)
				throw notFoundException("Invalid Course - " + ctx.getID());
			
			// Check our access
			CourseAccessControl access = new CourseAccessControl(ctx, c);
			access.validate();
			if (!access.getCanComment())
				throw securityException("Not Authorized");
			
			// Create the comment bean
			CourseComment cc = new CourseComment(c.getID(), ctx.getUser().getID());
			cc.setCreatedOn(Instant.now());
			cc.setText(ctx.getParameter("msgText"));
			
			// Get the DAO and the message template
			GetMessageTemplate mtdao = new GetMessageTemplate(con);
			mctxt.setTemplate(mtdao.get("COURSECOMMENT"));
			mctxt.addData("comment", cc);
			mctxt.addData("course", c);
			
			// Get instructors
			GetUserData uddao = new GetUserData(con);
			GetPilotDirectory pddao = new GetPilotDirectory(con);
			Map<Integer, Pilot> ins = new HashMap<Integer, Pilot>();
			for (AirlineInformation ai : uddao.getAirlines(true).values()) {
				Map<Integer, Pilot> usrs = CollectionUtils.createMap(pddao.getByRole("Instructor", ai.getDB()), "ID");
				Map<Integer, Pilot> admins = CollectionUtils.createMap(pddao.getByRole("AcademyAdmin", ai.getDB()), "ID");
				ins.putAll(usrs);
				ins.putAll(admins);
			}
			
			// Get Pilot IDs from comments - only add instructors
			boolean hasINS = (c.getInstructorID() != 0);
			Collection<Integer> IDs = new HashSet<Integer>();
			IDs.add(new Integer(c.getPilotID()));
			if (hasINS)
				IDs.add(new Integer(c.getInstructorID()));
			for (Iterator<CourseComment> i = c.getComments().iterator(); i.hasNext(); ) {
				CourseComment comment = i.next();
				Integer id = new Integer(comment.getAuthorID());
				if (ins.containsKey(id))
					IDs.add(id);
			}
			
			// Load Pilot Information
			GetPilot pdao = new GetPilot(con);
			UserDataMap udm = uddao.get(IDs);
			addrs = new ArrayList<Pilot>(pdao.get(udm).values());
			
			// Add additional instructors
			for (Iterator<Map.Entry<Integer, Pilot>> i = ins.entrySet().iterator(); i.hasNext(); ) {
				Map.Entry<Integer, Pilot> me = i.next();
				if (!IDs.contains(me.getKey()) || !hasINS || me.getValue().isInRole("AcademyAdmin"))
					addrs.add(me.getValue());
			}

			// Get the write DAO and write the comment
			SetAcademy wdao = new SetAcademy(con);
			wdao.comment(cc);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
        // Create the e-mail message
        Mailer mailer = new Mailer(ctx.getUser());
        mailer.setContext(mctxt);
        mailer.send(addrs);
		
		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("course", "read", ctx.getID());
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}
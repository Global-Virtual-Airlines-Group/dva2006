// Copyright 2008, 2009, 2016 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.cooler;

import java.util.*;

import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.util.CollectionUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to update the Thread Notification list.
 * @author Luke
 * @version 7.0
 * @since 2.1
 */

public class NotificationListUpdateCommand extends AbstractCommand {

	/**
	 * Executes the command.
	 * @param ctx the Command context
	 * @throws CommandException if an unhandled error occurs
	 */
	@Override
	public void execute(CommandContext ctx) throws CommandException {

		// Get the threads to clear
		Collection<String> IDs = ctx.getParameters("threadID");
		if (!CollectionUtils.isEmpty(IDs)) {
			try {
				SetCoolerNotification ndao = new SetCoolerNotification(ctx.getConnection());
				ctx.startTX();
				for (String id : IDs) {
					try {
						int threadID = StringUtils.parseHex(id);
						ndao.delete(threadID, ctx.getUser().getID());
					} catch (NumberFormatException nfe) {
						// empty
					}
				}

				ctx.commitTX();
			} catch (DAOException de) {
				ctx.rollbackTX();
				throw new CommandException(de);
			} finally {
				ctx.release();
			}
		}

		// Forward to the JSP
		CommandResult result = ctx.getResult();
		result.setURL("notifythreads.do");
		result.setType(ResultType.REDIRECT);
		result.setSuccess(true);
	}
}
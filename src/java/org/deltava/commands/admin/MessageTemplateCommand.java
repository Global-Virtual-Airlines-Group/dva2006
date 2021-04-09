// Copyright 2005, 2006, 2016, 2017, 2021 Global Virtual Airlines Group. All Rights Reserved.
package org.deltava.commands.admin;

import java.util.*;
import java.sql.Connection;
import java.util.stream.Collectors;

import org.deltava.beans.AuditLog;
import org.deltava.beans.system.*;
import org.deltava.commands.*;
import org.deltava.dao.*;

import org.deltava.security.command.MessageAccessControl;

import org.deltava.util.BeanUtils;
import org.deltava.util.CollectionUtils;
import org.deltava.util.EnumUtils;
import org.deltava.util.StringUtils;

/**
 * A Web Site Command to edit Message Templates.
 * @author Luke
 * @version 10.0
 * @since 1.0
 */

public class MessageTemplateCommand extends AbstractAuditFormCommand {

	/**
	 * Callback method called when editing the template.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execEdit(CommandContext ctx) throws CommandException {
		
		// Check if we're creating a new template
		String id = (String) ctx.getCmdParameter(Command.ID, null);
		boolean isNew = StringUtils.isEmpty(id);
		
		// Check our access
		MessageAccessControl access = new MessageAccessControl(ctx, null);
		access.validate();
		boolean isOK = isNew ? access.getCanCreate() : access.getCanEdit();
		if (!isOK)
			throw securityException("Cannot create/edit Message Template");
		
		// Get the command results
		CommandResult result = ctx.getResult();
		
		// If we're creating a new template, redirect to the JSP
		if (isNew) {
			ctx.setAttribute("access", access, REQUEST);
			result.setURL("/jsp/admin/msgTemplate.jsp");
			result.setSuccess(true);
			return;
		}

		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the message
			GetMessageTemplate dao = new GetMessageTemplate(con);
			MessageTemplate msg = dao.get(ctx.getDB(), id);
			if (msg == null)
				throw notFoundException("Invalid Message Template - " + id);
			
			// Update our access to calculate deletion rights
			access = new MessageAccessControl(ctx, msg);
			access.validate();
			
			// Save the template in the request
			ctx.setAttribute("access", access, REQUEST);
			ctx.setAttribute("template", msg, REQUEST);
			ctx.setAttribute("ctxValues", CollectionUtils.nonNull(msg.getNotifyContext()), REQUEST);
			readAuditLog(ctx, msg);
		} catch (DAOException de) {
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the JSP
		result.setURL("/jsp/admin/msgTemplate.jsp");
		result.setSuccess(true);
	}
	
	/**
	 * Callback method called when saving the template.
	 * @param ctx the Command context
	 * @throws CommandException if an error occurs
	 */
	@Override
	protected void execSave(CommandContext ctx) throws CommandException {

		// Check if we're creating a new template
		boolean isNew = (ctx.getCmdParameter(Command.ID, null) == null);
		try {
			Connection con = ctx.getConnection();
			
			// Get the DAO and the existing template
			MessageTemplate mt = null; MessageTemplate omt = null;
			if (!isNew) {
				GetMessageTemplate dao = new GetMessageTemplate(con);
				mt = dao.get((String) ctx.getCmdParameter(Command.ID, null));
				if (mt == null)
					throw notFoundException("Invalid Message Template - " + ctx.getCmdParameter(Command.ID, null));
				
				// Check our access
				MessageAccessControl access = new MessageAccessControl(ctx, mt);
				access.validate();
				if (!access.getCanEdit())
					throw securityException("Cannot edit Message Template");
				
				omt = BeanUtils.clone(mt);
			} else {
				// Check our access
				MessageAccessControl access = new MessageAccessControl(ctx, mt);
				access.validate();
				if (!access.getCanCreate())
					throw securityException("Cannot create Message Template");
				
				mt = new MessageTemplate(ctx.getParameter("name"));
			}
			
			// Load from the request
			mt.setSubject(ctx.getParameter("subject"));
			mt.setDescription(ctx.getParameter("desc"));
			mt.setBody(ctx.getParameter("body"));
			mt.setIsHTML(Boolean.valueOf(ctx.getParameter("isHTML")).booleanValue());
			mt.setNotificationTTL(StringUtils.parse(ctx.getParameter("ttl"), 3600));
			mt.setNotifyContext(StringUtils.isEmpty(ctx.getParameter("ctx")) ? null : ctx.getParameter("ctx"));
			mt.setActionTypes(ctx.getParameters("actions", Collections.emptyList()).stream().map(at -> EnumUtils.parse(NotifyActionType.class, at, null)).filter(Objects::nonNull).collect(Collectors.toList()));
			
			// Check audit log
			Collection<BeanUtils.PropertyChange> delta = BeanUtils.getDelta(omt, mt);
			AuditLog ae = AuditLog.create(mt, delta, ctx.getUser().getID());
			
			// Start transaction
			ctx.startTX();
			
			// Get the write DAO and update the template
			SetMessageTemplate wdao = new SetMessageTemplate(con);
			wdao.write(mt);
			
			// Write audit log
			writeAuditLog(ctx, ae);
			ctx.commitTX();
		} catch (DAOException de) {
			ctx.rollbackTX();
			throw new CommandException(de);
		} finally {
			ctx.release();
		}
		
		// Forward to the Message Templates
		CommandResult result = ctx.getResult();
		result.setType(ResultType.REDIRECT);
		result.setURL("msgtemplates", null, null);
		result.setSuccess(true);
	}	
	
	/**
	 * Callback method called when reading the template. <i>NOT IMPLEMENTED</i>
	 * @param ctx the Command context
	 */
	@Override
	protected void execRead(CommandContext ctx) throws CommandException {
		execEdit(ctx);
	}
}
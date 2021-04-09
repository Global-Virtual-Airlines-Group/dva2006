<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<c:if test="${!empty template}"><title>Message Template - ${template.name}</title></c:if>
<c:if test="${empty template}"><title>New Message Template</title></c:if>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.name, l:4, t:'Template Name'});
	golgotha.form.validate({f:f.subject, l:6, t:'E-Mail Subject'});
	golgotha.form.validate({f:f.desc, l:6, t:'Template Description'});
	golgotha.form.validate({f:f.body, l:15, t:'E-Mail Text'});
	golgotha.form.validate({f:f.ttl, min:5, t:'Push Notification TTL'});
	golgotha.form.submit(f);
	return true;
};

golgotha.local.loadObjects = function(txt) {
	const v = txt.value; const results = [];
	let pos = v.indexOf('$\{');
	while (pos > -1) {
		let pos2 = v.indexOf('}', pos+1);
		let pos3 = v.indexOf('.', pos+1);
		if ((pos3 > pos) && (pos3 < pos2)) {
			const varName = v.substring(pos + 2,pos3);
			results.push(varName);
		} else if (pos2 > pos) {
			const varName = v.substring(pos + 2,pos2);
			results.push(varName);
		}

		pos = v.indexOf('$\{', Math.max(pos2+1, pos+1));		
	}

	return results.filter(function(v, idx, self) { return (self.indexOf(v) == idx); });
};

golgotha.local.updateObjects = function() {
	const f = document.forms[0];
	const v = golgotha.form.getCombo(f.ctx);
	const opts = golgotha.local.loadObjects(f.body);
	if ((v) && (opts.indexOf(v) == -1)) opts.push(v);
	opts.sort();
	f.ctx.options.length = 0;
	f.ctx.add(new Option('[ NONE ]', ''));
	for (var x = 0; x < opts.length; x++) {
		f.ctx.add(new Option(opts[x]));
		if (opts[x] == v)
			f.ctx.selectedIndex = (x+1);
	}
};

golgotha.onDOMReady(golgotha.local.updateObjects);
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="actionTypes" className="org.deltava.beans.system.NotifyActionType" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form method="post" action="msgtemplate.do" linkID="${empty template ? null : template.name}" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Template Title Bar -->
<tr class="title caps">
 <td colspan="2">E-MAIL MESSAGE TEMPLATE</td>
</tr>

<!-- Message Template Data -->
<tr>
 <td class="label">Template Name</td>
<c:if test="${!empty template}">
 <td class="data pri bld">${template.name}</td>
</c:if>
<c:if test="${empty template}">
 <td class="data"><el:text name="name" className="pri bld" idx="*" required="true" size="20" max="32" value="${template.name}" /></td>
</c:if>
</tr>
<tr>
 <td class="label">E-Mail Subject</td>
 <td class="data"><el:text name="subject" idx="*" size="48" max="64" required="true" value="${template.subject}" /></td>
</tr>
<tr>
 <td class="label top">Push Notification Actions</td>
 <td class="data"><el:check name="actions" options="${actionTypes}" checked="${template.actionTypes}" cols="5" width="135" newLine="true" /></td>
</tr>
<tr>
 <td class="label">Push Notification TTL</td>
 <td class="data"><el:text name="ttl" idx="*" size="4" max="6" required="true" className="bld" value="${template.notificationTTL}" /></td>
</tr>
<tr>
 <td class="label">Push Context Object</td>
 <td class="data"><el:combo name="ctx" idx="*" size="1" options="${ctxValues}" value="${template.notifyContext}" /></td>
</tr>
<tr>
 <td class="label">Template Description</td>
 <td class="data"><el:text name="desc" idx="*" size="64" max="128" required="true" value="${template.description}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isHTML" idx="*" value="true" checked="${template.isHTML}" label="Send E-Mail message as HTML" /></td>
</tr>
<tr>
 <td class="label top">Template Text</td>
 <td class="data"><el:textbox name="body" idx="*" width="80%" className="req" height="8" onBlur="void golgotha.local.updateObjects()" resize="true">${template.body}</el:textbox></td>
</tr>
<%@ include file="/jsp/auditLog.jspf" %>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE MESSAGE TEMPLATE" /><c:if test="${access.canDelete}">&nbsp;<el:cmdbutton url="msgtemplatedelete" linkID="${template.name}" label="DELETE MESSAGE TEMPLATE" /></c:if> </td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

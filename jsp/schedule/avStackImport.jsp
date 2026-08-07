<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title>AviationStack Schedule Import</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:googleAnalytics />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
<script nonce="${contentSecurity.nonce}">
golgotha.local.refresh = function() {
	const f = document.forms[0];
	const p = fetch('avstatus.ws', {signal:AbortSignal.timeout(2500)});
	p.then(function(rsp) {
		if (!rsp.ok) return false;
		window.setTimeout(golgotha.local.refresh, 1000);
		rsp.json().then(function(js) {
			let msgs = '';
			js.entries.forEach(function(msg) { msgs += msg; msgs += '\n'; });
			f.msgs.value = msgs;
		});
	});
};

golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.isSubmitted = true;
	return true;
};

golgotha.onDOMReady(function() {
	window.setTimeout(golgotha.local.refresh, 1000);
	return true;
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="avimport.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AVIATIONSTACK SCHEDULE IMPORT</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="doImport" value="true" label="Import AviationStack Data" /></td>
</tr>
<tr id="updateRow" style="display:none;">
 <td class="label top">Messages</td>
 <td class="data"><el:textbox name="msgs" width="85%" height="5" readOnly="true" resize="true"></el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="LOAD AVIATIONSTACK SCHEDULE" /></td>
</tr>
</el:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>

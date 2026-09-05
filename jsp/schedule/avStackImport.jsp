<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
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
		const tmrID = window.setTimeout(golgotha.local.refresh, 2500);
		rsp.json().then(function(js) {
			const pr = document.getElementById('msgTitle');
			for (var x = 0; x < js.entries.length; x++) {
				const er = document.getElementById('msg-' + x);
				if (er) continue;

				const d = js.entries[x];
				const r = document.createElement('tr');
				r.setAttribute('id', 'msg-' + x);
				r.setAttribute('class', 'msgs');
				r.appendChild(golgotha.util.createElement('td', d.time, 'label top'));
				r.appendChild(golgotha.util.createElement('td', d.level, 'lvl top ' + d.level.toLowerCase()));
				r.appendChild(golgotha.util.createElement('td', d.msg, 'data small'));
				pr.parentNode.insertBefore(r, null);
			}

			golgotha.util.display('msgTitle', true);
			if (js.isComplete)
				window.clearTimeout(tmrID);
		});
	});
};

golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.isSubmitted = true;
	return true;
};

golgotha.onDOMReady(function() {
	window.setTimeout(golgotha.local.refresh, 1500);
	return true;
});
</script>
<style nonce="${contentSecurity.nonce}">
td.lvl.info {
	color: #10a020;
}

td.lvl.warn {
	color: #ff8040;
}

td.lvl.error {
	color: #a01010;
}
</style>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="avdl.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="3">AVIATIONSTACK SCHEDULE IMPORT</td>
</tr>
<c:if test="${!empty lastImport}">
<tr>
 <td class="label">Previous Import</td>
 <td class="data pri bld" colspan="2"><fmt:date date="${lastImport}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data" colspan="2"><el:box name="doImport" value="true" label="Import AviationStack Data" /><content:hasmsg>><br />
<el:box name="doForce" value="true" label="Force AviationStack re-Import" /></content:hasmsg>></td>
</tr>
<content:hasmsg>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data" colspan="2"><span class="error bld caps"><content:sysmsg /></span></td>
</tr>
</content:hasmsg>
<tr id="msgTitle" class="title caps" style="display:none;">
 <td colspan="3">IMPORT STATUS MESSAGES</td>
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

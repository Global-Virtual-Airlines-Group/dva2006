<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Raw Schedule Export</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="fileSaver" />
<content:googleAnalytics />
<content:newRelic>
<content:cspHeader /></content:newRelic>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:enum var="fmtTypes" className="org.deltava.beans.schedule.ScheduleFormat" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="schedexport.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate,this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>RAW SCHEDULE DATA DOWNLOAD</td>
</tr>
<tr>
 <td class="label">Schedule Source</td>
 <td class="data"><el:check name="src" idx="*" width="255" options="${srcInfo}" cols="5" newLine="true" /></td>
</tr>
<tr>
 <td class="label">Schedule Format</td>
 <td class="data"><el:combo name="fmt" idx="*" size="1" options="${fmtTypes}" firstEntry="[ SELECT FORMAT ]" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="DOWNLOAD RAW SCHEDULE DATA" /></td>
</tr>
</el:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
<script nonce="${contentSecurity.nonce}">
golgotha.local.validate = function(f) {
	golgotha.form.validate({f:f.src,min:1,t:'Raw Schedule Source'});
	golgotha.form.validate({f:f.fmt,t:'Schedule Export Format'});
	const srcs = [];
	f.src.forEach(function(cb) { if (cb.checked) srcs.push(cb.value); });
	golgotha.util.disable('ExportButton', true);
	golgotha.local.download(srcs, golgotha.form.getCombo(f.fmt));
	return false;
};

golgotha.local.download = function(srcs, fmt) {
	const fd = new FormData();
	fd.set('src', srcs.join());
	fd.set('fmt', fmt);

	const p = fetch('/schedexport.ws', {method:'post', body:fd, signal:AbortSignal.timeout(27500)});
	p.then(function(rsp) {
		if (!rsp.ok) {
			golgotha.form.showDialogMessage('Error exporting schedule data');
			golgotha.util.disable('ExportButton', false);
			return false;
		}

		const fn = rsp.headers.get('X-Schedule-Name');
		rsp.blob().then(function(b) {
			saveAs(b, fn);
			golgotha.util.disable('ExportButton', false);
		});
	});
};
</script>
</body>
</html>

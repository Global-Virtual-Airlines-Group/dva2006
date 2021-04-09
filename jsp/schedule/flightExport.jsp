<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
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
<script>
golgotha.local.validate = function(f) {
	golgotha.form.validate({f:f.src,min:1,t:'Raw Schedule Source'});
	const srcs = [];
	f.src.forEach(function(cb) { if (cb.checked) srcs.push(cb.value); });
	golgotha.local.download(srcs);
	golgotha.util.disable('ExportButton', true);
	return false;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="schedexport.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>RAW SCHEDULE DATA DOWNLOAD</td>
</tr>
<tr>
 <td class="label">Schedule Format</td>
 <td class="data"><el:check name="src" idx="*" width="230" options="${srcInfo}" cols="5" newLine="true" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="DOWNLOAD RAW SCHEDULE DATA" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
<script>
golgotha.local.download = function(srcs) {
	const xmlreq = new XMLHttpRequest();
	xmlreq.open('post', '/schedexport.ws', true);
	xmlreq.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	xmlreq.responseType = 'blob';
	xmlreq.onreadystatechange = function() {
		if ((xmlreq.readyState != 4) || (xmlreq.status != 200)) return false;
		const ct = xmlreq.getResponseHeader('Content-Type');
		const b = new Blob([xmlreq.response], {type:ct.substring(0, ct.indexOf(';')), endings:'native'});
		saveAs(b, xmlreq.getResponseHeader('X-Schedule-Name'));
		golgotha.util.disable('ExportButton', false);
		return true;
	};

	xmlreq.send('src=' + srcs.join());
	return true;
};
</script>
</body>
</html>

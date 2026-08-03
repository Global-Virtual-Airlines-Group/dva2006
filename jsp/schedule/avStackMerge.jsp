<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> AviationStack Schedule Merge</title>
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
golgotha.local.exts = ['.json','.json.gz','.json.bz2','.json.xz'];
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	let isOK = false;
	const fName = f.jsonData.value.substring(f.jsonData.value.lastIndexOf('\\') + 1).toLowerCase();
	for (var x = 0; x < golgotha.local.exts.length && !isOK; x++)
		isOK |= (fName.endsWith(golgotha.local.exts[x]));
	
	golgotha.form.submit(f);
	return true;
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
<el:form action="avmerge.do" method="post" op="save" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">AVIATIONSTACK SCHEDULE MERGE</td>
</tr>
<tr>
 <td class="label">Upload Data File</td>
 <td class="data"><el:file name="jsonData" idx="*" className="small req" size="80" max="144" />&nbsp;<span class="small ita">AviationStack schedule data must be in JSON Format.</span></td>
</tr>
<c:if test="${isMerge}">
<tr class="title caps">
 <td colspan="2">SCHEDULE MERGE RESULTS</td>
</tr>
<tr>
 <td class="label">Entries Merged</td>
 <td class="data pri bld"><fmt:int value="${entryCount}" /> Schedule Entries</td>
</tr>
<c:if test="${!empty status.errorMessages}">
<tr>
 <td class="label top">Error Messages</td>
 <td class="data"><c:forEach var="msg" items="${status.errorMessages}" varStatus="hasMore">${msg}<c:if test="${!hasMore.last}"><br></c:if></c:forEach></td>
</tr>
</c:if>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="UPLOAD AVIATIONSTACK SCHEDULE DATA" /></td>
</tr>
</el:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>

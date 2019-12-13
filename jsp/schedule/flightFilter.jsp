<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule Filter</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	
	
	
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
<el:form action="schedfilter.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<c:if test="${!empty status}">
<tr class="title caps">
 <td colspan="2">FLIGHT SCHEDULE IMPORT STATUS</td>
</tr>
<tr>
 <td class="label top">Import Results</td>
 <td class="data"><fmt:int value="${importCount}" /> RawSchedule Entries loaded from <span class="sec bld">${status.source.description}</span></td>
</tr>
<c:if test="${!empty status.messages}">
<tr>
 <td class="label top">Import Messages</td>
 <td class="data small"><c:forEach var="msg" items="${status.messages}">
 ${msg}<br /></c:forEach></td>
</tr>
</c:if>
</c:if>
<tr class="title caps">
 <td colspan="2">FLIGHT SCHEDULE FILTER / IMPORT</td>
</tr>
<tr>
 <td class="label">Sources</td>
 <td class="data"><el:check width="120" options="${sources}" /></td>
</tr>
<tr>
 <td class="label">Effective Date</td>
 <td class="data"><el:text name="effDate" size="10" max="10" required="true" value="${today}" /></td>
</tr>
<tr>
 <td class="label">Schedule Purge</td>
 <td class="data"><el:box name="doPurge" idx="*" value="true" checked="true" label="Purge existing Schedule Entries" /></td>
</tr>
<tr>
 <td class="label top">Import Options</td>
 <td class="data"><el:box name="canPurge" idx="*" value="true" checked="true" label="Mark imported Schedule Entries as Purgeable" /><br />
<el:box name="updateAirports" idx="*" value="true" checked="true" label="Update Airport/Airline mappings in database" /></td>
</tr>
</el:table>

<!-- Button bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
<el:text name="doImport" value="true" type="hidden" readOnly="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

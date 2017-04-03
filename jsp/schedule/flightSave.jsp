<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Flight Schedule Import</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
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
<el:form action="schedsave.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">FLIGHT SCHEDULE DATA UPLOAD - STEP TWO</td>
</tr>
<tr>
 <td class="label top">Import Results</td>
 <td class="data"><fmt:int value="${fn:sizeof(sessionScope.entries)}" /> Schedule Entries loaded
<c:if test="${innovataCache}"><br />
<span class="warn small caps">Cached Innovata, LLC data used - no new data available</span></c:if></td>
</tr>
<c:if test="${!empty errors}">
<tr>
 <td class="label top">Import Errors</td>
 <td class="data small"><c:forEach var="error" items="${sessionScope.errors}">
${error}<br />
</c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label">Schedule Purge</td>
 <td class="data"><el:box name="doPurge" idx="*" value="true" checked="true" label="Purge existing Schedule Entries" /></td>
</tr>
<tr>
 <td class="label top">Import Options</td>
 <td class="data"><el:box name="canPurge" idx="*" value="true" checked="true" label="Mark imported Schedule Entries as Purgeable" /><br />
<el:box name="isHistoric" idx="*" value="true" label="Mark imported Schedule Entries as Historic Flights" /><br />
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

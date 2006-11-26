<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Flight Schedule Import</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

setSubmit();
disableButton('SaveButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="schedsave.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">FLIGHT SCHEDULE DATA UPLOAD - STEP TWO</td>
</tr>
<tr>
 <td class="label" valign="top">Import Results</td>
 <td class="data"><fmt:int value="${fn:sizeof(sessionScope.entries)}" /> Schedule Entries loaded
<c:if test="${innovataCache}"><br />
<span class="warn small caps">Cached Innovata, LLC data used - no new data available</span></c:if></td>
</tr>
<c:if test="${!empty errors}">
<tr>
 <td class="label" valign="top">Import Errors</td>
 <td class="data small"><c:forEach var="error" items="${sessionScope.errors}">
${error}<br />
</c:forEach></td>
</tr>
</c:if>
<tr>
 <td class="label">Schedule Purge</td>
 <td class="data"><el:box name="doPurge" idx="*" value="true" label="Purge existing Schedule Entries" /></td>
</tr>
<tr>
 <td class="label" valign="top">Import Options</td>
 <td class="data"><el:box name="canPurge" idx="*" value="true" label="Mark imported Schedule Entries as Purgeable" /><br />
<el:box name="isHistoric" idx="*" value="true" label="Mark imported Schedule Entries as Historic Flights" /><br />
<el:box name="updateAirports" idx="*" value="true" label="Update Airport/Airline mappings in database" /></td>
</tr>
</el:table>

<!-- Button bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="SaveButton" type="submit" className="BUTTON" label="SAVE FLIGHT SCHEDULE" /></td>
</tr>
</el:table>
<el:text name="doImport" value="true" type="hidden" readOnly="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>

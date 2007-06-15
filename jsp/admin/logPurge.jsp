<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> System Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="datePicker" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.purgeDate, 10, 'Purge Date')) return false;

setSubmit();
disableButton('PurgeButton');
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="logNames" name="log.names" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="systemlogpurge.do" method="post" validate="return validate(this)">
<el:table className="form" space="default" pad="default">
<tr class="title caps">
 <td colspan="2">PURGE SYSTEM LOG ENTRIES</td>
</tr>
<tr>
 <td class="label">Log Name</td>
 <td class="data"><el:combo name="logName" idx="*" size="1" options="${logNames}" value="${param.logName}" /></td>
</tr>
<tr>
 <td class="label">Priority</td>
 <td class="data"><el:combo name="priority" idx="*" size="1" options="${priorities}" value="${param.priority}" /></td>
</tr>
<tr>
 <td class="label">Purge Date</td>
 <td class="data"><el:text name="purgeDate" idx="*" size="10" max="10" value="${param.purgeDate}" />
&nbsp;<el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].purgeDate')" /></td>
</tr>
<c:if test="${!empty rowsDeleted}">
<tr>
 <td class="pri bld" colspan="2"><fmt:int value="${rowsDeleted}" /> LOG ENTRIES REMOVED FROM THE DATABASE</td>
</tr>
</c:if>
</el:table>

<!-- Button Bar -->
<el:table className="bar" space="default" pad="default">
<tr>
 <td><el:button ID="PurgeButton" type="submit" className="BUTTON" label="PURGE SYSTEM LOG ENTRIES" /></td>
</tr>
</el:table>
</el:form>
<br />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

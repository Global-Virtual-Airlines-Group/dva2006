<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Oceanic Routes</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<c:if test="${access.canDelete && (!empty viewContext.results)}">
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
</c:if>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="routes.do" method="post" validate="return validate(this)">
<view:table className="view" pad="default" space="default" cmd="routes">
<!-- Table Header Bar -->
<tr class="title caps">
 <td width="15%">DATE</td>
 <td width="15%">&nbsp;</td>
 <td width="10%">TYPE</td>
 <td>DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:forEach var="route" items="${viewContext.results}">
<tr>
 <td class="pri bld"><fmt:date fmt="d" date="${route.date}" /></td>
 <td><el:cmdbutton url="route" op="${route.type}" linkID="${fn:dateFmt(route.date, 'MMddyyyy')}" label="VIEW ROUTE" /></td>
 <td class="sec bld">${route.typeName}</td>
 <td class="left">${route.typeName} routes for <fmt:date fmt="d" date="${route.date}" />, courtesy of ${route.source}</td>
</tr>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
<c:if test="${access.canDelete && (!empty viewContext.results)}">
 <td colspan="3"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
 <td colspan="2">PURGE BEFORE <el:text name="purgeDate" idx="*" size="10" max="10" />
 <el:button className="BUTTON" label="CALENDAR" onClick="void show_calendar('forms[0].purgeDate')" />
 <el:cmdbutton ID="PurgeButton" url="routepurge" post="true" label="PURGE OCEANIC ROUTES" /></td>
</c:if>
<c:if test="${!access.canDelete || (empty viewContext.results)}">
 <td colspan="5"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</c:if>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Command Statistics</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function setSort(combo)
{
var sortOpt = combo.options[combo.selectedIndex].value;
self.location = '/cmdstats.do?sortBy=' + sortOpt;
return true;
}

function validate(form)
{
if (!checkSubmit()) return false;
if (!validateNumber(form.purgeDays, 2, 'Days of Log Entries to keep')) return false;

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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="cmdstatpurge.do" method="post" validate="return validate(this)">
<el:table className="view" space="default" pad="default">
<!-- Table Header Bar -->
<tr class="title">
 <td colspan="4" class="caps">COMMAND INVOCATION STATISTICS</td>
 <td colspan="4" class="right"><el:combo name="sortBy" idx="*" size="1" options="${sortOptions}" value="${sortType}" onChange="void setSort(this)" /></td>
</tr>

<!-- Table Legend Bar -->
<tr class="title caps">
 <td width="15%">COMMAND NAME</td>
 <td width="10%">INVOKED</td>
 <td width="10%">SUCCESSFUL</td>
 <td width="10%">PERCENT</td>
 <td width="10%">AVG. TIME</td> 
 <td width="14%">AVG. BACK END</td>
 <td width="12%">MAX TIME</td>
 <td>MAX BACK END</td>
</tr>

<!-- Table Statistics Data -->
<c:forEach var="stat" items="${stats}">
<view:row entry="${stat}">
 <td class="pri bld">${stat.name}</td>
 <td class="sec"><fmt:int value="${stat.count}" /></td>
 <td><fmt:int value="${stat.successCount}" /></td>
 <td><fmt:dec value="${stat.successCount / stat.count * 100}" />%</td>
 <td class="pri bld"><fmt:int value="${stat.avgTime}" /> ms</td>
 <td><fmt:int value="${stat.avgBackEndTime}" /> ms</td>
 <td class="bld"><fmt:int value="${stat.maxTime}" /> ms</td>
 <td><fmt:int value="${stat.maxBackEndTime}" /> ms</td>
</view:row>
</c:forEach>

<!-- Bottom Row -->
<tr class="title caps">
 <td colspan="8">PURGE ENTRIES MORE THAN <el:text name="purgeDays" idx="*" size="1" max="3" value="" />
 DAYS OLD <el:button ID="PurgeButton" type="submit" className="BUTTON" label="PURGE COMMAND LOG" /></td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

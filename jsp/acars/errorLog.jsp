<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Client Error Logs</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
var filterType = (f.viewType) ? f.viewType.selectedIndex : 0;
golgotha.form.validate({f:f.viewType, t:'Filter Type'});
if (filterType == 2)
 	golgotha.form.validate({f:f.author, t:'Error Report Author'});
else if (filterType == 3)
	golgotha.form.validate({f:f.build, t:'ACARS Client Build'});

golgotha.form.submit(f);
return true;
};

golgotha.local.setViewType = function(idx) {
	document.forms[0].viewType.selectedIndex = idx;
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarserrors.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate,this)">
<view:table cmd="acarserrors">
<!-- View Header Bar -->
<tr class="title">
 <td colspan="3" class="left">ACARS CLIENT ERROR LOGS</td>
 <td colspan="3" class="right">FILTER BY <el:combo name="viewType" idx="*" size="1" firstEntry="-" options="${filterOpts}" value="${param.viewType}" />
 BUILD <el:combo name="build" idx="*" size="1" firstEntry="-" options="${clientBuilds}" value="${param.build}" onChange="void golgotha.local.setViewType(3)" />
 USER <el:combo name="author" idx="*" size="1" firstEntry="-" options="${authors}" value="${param.author}" onChange="void golgotha.local.setViewType(2)" />
 <el:button ID="SortButton" type="submit" label="GO" /></td>
</tr>

<!-- View Legend Bar -->
<tr class="title caps">
 <td>#</td>
 <td style="width:14%">DATE/TIME</td>
 <td class="nophone">PILOT NAME</td>
 <td style="width:6%">BUILD</td>
 <td class="nophone">FS</td>
 <td class="left">ERROR MESSAGE</td>
</tr>

<!-- View Data -->
<!-- Log Entries -->
<c:forEach var="err" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[err.userID]}" scope="page" />
<c:set var="pilotLoc" value="${userData[err.userID]}" scope="page" />
<view:row entry="${err}">
 <td class="sec bld"><fmt:int value="${err.ID}" /></td>
 <td class="small bld"><el:cmd url="acarserror" link="${err}"><fmt:date date="${err.createdOn}" t="HH:mm" /></el:cmd></td>
 <td class="pri bld nophone"><el:profile location="${pilotLoc}">${pilot.name}</el:profile></td>
 <td class="sec bld"><fmt:int value="${err.clientBuild}" /><c:if test="${err.beta > 0}">b${err.beta}</c:if></td>
 <td class="small nophone">${err.simulator}</td>
 <td class="left"><fmt:text value="${err.message}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="6"><view:scrollbar><view:pgUp />&nbsp;<view:pgDn /></view:scrollbar>&nbsp;</td>
</tr>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

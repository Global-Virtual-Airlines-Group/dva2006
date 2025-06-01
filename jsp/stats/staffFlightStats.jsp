<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Program Staff Flight Statistics</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:googleAnalytics />
<content:js name="common" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:cspHeader />
<script async>
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:enum var="sortTypes" className="org.deltava.beans.stats.FlightStatsSort" exclude="OVLEGS,OILEGS,PIDS,DATE" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="staffstats.do" method="post" validate="return true">
<view:table cmd="staffstats">
<tr class="title">
 <td colspan="7" class="left caps"><span class="nophone"><content:airline /> PROGRAM STAFF&nbsp;</span>FLIGHT STATISTICS</td>
 <td colspan="6" class="right">DATE RANGE <el:combo name="days" options="${dayOpts}" value="${daysBack}" onChange="void golgotha.local.updateSort()" /> 
 SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortTypes}" value="${viewContext.sortType}" onChange="void golgotha.local.updateSort()" /></td>
</tr>
<%@ include file="/jsp/stats/pirepStats.jspf" %>
</view:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>

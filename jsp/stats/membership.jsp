<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Membership Statistics</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<content:favicon />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.quantiles, min:1, t:'Quantile Number'});
golgotha.form.submit();
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
<el:form action="memberstats.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="left caps"><content:airline /> FLIGHT STATISTICS</td>
 <td class="right">QUANTILES <el:text name="quantiles" idx="*" size="2" max="2" value="${quantileCount}" /></td>
</tr>

<!-- Airline Totals -->
<tr>
 <td class="label">Total Pilots</td>
 <td class="data" colspan="2"><b><fmt:int value="${totals.activePilots}" /> active pilots</b>,
 <fmt:int value="${totals.totalPilots}" /> total (<fmt:dec value="${totals.activePilots * 100 / totals.totalPilots}" fmt="##0.0" />% active)</td>
</tr>

<!-- Membership Quantiles -->
<tr class="title">
 <td colspan="3" class="left caps">MEMBERSHIP QUANTILES</td>
</tr>
<c:set var="qCount" value="0" scope="page" />
<c:forEach var="qLabel" items="${fn:keys(quantiles)}">
<c:set var="qJoinDate" value="${quantiles[qLabel]}" scope="page" />
<c:set var="qCount" value="${qCount + 1}" scope="page" />
<tr>
 <td class="label"><fmt:int value="${qCount}" /></td>
 <td class="data" colspan="2">Joined on or before <span class="pri bld"><fmt:date date="${qJoinDate}" fmt="d" /></span> 
(<fmt:int value="${totals.activePilots * qLabel / 100}" /> pilots)</td>
</tr>
</c:forEach>

<!-- Join Date statistics -->
<tr class="title">
 <td colspan="3" class="left caps">JOIN DATE STATISTICS</td>
</tr>
<c:forEach var="jDateStat" items="${joinDates}">

<tr>
 <td class="label"><fmt:int value="${jDateStat.ID}" /> Days ago</td>
 <td class="data">Period of <span class="pri bld"><fmt:date date="${jDateStat.date}" fmt="d" /></span> 
 (<fmt:int value="${jDateStat.count}" /> Pilots joined)</td>
 <td class="data"><el:img y="12" x="${(jDateStat.count * 450) / maxCount}" src="cooler/bar_blue.png" caption="${jDateStat.date}" /></td>
</tr>
</c:forEach>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

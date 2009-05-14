<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Statistics Dashboard</title>
<content:css name="main" browserSpecific="true" />
<content:css name="view" />
<content:css name="form" />
<content:pics />
<script language="JavaScript" type="text/javascript">
function updateSort()
{
document.forms[0].submit();
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
<el:form action="dashboard.do" method="post" validate="return true">
<el:table className="view" pad="default" space="default">
<tr class="title">
 <td colspan="2" class="left">STATISTICS DASHBOARD</td>
 <td colspan="2">SORT BY <el:combo name="sortType" size="1" idx="*" options="${sortOptions}" value="${param.sortType}" onChange="void updateSort()" /></td>
 <td colspan="2" class="right">FROM <el:text name="startDays" idx="*" size="2" max="3" value="${startDays}" /> TO
 <el:text name="endDays" idx="*" size="2" max="3" value="${endDays}" /> DAYS AGO</td>
</tr>
<c:set var="pirepApproval" value="${results['pirepApproval']}" scope="page" />
<c:if test="${!empty pirepApproval}">
<!-- PIREP Approval Data -->
<tr class="title caps">
 <td colspan="6" class="left">FLIGHT REPORT APPROVAL DELAY</td>
</tr>
<tr class="title">
 <td width="15%" class="caps">${param.paGroup}</td>
 <td width="25%">ORDER BY <el:combo name="paGroup" size="1" idx="*" options="${pirepGroupOptions}" value="${param.paGroup}" onChange="void updateSort()" /></td>
 <td width="15%">AVERAGE</td>
 <td width="15%">MAXIMUM</td>
 <td width="15%">MINIMUM</td>
 <td width="15%">TOTAL</td>
</tr>
<c:forEach var="metric" items="${pirepApproval}">
<tr>
 <td colspan="2" class="pri bld">${metric.name}</td>
 <td><fmt:dec value="${metric.average}" /> hours</td>
 <td><fmt:dec value="${metric.maximum}" /> hours</td>
 <td><fmt:dec value="${metric.minimum}" /> hours</td>
 <td><fmt:int value="${metric.count}" /> reports</td>
</tr>
</c:forEach>
</c:if>
<c:set var="examGrade" value="${results['examGrading']}" scope="page" />
<c:if test="${!empty examGrade}">
<!-- Exam Grading Data -->
<tr class="title caps">
 <td colspan="6" class="left">PILOT EXAMINATION SCORING DELAY</td>
</tr>
<tr class="title">
 <td width="15%" class="caps">${param.examGroup}</td>
 <td width="25%">ORDER BY <el:combo name="examGroup" size="1" idx="*" options="${examGroupOptions}" value="${param.examGroup}" onChange="void updateSort()" /></td>
 <td width="15%">AVERAGE</td>
 <td width="15%">MAXIMUM</td>
 <td width="15%">MINIMUM</td>
 <td width="15%">TOTAL</td>
</tr>
<c:forEach var="metric" items="${examGrade}">
<tr>
 <td colspan="2" class="pri bld">${metric.name}</td>
 <td><fmt:dec value="${metric.average}" /> hours</td>
 <td><fmt:dec value="${metric.maximum}" /> hours</td>
 <td><fmt:dec value="${metric.minimum}" /> hours</td>
 <td><fmt:int value="${metric.count}" /> exams</td>
</tr>
</c:forEach>
</c:if>
<c:set var="rideGrade" value="${results['rideGrading']}" scope="page" />
<c:if test="${!empty rideGrade}">
<!-- Check Ride Grading Data -->
<tr class="title caps">
 <td colspan="6" class="left">PILOT CHECK RIDE SCORING DELAY</td>
</tr>
<tr class="title">
 <td width="15%" class="caps">${param.rideGroup}</td>
 <td width="25%">ORDER BY <el:combo name="rideGroup" size="1" idx="*" options="${rideGroupOptions}" value="${param.rideGroup}" onChange="void updateSort()" /></td>
 <td width="15%">AVERAGE</td>
 <td width="15%">MAXIMUM</td>
 <td width="15%">MINIMUM</td>
 <td width="15%">TOTAL</td>
</tr>
<c:forEach var="metric" items="${rideGrade}">
<tr>
 <td colspan="2" class="pri bld">${metric.name}</td>
 <td><fmt:dec value="${metric.average}" /> hours</td>
 <td><fmt:dec value="${metric.maximum}" /> hours</td>
 <td><fmt:dec value="${metric.minimum}" /> hours</td>
 <td><fmt:int value="${metric.count}" /> rides</td>
</tr>
</c:forEach>
</c:if>
<c:set var="frStats" value="${results['pirepStats']}" scope="page" />
<c:if test="${!empty frStats}">
<!-- Flight Report Statistics -->
<tr class="title caps">
 <td colspan="6" class="left">FLIGHT REPORT STATISTICS</td>
</tr>
<tr class="title">
 <td width="15%" class="caps">${param.frGroup}</td>
 <td width="25%">ORDER BY <el:combo name="frGroup" size="1" idx="*" options="${pirepGroupOptions}" value="${param.frGroup}" onChange="void updateSort()" /></td>
 <td width="15%">AVERAGE</td>
 <td width="15%">MAXIMUM</td>
 <td width="15%">MINIMUM</td>
 <td width="15%">TOTAL</td>
</tr>
<c:forEach var="metric" items="${frStats}">
<tr>
 <td colspan="2" class="pri bld">${metric.name}</td>
 <td><fmt:dec value="${metric.average}" /> hours</td>
 <td><fmt:dec value="${metric.maximum}" /> hours</td>
 <td><fmt:dec value="${metric.minimum}" /> hours</td>
 <td><fmt:int value="${metric.count}" /> flights</td>
</tr>
</c:forEach>
</c:if>
<c:set var="afrStats" value="${results['acarsStats']}" scope="page" />
<c:if test="${!empty afrStats}">
<!-- ACARS Flight Report Statistics -->
<tr class="title caps">
 <td colspan="6" class="left">ACARS FLIGHT REPORT STATISTICS</td>
</tr>
<tr class="title">
 <td width="15%" class="caps">${param.afrGroup}</td>
 <td width="25%">ORDER BY <el:combo name="afrGroup" size="1" idx="*" options="${pirepGroupOptions}" value="${param.afrGroup}" onChange="void updateSort()" /></td>
 <td width="15%">AVERAGE</td>
 <td width="15%">MAXIMUM</td>
 <td width="15%">MINIMUM</td>
 <td width="15%">TOTAL</td>
</tr>
<c:forEach var="metric" items="${afrStats}">
<tr>
 <td colspan="2" class="pri bld">${metric.name}</td>
 <td><fmt:dec value="${metric.average}" /> hours</td>
 <td><fmt:dec value="${metric.maximum}" /> hours</td>
 <td><fmt:dec value="${metric.minimum}" /> hours</td>
 <td><fmt:int value="${metric.count}" /> flights</td>
</tr>
</c:forEach>
</c:if>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Top Dispatchers</title>
<content:css name="main" />
<content:css name="view" />
<content:css name="form" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.updateSort = function() { return document.forms[0].submit(); };
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
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
<el:form action="dspstats.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="view">
<tr class="title">
 <td colspan="4" class="left caps"><span class="nophone"><content:airline /> </span>TOP DISPATCHERS - <fmt:date date="${range.startDate}" tzName="UTC" fmt="d" /> - 
 <fmt:date date="${range.endDate}" tzName="UTC" fmt="d" /></td>
 <td colspan="2" class="right"><span class="nophone">SELECT </span><el:combo name="range" idx="*" size="1" firstEntry="[ SELECT ]" value="${range}" options="${ranges}" onChange="void golgotha.local.updateSort()" /></td>
</tr>

<!-- Table Header bar -->
<tr class="title caps">
 <td style="width:7%">#</td>
 <td style="width:40%">DISPATCHER</td>
 <td class="nophone" style="width:15%">PILOT CODE</td>
 <td style="width:10%">FLIGHTS</td>
 <td style="width:10%">HOURS</td>
 <td>PERCENT</td>
</tr>

<!-- Table Statistics Data -->
<c:set var="idx" value="0" scope="page" />
<c:forEach var="stat" items="${stats}">
<c:set var="idx" value="${idx + 1}" scope="page" />
<c:set var="userLoc" value="${userData[stat.ID]}" scope="page" />
<c:set var="pilot" value="${pilots[stat.ID]}" scope="page" />
<tr>
 <td class="sec bld"><fmt:int value="${idx}" /></td>
 <td><el:profile className="bld plain" location="${userLoc}">${pilot.name}</el:profile></td>
 <td class="pri bld nophone">${pilot.pilotCode}</td>
 <td><fmt:int value="${stat.legs}" /></td>
 <td><fmt:dec value="${stat.hours}" fmt="##0.00" /></td>
 <td class="sec"><fmt:dec value="${stat.hours * 100.0 / totalHours}" fmt="##0.00" />%</td>
</tr>
</c:forEach>

<!-- Bottom Bar -->
<tr class="title">
 <td colspan="6">&nbsp;</td>
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

<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Optimal Landings</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.update = function() { return document.forms[0].submit(); };
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.viewCount, min:1, t:'Number of Landings'});
golgotha.form.submit(f);
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
<el:form action="landings.do" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
The members of <content:airline /> are a skilled group - and they can prove it. Below is a list of the best landings and the Pilots who made them. Landings are rated on a combination of touchdown speed (relative to an optimum rate) and 
distance down the runway.<br />
<br />
<el:table className="view">
<!-- Table top Header bar -->
<tr class="title">
 <td class="left caps" colspan="3"><span class="nophone"><content:airline /> </span>OPTIMAL LANDINGS</td>
 <td class="right" colspan="5">TOP <el:text name="viewCount" idx="*" size="1" max="2" value="${viewContext.count}" /> 
FLIGHTS <span class="nophone">WITHIN <el:combo name="days" idx="*" size="1" options="${dateFilter}" value="${daysBack}" onChange="void golgotha.local.update()" /></span> IN  
<el:combo name="eqType" idx="*" size="1" options="${eqTypes}" value="${param.eqType}" onChange="void golgotha.local.update()" /><span class="nophone"><el:button ID="SearchButton" type="submit" label="GO" /></span></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td>#</td>
 <td style="width:20%">PILOT NAME</td>
 <td class="nophone">DATE</td>
 <td>FLIGHT</td>
 <td>EQUIPMENT</td>
 <td style="width:10%">TOUCHDOWN</td>
 <td>DISTANCE</td>
 <td class="nophone">AIRPORTS</td>
</tr>

<!-- Table Flight Report Data -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[pirep.authorID]}" scope="page" />
<c:set var="rwy" value="${rwys[pirep.ID]}" scope="page" />
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr>
 <td class="sec bld">${entryNumber}</td>
 <td>${pilot.name}</td>
 <td class="pri nophone"><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td><el:cmd className="small bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="sec small">${pirep.equipmentType}</td>
 <td class="bld"><fmt:int value="${pirep.landingVSpeed}" /> ft/min</td>
 <td class="sec bld"><fmt:int value="${rwy.distance}" /> ft</td>
 <td class="nophone"><span class="small">${pirep.airportA.name}</span> Runway ${rwy.name}</td>
</tr>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="8">&nbsp;</td>
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

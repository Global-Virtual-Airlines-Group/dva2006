<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Greased Landing Club</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:js name="common" />
<content:pics />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golgotha.form.validate({f:f.viewCount, min:1, t:'Number of Landings'});
golgotha.form.submit(f);
return true;
};

golgotha.local.update = function() { return document.forms[0].submit(); };
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
The members of <content:airline /> are a skilled group - and they can prove it. Below is a list of the 
smoothest landings, and the greaser pilots who made them:<br />
<br />
<el:table className="view">
<!-- Table top Header bar -->
<tr class="title">
 <td class="left caps" colspan="3"><content:airline /> GREASED LANDING CLUB</td>
 <td class="right" colspan="4"><el:text name="viewCount" idx="*" size="1" max="2" value="${viewCount}" /> 
FLIGHTS WITHIN <el:combo name="days" idx="*" size="1" options="${dateFilter}" value="${param.days}" onChange="void golgotha.local.update()" /> 
IN <el:combo name="eqType" idx="*" size="1" options="${eqTypes}" value="${param.eqType}" onChange="void golgotha.local.update()" />
<el:button ID="SearchButton" type="submit" label="GO" /></td>
</tr>

<!-- Table Header Bar-->
<tr class="title caps">
 <td style="width:5%">#</td>
 <td style="width:20%">PILOT NAME</td>
 <td style="width:10%">DATE</td>
 <td style="width:10%">FLIGHT #</td>
 <td style="width:10%">EQUIPMENT</td>
 <td style="width:10%">TOUCHDOWN</td>
 <td>AIRPORTS</td>
</tr>

<!-- Table Flight Report Data -->
<c:set var="entryNumber" value="0" scope="page" />
<c:forEach var="pirep" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[fn:PilotID(pirep)]}" scope="page" />
<c:set var="entryNumber" value="${entryNumber + 1}" scope="page" />
<tr>
 <td class="sec bld">${entryNumber}</td>
 <td>${pilot.name}</td>
 <td class="pri bld"><fmt:date fmt="d" date="${pirep.date}" /></td>
 <td><el:cmd className="small bld" url="pirep" link="${pirep}">${pirep.flightCode}</el:cmd></td>
 <td class="sec">${pirep.equipmentType}</td>
 <td class="bld"><fmt:int value="${pirep.landingVSpeed}" /> ft/min</td>
 <td class="small">${pirep.airportD.name} - ${pirep.airportA.name}</td>
</tr>
</c:forEach>

<!-- Button Bar -->
<tr class="title">
 <td colspan="7">&nbsp;</td>
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

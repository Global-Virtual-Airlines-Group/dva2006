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
<title><content:airline /> ACARS Empty Flight Log</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<script language="JavaScript" type="text/javascript">
function switchType(combo)
{
var logType = combo.options[combo.selectedIndex].value;
self.location = 'acarsempty.do?id=' + logType;
return true;
}

function selectAll()
{
var form = document.forms[0];
for (var x = 0; x < form.flightID.length; x++)
	isChecked = form.flightID[x].checked = true;
	
return true;
}

function validate(form)
{
var isChecked = false;
for (var x = 0; x < form.flightID.length; x++)
	isChecked = (isChecked || form.flightID[x].checked);

// Check if at least one flight is checked
if (!isChecked) {
	alert('Select at least one Flight entry to delete.');
	return false;
}

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
<el:form action="acarsdelf.do" method="post" validate="return validate(this)">
<view:table className="view" space="default" pad="default" cmd="acarsempty">
<!-- View Header Bar -->
<tr class="title">
 <td colspan="4" class="left">EMPTY ACARS FLIGHT INFORMATION ENTRIES</td>
 <td colspan="4" class="right">VIEW EMPTY <el:combo name="viewType" idx="*" size="1" options="${displayTypes}" value="${displayType}" onChange="void switchType(this)" /></td>
</tr>

<!-- View Legend Bar -->
<tr class="title caps">
 <td width="5%">ID</td>
 <td width="5%"><el:button onClick="void selectAll()" className="BUTTON" label="ALL" /></td>
 <td width="15%">START/END TIME</td>
 <td width="20%">PILOT NAME / CODE</td>
 <td width="10%">FLIGHT</td>
 <td width="18%">ORIGIN</td>
 <td>DESTINATION</td>
</tr>

<!-- Result Data -->
<c:forEach var="flight" items="${viewContext.results}">
<c:set var="pilot" value="${pilots[flight.pilotID]}" scope="request" />
<c:set var="pilotLoc" value="${userData[flight.pilotID]}" scope="request" />
<view:row entry="${info}">
 <td class="pri bld"><el:cmd url="acarsinfo" link="${flight}"><fmt:int value="${flight.ID}" /></el:cmd></td>
 <td><el:box name="flightID" idx="*" value="${flight.ID}" label="" /></td>
 <td class="small"><fmt:date t="HH:mm" date="${flight.startTime}" />
<c:if test="${!empty flight.endTime}">
<br /><fmt:date t="HH:mm" date="${flight.endTime}" />
</c:if>
</td>
 <td class="pri bld"><el:profile location="${pilotLoc}">${pilot.name}</el:profile> (${pilot.pilotCode})</td>
 <td class="bld">${flight.flightCode}</td>
 <td class="small">${flight.airportD.name} (<fmt:airport airport="${flight.airportD}" />)</td>
 <td class="small">${flight.airportA.name} (<fmt:airport airport="${flight.airportA}" />)</td>
</view:row>
</c:forEach>

<!-- Scroll Bar -->
<tr class="title">
 <td colspan="7"><el:button onClick="void selectAll()" className="BUTTON" label="SELECT ALL" />
  <el:button type="submit" label="DELETE FLIGHTS" className="BUTTON" /><view:scrollbar><br />
<view:pgUp />&nbsp;<view:pgDn /></view:scrollbar></td>
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

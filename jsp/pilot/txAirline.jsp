<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Airline Transfer for ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script async>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.dbName, t:'Airline Name'});
	golgotha.form.validate({f:f.eqType, t:'Equipment Program'});
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
<content:enum var="ranks" className="org.deltava.beans.Rank" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="txairline.do" method="post" link="${pilot}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">INTER-AIRLINE PILOT TRANSFER FOR ${pilot.name}</td>
</tr>
<tr>
 <td class="label">Current Rank / Program</td>
 <td class="data">${pilot.rank.name}, <span class="sec">${pilot.equipmentType}</span></td>
</tr>
<tr>
 <td class="label">New Airline</td>
 <td class="data"><el:combo name="dbName" size="1" idx="*" options="${airlines}" firstEntry="[ SELECT AIRLINE ]" value="${param.dbName}" /></td>
</tr>
<c:if test="${!empty eqTypes}">
<tr>
 <td class="label top">Equipment Program</td>
 <td class="data"><el:combo name="eqType" size="1" idx="*" options="${eqTypes}" firstEntry="[ SELECT ]" /><br />
<c:forEach var="eqType" items="${eqTypes}">
${eqType.name} (Stage <fmt:int value="${eqType.stage}" />)<br />
</c:forEach></td>
</tr>
<tr>
 <td class="label">Rank</td>
 <td class="data"><el:combo name="rank" size="1" idx="*" options="${ranks}" value="${pilot.rank.name}" /></td>
</tr>
</c:if>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="assignID" idx="*" value="true" checked="${param.assignID}" label="Automatically assign Pilot ID at new Airline" /><br />
<el:box name="keepActive" idx="*" value="true" checked="${currentAirline.allowMultiAirline}" label="Keep Pilot active after transfer" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="${!empty eqTypes ? 'TRANSFER PILOT' : 'SELECT AIRLINE'}" /></td>
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

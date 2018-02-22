<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title>Currency Check Ride for ${pilot.name}</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<script>
<fmt:js var="golgotha.local.eqAircraft" object="${eqAircraft}" />
golgotha.local.updateEQ = function(combo) {
	var eq = golgotha.form.getCombo(combo);
	var acTypes = golgotha.local.eqAircraft[eq];
	var acc = document.forms[0].acType;
	acc.options.length = acTypes.length + 1;
	acc.selectedIndex = 0;
	for (var x = 0; x < acTypes.length; x++)
		acc.options[x + 1] = new Option(acTypes[x], acTypes[x]);

	return true;
};

golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.crType, t:'Aircraft Type'});
	golgotha.form.validate({f:f.eqType, t:'Equimpment Program'});
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
<el:form action="currencyassign.do" method="post" link="${pilot}" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">CURRENCY CHECK RIDE<span class="nophone"> - ${pilot.rank.name} ${pilot.name} (${pilot.pilotCode})</span></td>
</tr>
<c:choose>
<c:when test="${hasCheckRide}">
<tr>
 <td colspan="2" class="pri bld mid">You currently have a pending Check Ride at <content:airline />. You cannot have more than one pending Check Ride at a time.</td>
</tr>
</c:when>
<c:when test="${empty eqTypes}">
<tr>
 <td colspan="2" class="pri bld mid">You do not have any Equipment type ratings that will expire before <fmt:date fmt="d" date="${expiryDate}" />.</td>
</tr>
</c:when>
<c:otherwise>
<tr>
 <td class="label">Equipment Program</td>
 <td class="data"><el:combo name="eqType" idx="*" size="1" firstEntry="[ EQUIPMENT PROGRAM ]" options="${eqTypes}" value="${param.eqType}" onChange="void golgotha.local.updateEQ(this)" />
 <span class="small ita nophone">(Don't see a program listed? You can only request Check Rides for type ratings that expire before <fmt:date fmt="d" date="${expiryDate}" />.)</span></td>
</tr>
<tr>
 <td class="label">Aircraft Type</td>
 <td class="data"><el:combo name="acType" idx="*" size="1" firstEntry="[ AIRCRAFT TYPE ]" options="${empty eqType ? actypes : eqType.primaryRatings}" value="${param.acType}" /></td>
</tr>
</c:otherwise>
</c:choose>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="ASSIGN CHECK RIDE" /></td>
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

<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> - Hub Airport</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1">
<content:googleAnalytics />
<fmt:aptype var="useICAO" />
<content:newRelic>
<content:cspHeader />
</content:newRelic>
<script nonce="${contentSecurity.nonce}">
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.airline, t:'Airline'});
	golgotha.form.validate({f:f.airport, t:'Airport'});
	golgotha.form.validate({f:f.destCount, min:1, t:'Destination Flight Count'});
	golgotha.form.submit(f);
	return true;
};

golgotha.local.updateAirline = function(cb) {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config.clone();
	cfg.airline = golgotha.form.getCombo(cb);
	golgotha.airportLoad.changeAirline([f.airport], cfg);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config;
	cfg.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers([f.airport]);
	golgotha.airportLoad.setText([f.airline,f.airport]);
	if (f.airline.selectedIndex > 0)
		f.airline.onchange();
});
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:empty var="emptyList" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="hub.do" method="post" op="save" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2">HUB AIRPORT PROFILE</td>
</tr>
<tr>
 <td class="label">Airline Name</td>
 <td class="data"><el:combo name="airline" idx="*" size="1" required="true" options="${airlines}" firstEntry="[ AIRLINE ]" value="${hub.airline}" onChange="void golgotha.local.updateAirline(this);" /></td>
</tr>
<tr>
 <td class="label">Airport</td>
 <td class="data"><el:combo name="airport" idx="*" size="1" required="true" options="${emptyList}" value="${hub.airport}" /><el:airportCode combo="airport" idx="*" airport="${hub.airport}" /></td>
</tr>
<tr>
 <td class="label">Destinations Served</td>
 <td class="data"><el:text name="destCount" idx="*" size="3" max="4" required="true" value="${hub.destinationCount}" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE HUB AIRPORT PROFILE" /></td>
</tr>
</el:table>
</el:form>
<br>
<content:copyright />
</content:region>
</content:page>
</body>
</html>

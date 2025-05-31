<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_mapbox.tld" prefix="map" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Airport Gate Information</title>
<content:expire expires="3600" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:googleAnalytics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:js name="gateInfo" />
<map:api version="3" />
<fmt:aptype var="useICAO" />
<content:cspHeader />
<script async>
golgotha.gate.hasPFI = ${airport.hasPFI};
golgotha.gate.hasSchengen = ${airport.isSchengen};
golgotha.gate.showTabs = true;
golgotha.local.update = function(cb) {
	if (!golgotha.form.check()) return false;
	self.location = '/gateinfo.do?id=' + golgotha.form.getCombo(cb);
	golgotha.form.submit(cb.parentElement);
	return true;
};

golgotha.onDOMReady(function() {
	const f = document.forms[0];
	const cfg = golgotha.airportLoad.config;
	cfg.doICAO = ${useICAO};
	golgotha.airportLoad.setHelpers([f.id]);
	golgotha.gate.load({id:'${airport.ICAO}'}); 
	return true;
});
</script>
</head>
<content:copyright visible="false" />
<body onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="gateinfo.do" method="get" validate="return false">
<el:table ID="gateInfo" className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>GATE INFORMATION -<span class="nophone"> ${airport.name}</span> (<el:cmd url="airportinfo" linkID="${airport.ICAO}"><fmt:airport airport="${airport}" /></el:cmd>)</td>
 <td style="width:35%" class="nophone right">AIRPORT <el:combo name="id" size="1" idx="*" value="${airport}" options="${airports}"  onChange="void golgotha.local.update(this)" />
 <el:text name="idCode" size="3" max="4" className="caps" value="${airport.ICAO}" onBlur="void document.forms[0].id.setAirport(this.value, true)" /></td>
</tr>
<c:if test="${!empty airlines}">
<tr>
 <td class="label top">Airlines Served</td>
 <td class="data" colspan="2"><fmt:list value="${airlines}" delim=", " /></td>
</tr>
</c:if>
<c:if test="${airport.isSchengen || airport.hasPFI}">
<tr>
 <td class="label top">Customs Zones</td>
 <td class="data bld caps" colspan="2"><c:if test="${airport.isSchengen}"><div class="ter">This Airport is part of the Schengen Area</div></c:if>
<c:if test="${airport.hasPFI}"><span class="pri">This Airport has a US Customs Pre-Flight Inspection station</span></c:if></td>
</tr>
</c:if>
<tr>
 <td class="label">Gate Legend</td>
 <td class="data small" colspan="2"><img src="https://maps.google.com/mapfiles/kml/pal2/icon56.png" alt="Our Gate"  width="16" height="16" />&nbsp;Domestic  | <img src="https://maps.google.com/mapfiles/kml/pal2/icon48.png" alt="International Gate"  width="16" height="16" />&nbsp;International 
<c:if test="${airport.hasPFI}"> | <img src="https://maps.google.com/mapfiles/kml/pal2/icon16.png" alt="USPFI Gate" width="16" height="16" />&nbsp;US PFI</c:if>
<c:if test="${airport.isSchengen}"> | <img src="https://maps.google.com/mapfiles/kml/pal2/icon17.png" alt="Schengen Zone Gate" width="16" height="16" />&nbsp;Schengen</c:if>
 | <img src="https://maps.google.com/mapfiles/kml/pal3/icon52.png" alt="Frequently Used Gate"  width="16" height="16" /> Frequently Used | <img src="https://maps.google.com/mapfiles/kml/pal3/icon60.png" alt="Other Gate"  width="16" height="16" /> Other</td>
</tr>
<tr>
 <td colspan="3"><map:div ID="mapBox" height="570" /></td>
</tr>
<tr class="title mid" id="buttonRow" style="display:none;">
 <td colspan="3"><el:button ID="SaveButton" label="SAVE GATE ASSIGNMENTS" onClick="void golgotha.gate.save()" />&nbsp;<el:button ID="UndoButton" label="UNDO CHANGES" onClick="void golgotha.gate.undo()" />&nbsp;</td>
</tr>
</el:table>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<script async>
<map:token />
<map:point var="golgotha.local.mapC" point="${airport}" />
<map:bounds var="golgotha.local.mapBounds" items="${rwys}" />

// Create the map
const map = new golgotha.maps.Map(document.getElementById('mapBox'), {center:golgotha.local.mapC,zoom:15,minZoom:12,maxZoom:19,scrollzoom:false,style:'mapbox://styles/mapbox/satellite-v9'});
map.fitBounds(golgotha.local.mapBounds);
map.on('zoomend', function() { map.toggle(golgotha.local.gates, (map.getZoom() > 11)); });
</script>
</body>
</html>

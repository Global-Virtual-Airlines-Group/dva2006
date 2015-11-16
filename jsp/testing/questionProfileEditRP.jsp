<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<html lang="en">
<head>
<title>Examination Question Profile</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:json />
<content:js name="airportRefresh" />
<map:api version="3" />
<content:js name="routePlot" />
<content:googleAnalytics eventSupport="true" />
<script type="text/javascript">
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
golglotha.form.validate({f:f.question, l:20, t:'Question Text'});
golglotha.form.validate({f:f.route1, l:3, t:'First Route Choice'});
golglotha.form.validate({f:f.route2, l:3, t:'Second Route Choice'});
golglotha.form.validate({f:f.imgData, ext:['gif','jpg','png'], t:'Image Resource'});
golglotha.form.validate({f:f.owner, t:'Owner'});
golglotha.form.validate({f:f.airline, min:1, t:'Airline'});
golglotha.form.validate({f:f.airportD, t:'Departure Airport'});
golglotha.form.validate({f:f.airportA, t:'Arrival Airport'});
golglotha.form.validate({f:f.correctChoice, t:'Correct Answer to this Question'});
golgotha.form.submit(f);
return true;
};

golgotha.local.massageRoute = function(txtbox)
{
var f = document.forms[0];
if (txtbox == null) txtbox = f.route1;
var wps = txtbox.value.split(' ');
if ((txtbox.value.length < 2) || (wps.length < 3))
	return false;

// Get SID/STAR
var sid = golgotha.form.getCombo(f.sid);
var star = golgotha.form.getCombo(f.star);

//Add the selected SID/STAR to the route
if ((sid != null) && (wps[0].indexOf('.') == -1))
	txtbox.value = sid + ' ' + txtbox.value;
if ((star != null) && (wps[wps.length - 1].indexOf('.') == -1))
	txtbox.value = txtbox.value + ' ' + star;

return true;
};

golglotha.local.updateCorrect = function(txtbox)
{
var f = document.forms[0];

// Update correct answer choices
var combo = f.correctChoice;
var oldAnswer = combo.selectedIndex;
combo.options.length = 1;
combo.options[0] = new Option('-');
var hasAnswers = false; var maxAnswer = 0;
for (var x = 1; x <= 5; x++) {
	var rt = eval('f.route' + x + '.value');
	if (rt.length > 2) {
		var ofs = f.correctChoice.length;
		combo.options.length = (ofs + 1);
		combo.options[ofs] = new Option(rt);
		maxAnswer = x;
	}
}

// Hide unused answers
for (var x = 1; x <= 5; x++) {
	var rt = eval('f.route' + x + '.value');
	var row = document.getElementById('choice' + x);
	if (x > (maxAnswer + 1))
		row.style.display = 'none';
	else
		row.style.display = '';
}

// Show the correct answer
var row = document.getElementById('correctAnswerRow');
row.style.display = (maxAnswer > 1) ? '' : 'none';
combo.selectedIndex = (oldAnswer >= combo.options.length) ? 0 : oldAnswer;
golgotha.local.plotRouteMap(combo);
return true;
};

golgotha.local.plotRouteMap = function(combo)
{
var f = document.forms[0];
if (combo.selectedIndex == 0) {
	map.clearOverlays();
	return false;
}

// Create updated map of parameters for route plotting
var opt = combo.options[combo.selectedIndex];
var wps = opt.value.split(' ');
if (wps[0].indexOf('.') != -1) {
	golgotha.form.setCombo(f.sid, wps[0]);
	wps.splice(0, 1);
}
if (wps[wps.length -1].indexOf('.') != -1) {
	golgotha.form.setCombo(f.star, wps[wps.length - 1]);
	wps.splice(wps.length - 1, 1); 
}

var params = getAJAXParams();
params['route'] = wps.join(' ');
golgotha.routePlot.plotMap(params);
return true;
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.local.updateCorrect()" onunload="void golgotha.maps.util.unload()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="qprofile.do" link="${question}" op="save" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<!-- Question Title Bar -->
<tr class="title caps">
 <td colspan="2">EXAMINATION ROUTE PLOTTING QUESTION PROFILE</td>
</tr>
<tr>
 <td class="label">Question Text</td>
 <td class="data bld"><el:text name="question" idx="*" size="120" className="req" max="255" value="${question.question}" /></td>
</tr>
<tr>
 <td class="label">Owner Airline</td>
 <td class="data"><el:combo name="owner" idx="*" size="1" className="req" firstEntry="-" options="${airlines}" value="${question.owner}" /></td>
</tr>
<tr>
 <td class="label">Airlines</td>
 <td class="data"><el:check name="airline" width="175" options="${airlines}" className="req" checked="${question.airlines}" /></td>
</tr>
<tr>
 <td class="label top">Pilot Examinations</td>
 <td class="data"><el:check name="examNames" idx="*" cols="5" width="190" newLine="true" className="small" checked="${question.exams}" options="${examNames}" /></td>
</tr>
<c:if test="${!empty question}">
<tr>
 <td class="label">Statistics</td>
<c:if test="${question.totalAnswers > 0}">
 <td class="data">Answered <fmt:int value="${question.totalAnswers}" /> times,
 <fmt:int value="${question.correctAnswers}" /> correctly 
 (<fmt:dec value="${question.correctAnswers / question.totalAnswers * 100}" />%)</td>
</c:if>
<c:if test="${question.totalAnswers == 0}">
 <td class="data bld">This Question has never been included in a Pilot Examination</td>
</c:if>
</tr>
<c:if test="${question.size > 0}">
<tr>
 <td class="label">Image Information</td>
 <td class="data"><span class="pri bld">${question.typeName}</span> image, <fmt:int value="${question.size}" />
 bytes <span class="sec">(<fmt:int value="${question.width}" /> x <fmt:int value="${question.height}" />
 pixels)</span> <el:link className="pri bld small" url="javascript:void golgotha.exam.viewImage(${question.width},${question.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label top">Upload Image</td>
 <td class="data"><el:file name="imgData" idx="*" className="small" size="64" max="192" /><c:if test="${!empty question}"><br />
<el:box name="clearImg" className="small" idx="*" value="true" label="Clear Image Resource" /></c:if></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" className="small sec" value="true" checked="${question.active}" label="Question is Available" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" options="${airports}" firstEntry="-" value="${question.airportD}" className="req" onChange="this.updateAirportCode(); this.loadSIDSTAR(golgotha.form.getCombo(this), 'sid')" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" value="" onBlur="void document.forms[0].airportD.setAirport(this.value, true)" /></td>
</tr>
<tr>
 <td class="label">SID</td>
 <td class="data"><el:combo name="sid" size="1" options="${sids}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" firstEntry="-" value="${question.airportA}" className="req" onChange="this.updateAirportCode(); this.loadSIDSTAR(golgotha.form.getCombo(this), 'star')" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" value="" onBlur="void document.forms[0].airportA.setAirport(this.value, true)" /></td>
</tr>
<tr>
 <td class="label">STAR</td>
 <td class="data"><el:combo name="star" size="1" options="${stars}" firstEntry="-" /></td>
</tr>
<tr id="choice1">
 <td class="label">Choice #1</td>
 <td class="data"><el:text name="route1" idx="*" size="128" max="224" onBlur="golgotha.local.massageRoute(this); golgotha.local.updateCorrect()" className="small req" value="${fn:get(question.choices, 0)}" /></td>
</tr>
<tr id="choice2" style="display:none;">
 <td class="label">Choice #2</td>
 <td class="data"><el:text name="route2" idx="*" size="128" max="224" onBlur="golgotha.local.massageRoute(this); golgotha.local.updateCorrect()" className="small req" value="${fn:get(question.choices, 1)}" /></td>
</tr>
<tr id="choice3" style="display:none;">
 <td class="label">Choice #3</td>
 <td class="data"><el:text name="route3" idx="*" size="128" max="224" onBlur="golgotha.local.massageRoute(this); golgotha.local.updateCorrect()" className="small" value="${fn:get(question.choices, 2)}" /></td>
</tr>
<tr id="choice4" style="display:none;">
 <td class="label">Choice #4</td>
 <td class="data"><el:text name="route4" idx="*" size="128" max="224" onBlur="golgotha.local.massageRoute(this); golgotha.local.updateCorrect()" className="small" value="${fn:get(question.choices, 3)}" /></td>
</tr>
<tr id="choice5" style="display:none;">
 <td class="label">Choice #5</td>
 <td class="data"><el:text name="route5" idx="*" size="128" max="224" onBlur="golgotha.local.massageRoute(this); golgotha.local.updateCorrect()" className="small" value="${fn:get(question.choices, 4)}" /></td>
</tr>
<tr id="correctAnswerRow" style="display:none;">
 <td class="label top">Correct Answer</td>
 <td class="data"><el:combo name="correctChoice" onChange="void golgotha.local.plotRouteMap(this)" idx="*" size="1" className="small req" options="${question.choices}" firstEntry="-" value="${question.correctAnswer}" /></td>
</tr>
<tr>
 <td class="label top">Route Map</td>
 <td class="data"><map:div ID="googleMap" height="480" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button ID="SaveButton" type="submit" label="SAVE QUESTION" />
<c:if test="${access.canDelete}"> <el:cmdbutton ID="DeleteButton" url="qpdelete" link="${question}" label="DELETE QUESTION" /></c:if></td>
</tr>
</el:table>
<el:text name="isRoutePlot" type="hidden" value="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<fmt:aptype var="useICAO" />
<c:set var="mapDistance" value="${(empty question) ? 300 : question.distance}" scope="page" />
<script id="mapInit" defer>
var f = document.forms[0];
golgotha.airportLoad.config.doICAO = '${useICAO}';
golgotha.airportLoad.config.useSched = false;
golgotha.airportLoad.setHelpers(f.airportD, true);
golgotha.airportLoad.setHelpers(f.airportA, true);
f.airportD.updateAirportCode();
f.airportA.updateAirportCode();

// Create map
<map:point var="mapC" point="${mapCenter}" />
var mapTypes = {mapTypeIds:[google.maps.MapTypeId.TERRAIN, google.maps.MapTypeId.SATELLITE]};
var mapOpts = {center:mapC, zoom:golgotha.maps.util.getDefaultZoom(${mapDistance}), scrollwheel:false, streetViewControl:false, mapTypeControlOptions:mapTypes};
var map = new golgotha.maps.Map(document.getElementById('googleMap'), mapOpts);
<map:type map="map" type="${gMapType}" default="TERRAIN" />
map.infoWindow = new google.maps.InfoWindow({content:'', zIndex:golgotha.maps.z.INFOWINDOW});
google.maps.event.addListener(map, 'click', map.closeWindow);
</script>
</body>
</html>

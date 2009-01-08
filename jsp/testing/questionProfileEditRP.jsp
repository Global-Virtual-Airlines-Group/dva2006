<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<%@ taglib uri="/WEB-INF/dva_googlemaps.tld" prefix="map" %>
<map:xhtml>
<head>
<title>Examination Question Profile</title>
<content:css name="main" browserSpecific="true" />
<content:css name="form" />
<content:pics />
<content:js name="common" />
<content:js name="airportRefresh" />
<content:js name="googleMaps" />
<content:js name="routePlot" />
<map:api version="2" />
<map:vml-ie />
<content:sysdata var="imgPath" name="path.img" />
<content:googleAnalytics eventSupport="true" />
<script language="JavaScript" type="text/javascript">
document.imgPath = '${imgPath}';

function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.question, 20, 'Question Text')) return false;
if (!validateText(form.route1, 3, 'First Route Choice')) return false;
if (!validateText(form.route2, 3, 'Second Route Choice')) return false;
if (!validateFile(form.imgData, 'gif,jpg,png', 'Image Resource')) return false;
if (!validateCombo(form.owner, 'Owner')) return false;
if (!validateCheckBox(form.airline, 1, 'Airline')) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;
if (!validateCombo(form.correctChoice, 'Correct Answer to this Question')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
return true;
}

function massageRoute(txtbox)
{
var f = document.forms[0];
if (txtbox == null) txtbox = f.route1;
var wps = txtbox.value.split(' ');
if ((txtbox.value.length < 2) || (wps.length < 3))
	return false;

//Get SID/STAR
var sidC = f.sid;
var starC = f.star;
var sid = (sidC.selectedIndex == 0) ? null : sidC.options[sidC.selectedIndex].value;
var star = (starC.selectedIndex == 0) ? null : starC.options[starC.selectedIndex].value;

//Add the selected SID/STAR to the route
if ((sid != null) && (wps[0].indexOf('.') == -1))
	txtbox.value = sid + ' ' + txtbox.value;
if ((star != null) && (wps[wps.length - 1].indexOf('.') == -1))
	txtbox.value = txtbox.value + ' ' + star;

return true;
}

function updateCorrect(txtbox)
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
	var row = getElement('choice' + x);
	if (x > (maxAnswer + 1))
		row.style.display = 'none';
	else
		row.style.display = '';
}

// Show the correct answer
var row = getElement('correctAnswerRow');
row.style.display = (maxAnswer > 1) ? '' : 'none';
combo.selectedIndex = (oldAnswer >= combo.options.length) ? 0 : oldAnswer;
plotRouteMap(combo);
return true;
}

function plotRouteMap(combo)
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
	setCombo(f.sid, wps[0]);
	wps.splice(0, 1);
}
if (wps[wps.length -1].indexOf('.') != -1) {
	setCombo(f.star, wps[wps.length - 1]);
	wps.splice(wps.length - 1, 1); 
}

var params = getAJAXParams();
params['route'] = wps.join(' ');
plotMap(params);
return true;
}
<c:if test="${question.size > 0}">
function viewImage(x, y)
{
var flags = 'height=' + y + ',width=' + x + ',menubar=no,toolbar=no,status=yes,scrollbars=yes';
var w = window.open('/exam_rsrc/${question.hexID}', 'questionImage', flags);
return true;
}
</c:if>
</script>
</head>
<content:copyright visible="false" />
<body onunload="GUnload()" onload="void updateCorrect()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="airlines" name="apps" mapValues="true" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="qprofile.do" link="${question}" op="save" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form" pad="default" space="default">
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
 <td class="label" valign="top">Pilot Examinations</td>
 <td class="data"><el:check name="examNames" idx="*" cols="5" width="190" newLine="true" className="small" checked="${question.pools}" options="${examNames}" /></td>
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
 pixels) <el:link className="pri bld small" url="javascript:void viewImage(${question.width},${question.height})">VIEW IMAGE</el:link></td>
</tr>
</c:if>
</c:if>
<tr>
 <td class="label" valign="top">Upload Image</td>
 <td class="data"><el:file name="imgData" idx="*" className="small" size="64" max="192" /><c:if test="${!empty question}"><br />
<el:box name="clearImg" className="small" idx="*" value="true" label="Clear Image Resource" /></c:if></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="active" className="small sec" value="true" checked="${question.active}" label="Question is Available" /></td>
</tr>
<tr>
 <td class="label">Departing from</td>
 <td class="data"><el:combo name="airportD" idx="*" size="1" options="${airports}" firstEntry="-" value="${question.airportD}" className="req" onChange="changeAirport(this); updateSIDSTAR(document.forms[0].sid, getValue(this), 'sid')" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" value="${question.airportD.IATA}" onBlur="setAirport(document.forms[0].airportD, this.value); updateSIDSTAR(document.forms[0].sid, this.value, 'sid')" /></td>
</tr>
<tr>
 <td class="label">SID</td>
 <td class="data"><el:combo name="sid" size="1" options="${sids}" firstEntry="-" /></td>
</tr>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" idx="*" size="1" options="${airports}" firstEntry="-" value="${question.airportA}" className="req" onChange="changeAirport(this); updateSIDSTAR(document.forms[0].star, getValue(this), 'star')" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" value="${question.airportA.IATA}" onBlur="setAirport(document.forms[0].airportA, this.value); updateSIDSTAR(document.forms[0].star, this.value, 'star')" /></td>
</tr>
<tr>
 <td class="label">STAR</td>
 <td class="data"><el:combo name="star" size="1" options="${stars}" firstEntry="-" /></td>
</tr>
<tr id="choice1">
 <td class="label">Choice #1</td>
 <td class="data"><el:text name="route1" idx="*" size="128" max="224" onBlur="massageRoute(this); updateCorrect()" className="small req" value="${fn:get(question.choices, 0)}" /></td>
</tr>
<tr id="choice2" style="display:none;">
 <td class="label">Choice #2</td>
 <td class="data"><el:text name="route2" idx="*" size="128" max="224" onBlur="massageRoute(this); updateCorrect()" className="small req" value="${fn:get(question.choices, 1)}" /></td>
</tr>
<tr id="choice3" style="display:none;">
 <td class="label">Choice #3</td>
 <td class="data"><el:text name="route3" idx="*" size="128" max="224" onBlur="massageRoute(this); updateCorrect()" className="small" value="${fn:get(question.choices, 2)}" /></td>
</tr>
<tr id="choice4" style="display:none;">
 <td class="label">Choice #4</td>
 <td class="data"><el:text name="route4" idx="*" size="128" max="224" onBlur="massageRoute(this); updateCorrect()" className="small" value="${fn:get(question.choices, 3)}" /></td>
</tr>
<tr id="choice5" style="display:none;">
 <td class="label">Choice #5</td>
 <td class="data"><el:text name="route5" idx="*" size="128" max="224" onBlur="massageRoute(this); updateCorrect()" className="small" value="${fn:get(question.choices, 4)}" /></td>
</tr>
<tr id="correctAnswerRow" style="display:none;">
 <td class="label" valign="top">Correct Answer</td>
 <td class="data"><el:combo name="correctChoice" onChange="void plotRouteMap(this)" idx="*" size="1" className="small req" options="${question.choices}" firstEntry="-" value="${question.correctAnswer}" /></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="480" /></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar" pad="default" space="default">
<tr>
 <td><el:button ID="SaveButton" type="SUBMIT" className="BUTTON" label="SAVE QUESTION" />
<c:if test="${access.canDelete}"> <el:cmdbutton ID="DeleteButton" url="qpdelete" link="${question}" label="DELETE QUESTION" /></c:if></td>
</tr>
</el:table>
<el:text name="isRoutePlot" type="hidden" value="true" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<c:set var="mapDistance" value="${(empty question) ? 300 : question.distance}" scope="request" />
<script language="JavaScript" type="text/javascript">
var doRunways = false;
<map:point var="mapC" point="${mapCenter}" />
var map = new GMap2(getElement("googleMap"), {mapTypes:[G_SATELLITE_MAP, G_PHYSICAL_MAP]});
map.addControl(new GSmallMapControl());
map.addControl(new GMapTypeControl());
map.setCenter(mapC, getDefaultZoom(${mapDistance}));
map.enableDoubleClickZoom();
map.enableContinuousZoom();
map.setMapType(G_PHYSICAL_MAP);
</script>
</body>
</map:xhtml>

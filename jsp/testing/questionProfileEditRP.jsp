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
<content:googleAnalytics eventSupport="true" />
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;
if (!validateText(form.question, 20, 'Question Text')) return false;
if (!validateText(form.routeCodes, 5, 'Correct Answer to this Question')) return false;
if (!validateFile(form.imgData, 'gif,jpg,png', 'Image Resource')) return false;
if (!validateCombo(form.owner, 'Owner')) return false;
if (!validateCheckBox(form.airline, 1, 'Airline')) return false;
if (!validateCombo(form.airportD, 'Departure Airport')) return false;
if (!validateCombo(form.airportA, 'Arrival Airport')) return false;

setSubmit();
disableButton('SaveButton');
disableButton('DeleteButton');
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
<body onunload="GUnload()">
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
 <td class="data"><el:combo name="airportD" size="1" options="${airports}" firstEntry="-" value="${question.airportD}" className="req" onChange="changeAirport(this); updateSIDSTAR(document.forms[0].sid, getValue(this), 'sid')" />
 <el:text ID="airportDCode" name="airportDCode" idx="*" size="3" max="4" value="${question.airportD.IATA}" onBlur="setAirport(document.forms[0].airportD, this.value); updateSIDSTAR(document.forms[0].sid, this.value, 'sid')" /></td>
</tr>
<c:if test="${empty question}">
<tr>
 <td class="label">SID</td>
 <td class="data"><el:combo name="sid" size="1" options="${emptyList}" firstEntry="-" onChange="void plotMap()" /></td>
</tr>
</c:if>
<tr>
 <td class="label">Arriving at</td>
 <td class="data"><el:combo name="airportA" size="1" options="${airports}" firstEntry="-" value="${question.airportA}" className="req" onChange="changeAirport(this); updateSIDSTAR(document.forms[0].star, getValue(this), 'star')" />
 <el:text ID="airportACode" name="airportACode" idx="*" size="3" max="4" value="${question.airportA.IATA}" onBlur="setAirport(document.forms[0].airportA, this.value); updateSIDSTAR(document.forms[0].star, this.value, 'star')" /></td>
</tr>
<c:if test="${empty question}">
<tr>
 <td class="label">STAR</td>
 <td class="data"><el:combo name="star" size="1" options="${emptyList}" firstEntry="-" onChange="void plotMap()" /></td>
</tr>
</c:if>
<tr>
 <td class="label" valign="top">Flight Route</td>
 <td class="data"><el:textbox name="route" idx="*" width="90%" className="req" height="2" onBlur="void plotMap()">${question.correctAnswer}</el:textbox></td>
</tr>
<tr>
 <td class="label" valign="top">Correct Answer</td>
 <td class="data"><el:textbox name="routeCodes" idx="*" width="90%" height="2" readOnly="true">${question.correctAnswer}</el:textbox></td>
</tr>
<tr>
 <td class="label" valign="top">Route Map</td>
 <td class="data"><map:div ID="googleMap" x="100%" y="320" /></td>
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
<script language="JavaScript" type="text/javascript">
var doRunways = false;
<map:point var="mapC" point="${mapCenter}" />
<c:set var="mapDistance" value="${(empty question) ? 300 : question.distance}" scope="request" />
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

<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html lang="en">
<head>
<title><content:airline /> Senior Captain Nominations</title>
<content:css name="main" />
<content:css name="form" />
<content:css name="view" />
<content:json />
<content:js name="common" />
<content:js name="scNominate" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script async>
golgotha.local.validate = function(f)
{
if (!golgotha.form.check()) return false;
<content:filter roles="HR">
const act = f.action;
if (act.indexOf('scnompurge.do') != -1) {
	if (!confirm('Are you sure you wish to purge all Senior Captain nominations?')) return false;
	golgotha.form.submit(f);
	return true;
}
</content:filter>
if (!f.id) return false;
if (f.id.selectedIndex == 0)
	throw new golgotha.event.ValidationError('Please select the Pilot you wish to nominate.', f.id);

golgotha.form.validate({f:f.body, l:30, t:'Nomination Comments'});

// Confirm
const pilotName = f.id.options[f.id.selectedIndex].pilotName;
if (!confirm('Are you sure you wish to nominate ' + pilotName + ' for Senior Captain?')) return false;
golgotha.form.submit(f);
return true;	
};
</script>
</head>
<content:copyright visible="false" />
<body onload="void golgotha.sc.getPilots()">
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="maxNoms" name="users.sc.maxNominations" default="5" />
<content:sysdata var="minFlights" name="users.sc.minFlights" default="5" />
<content:sysdata var="minAge" name="users.sc.minAge" default="120" />
<content:empty var="emptyList" />
<c:set var="cspan" value="${canSeeScore ? 8 : 6}" scope="page" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="scnominate.do" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="view">

<!-- Header Bar -->
<tr class="title caps">
 <td colspan="${cspan}" class="left"><content:airline />&nbsp;${qtr} SENIOR CAPTAIN NOMINATIONS</td>
</tr>
<tr>
 <td colspan="${cspan}" class="left">Promotion to the rank of <content:airline /> Senior Captain is the highest rank available for pilots without being involved in the day to day operations  
management of our virtual airline. Unlike other promotions, it is granted based on qualitative achievement over time rather than written examinations and check rides. To become a Senior
Captain at <content:airline />, one needs to be nominated to the position along with a brief description of what the individual has done to make <content:airline /> a better place. Since this  
is a qualitative process, there are no specific restrictions or requirements as to what a nomination should contain - it's up to you to best communicate how a member makes our virtual airline better.<br />
<br />
Keep in mind that you are limited to <fmt:int value="${maxNoms}" /> Senior Captain nominations per quarter, so please be judicious in who you nominate. This also isn't a popularity contest - 
promoting a member to Senior Captain is the prerogative of the <content:airline /> staff, and this process is designed to bring attention to people who otherwise might get missed. The modest, 
helpful type is just the person we're looking for!<br />
<br />
We look forward to your help in recognizing those who make <content:airline /> a better place for all its members!</td>
</tr>

<c:set var="showHeader" value="${!empty allNoms || !empty myEQNoms || !empty myNoms}" scope="page" />
<c:if test="${showHeader}">
<!-- Header Bar -->
<tr class="title caps">
 <td style="width:20%">PILOT NAME</td>
 <td style="width:10%">EQUIPMENT TYPE</td>
 <td style="width:10%">JOINED ON</td>
 <td style="width:15%">LEGS / HOURS</td>
 <td style="width:15%">ONLINE</td>
 <td style="width:15%">ACARS</td>
<c:if test="${canSeeScore}">
 <td>NOMINATED ON</td>
 <td style="width:5%">SCORE</td>
</c:if>
</tr>
</c:if>
<content:filter roles="HR">
<!-- All Senior Captain nominations -->
<c:forEach var="nomStatus" items="${fn:keys(allNoms)}">
<c:set var="noms" value="${allNoms[nomStatus]}" scope="page" />
<c:if test="${!empty noms}">
<tr class="title caps">
 <td class="left" colspan="${cspan}"><fmt:int value="${fn:sizeof(noms)}" />&nbsp;${nomStatus} PILOTS - <a id="toggleAll" href="javascript:void golgotha.sc.toggleAll()">SHOW ALL COMMENTS</a></td>
</tr>
<c:forEach var="nom" items="${noms}">
<c:set var="pilot" value="${pilots[nom.ID]}" scope="page" />
<view:row entry="${nom}">
 <td><a id="tc${nom.ID}" class="ncToggle plain" onclick="javascript:void golgotha.sc.toggleComments(${nom.ID})"> + </a><el:cmd url="scnominate" link="${nom}" className="pri bld">${pilot.name}</el:cmd></td>
 <td class="sec bld">${pilot.equipmentType}</td>
 <td class="small"><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
 <td class="bld"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td class="sec"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
 <td><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours</td>
 <td class="small bld"><fmt:date fmt="d" date="${nom.createdOn}" /></td>
 <td class="pri bld"><fmt:int value="${nom.score}" /></td>
</view:row>
<c:forEach var="nc" items="${nom.comments}">
<c:set var="author" value="${pilots[nc.authorID]}" scope="page" />
<view:row entry="${nc}" className="nc-${nom.ID}" style="display:none;">
 <td colspan="${cspan}" class="small left"><span class="bld">${author.rank.name}</span> <span class="pri bld">${author.name}</span>
 (${author.pilotCode}) - <fmt:msg value="${nc.body}" /></td>
</view:row>
</c:forEach>
</c:forEach>
</c:if>
</c:forEach>
</content:filter>
<c:if test="${!empty myEQNoms}">
<tr class="title caps">
 <td class="left" colspan="${cspan}"><fmt:int value="${fn:sizeof(myEQNoms)}" />&nbsp;${user.equipmentType} PILOTS NOMINATED</td>
</tr>
<c:forEach var="nom" items="${myEQNoms}">
<c:set var="pilot" value="${pilots[nom.ID]}" scope="page" />
<c:set var="ac" value="${acMap[nom]}" scope="page" />
<view:row entry="${nom}">
<c:choose>
<c:when test="${ac.canRead}">
 <td><el:cmd url="scnominate" link="${nom}" className="pri bld">${pilot.name}</el:cmd></td>
</c:when>
<c:otherwise>
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd></td>
</c:otherwise>
</c:choose>
 <td class="sec bld">${pilot.equipmentType}</td>
 <td class="small"><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
 <td class="bld"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td class="sec"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
 <td><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours</td>
 <td class="small bld"><fmt:date fmt="d" date="${nom.createdOn}" /></td>
 <td class="pri bld"><fmt:int value="${nom.score}" /></td>
</view:row>
<c:forEach var="nc" items="${nom.comments}">
<c:set var="author" value="${pilots[nc.authorID]}" scope="page" />
<c:if test="${!fn:hasRole('HR', author) || !nc.support}">
<view:row entry="${nc}" className="nc-${nom.ID}">
 <td colspan="${cspan}" class="small left"><span class="bld">${author.rank.name}</span> <span class="pri bld">${author.name}</span>
 (${author.pilotCode}) - <fmt:msg value="${nc.body}" /></td>
</view:row>
</c:if>
</c:forEach>
</c:forEach>
</c:if>
<c:if test="${!empty myNoms}">
<!-- My Nominations -->
<tr class="title caps">
 <td class="left" colspan="${cspan}">MY SENIOR CAPTAIN NOMINATIONS - <fmt:int value="${fn:sizeof(myNoms)}" /> PILOTS NOMINATED</td>
</tr>
<c:forEach var="nom" items="${myNoms}">
<c:set var="pilot" value="${pilots[nom.ID]}" scope="page" />
<c:set var="ac" value="${acMap[nom]}" scope="page" />
<tr>
<c:choose>
<c:when test="${ac.canUpdate || ac.canDispose}">
 <td><el:cmd url="scnominate" link="${nom}" className="pri bld">${pilot.name}</el:cmd></td>
</c:when>
<c:otherwise>
 <td><el:cmd url="profile" link="${pilot}" className="pri bld">${pilot.name}</el:cmd></td>
</c:otherwise>
</c:choose>
 <td class="sec bld">${pilot.equipmentType}</td>
 <td class="small"><fmt:date fmt="d" date="${pilot.createdOn}" /></td>
 <td class="bld"><fmt:int value="${pilot.legs}" /> legs, <fmt:dec value="${pilot.hours}" /> hours</td>
 <td class="sec"><fmt:int value="${pilot.onlineLegs}" /> legs, <fmt:dec value="${pilot.onlineHours}" /> hours</td>
 <td><fmt:int value="${pilot.ACARSLegs}" /> legs, <fmt:dec value="${pilot.ACARSHours}" /> hours</td>
<c:if test="${canSeeScore}">
 <td class="small bld"><fmt:date fmt="d" date="${nom.createdOn}" /></td>
 <td class="pri bld"><fmt:int value="${nom.score}" /></td>
</c:if>
</tr>
<c:forEach var="nc" items="${nom.comments}">
<c:set var="author" value="${pilots[nc.authorID]}" scope="page" />
<c:if test="${author.ID == user.ID}">
<view:row entry="${nc}">
 <td colspan="${cspan}" class="small left"><fmt:msg value="${nc.body}" /></td>
</view:row>
</c:if>
</c:forEach>
</c:forEach>
</c:if>
<c:if test="${showHeader}">
<tr class="title">
 <td colspan="${cspan}">&nbsp;</td>
</tr>
</c:if>
</el:table>
<c:choose>
<c:when test="${!access.canNominate}">
<br />
Sorry, you are unable to nominate anyone for the rank of Senior Captain at this time. You need to have logged at least <fmt:int value="${minFlights}" /> flight legs and have been an active member at  
<content:airline /> for at least <fmt:int value="${minAge}" /> days. In order to best understand the qualities needed to improve your virtual airline, it's best to have been a member for a while!<br /> 
<br />
</c:when>
<c:when test="${(fn:sizeof(myNoms) >= maxNoms) && !access.canNominateUnlimited}">
<br />
Sorry, but you have already nominated <fmt:int value="${maxNoms}" /> <content:airline /> pilots for promotion to Senior Captain this calendar quarter. In order to ensure that nominations are reserved 
for the most deserving individuals, we limit the number of nomnations that can be made every quarter.<br />
<br />
</c:when>
<c:otherwise>
<!-- New Nomination form -->
<el:table className="form">
<tr class="title caps">
 <td colspan="2">NEW <content:airline /> SENIOR CAPTAIN NOMINATION</td>
</tr>
<tr>
 <td class="label">Pilot</td>
 <td class="data">
<div id="rowSelectPilot" style="display:none;"><el:combo ID="selectPilot" name="id" idx="*" size="1" className="req" firstEntry="[ SELECT PILOT ]" options="${emptyList}" onChange="void golgotha.sc.setPilot(this)" />
 <el:text name="pilotSearch" idx="*" size="12" max="24" value="" onChange="void golgotha.sc.search(this.value)" />&nbsp;<span class="small ita">(Type the first few letters of an eligible Pilot's name to jump to them in the list.)</span></div>
<div id="rowLoading" class="bld caps">LOADING ELIGIBLE PILOT LIST, PLEASE WAIT...</div>
<div id="rowError" class="bld error caps" style="display:none;">ERROR LOADING ELIGIBLE PILOT LIST <span id="errorCode"></span> <el:button ID="RefreshButton" label="RELOAD" onClick="void golgotha.sc.getPilots()" /></div>
</td>
</tr>
<tr id="rowComments" style="display:none;">
 <td class="label top">Comments</td>
 <td class="data"><el:textbox name="body" idx="*" width="90%" resize="true" height="5"></el:textbox></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><el:button type="submit" label="SAVE SENIOR CAPTAIN NOMINATION" />
<content:filter roles="HR">
&nbsp;<el:cmdbutton url="scnompurge" label="PURGE SENIOR CAPTAIN NOMINATIONS" />
<c:if test="${prevQuarterPending}">
&nbsp;<el:cmdbutton url="scnompostpone" label="MOVE OLD PENDING NOMINATIONS" /></c:if>
&nbsp;<el:cmdbutton url="scnomrescore" label="RESCORE SENIOR CAPTAIN NOMINATIONS" /></content:filter></td>
</tr>
</el:table>
<el:text name="support" type="hidden" value="true" />
</c:otherwise>
</c:choose>
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page session="false" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
<title><content:airline /> Water Cooler - ${thread.subject}</title>
<content:css name="main" browserSpecific="true" />
<content:css name="cooler" />
<content:css name="form" />
<c:forEach var="domain" items="${userDomains}">
<content:css host="www.${domain}" name="signature" browserSpecific="true" ie7suffix="ie7" />
</c:forEach>
<content:pics />
<content:js name="common" />
<content:filter roles="Moderator"><content:js name="datePicker" /></content:filter>
<script language="JavaScript" type="text/javascript">
function validate(form)
{
if (!checkSubmit()) return false;

// Validate response
var act = form.action;
if (act.indexOf('imgvote.do') != -1) {
	if (!validateCombo(form.score, 'Image Rating')) return false;
} else if (act.indexOf('threadmove.do') != -1) {
	if (!validateCombo(form.newChannel, 'Channel Name')) return false;
} else if (act.indexOf('threadsubjectedit.do') != -1) {
	if (!validateText(form.newTitle, 5, 'New Discussion Thread Title')) return false;
} else {
	var hasResponse = (form.msgText.value.length > 3);
	if (!hasResponse)
		if (!validateCheckBox(form.pollVote, 1, 'poll Vote')) return false;
}

setSubmit();
disableButton('SaveButton');
disableButton('LockButton');
disableButton('HideButton');
disableButton('UnlockButton');
disableButton('UnstickButton');
disableButton('StickButton');
disableButton('VoteButton');
disableButton('ImgDeleteButton');
disableButton('DeleteButton');
disableButton('MoveButton');
disableButton('CalendarButton');
disableButton('EmoticonButton');
return true;
}

function openEmoticons()
{
var flags = 'height=280,width=250,menubar=no,toolbar=no,status=no,scrollbars=yes';
var w = window.open('emoticons.do', 'emoticonHelp', flags);
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body onload="void initLinks()">
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>
<c:set var="serverName" value="${pageContext.request.serverName}" scope="request" />
<c:set var="user" value="${pageContext.request.userPrincipal}" scope="request" />
<content:sysdata var="imgPath" name="path.img" />
<content:sysdata var="ccLevels" name="centuryClubLevels" />
<c:set var="postCount" value="${fn:sizeof(thread.posts)}" scope="request" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="threadReply.do" linkID="0x${thread.ID}" method="post" validate="return validate(this)">
<el:table className="thread form" pad="default" space="default">
<!-- Thread Header -->
<tr class="title">
 <td colspan="3" class="left caps"><el:cmd className="title" url="channels"><content:airline />
 WATER COOLER</el:cmd> | <el:cmd className="title" url="channel" linkID="${thread.channel}">${thread.channel}</el:cmd> |
 ${thread.subject}<c:if test="${access.canReport && (postCount > 1)}">
 ( <el:cmd url="threadreport" linkID="0x${thread.ID}" className="small">WARN MODERATORS</el:cmd> )</c:if></td>
</tr>
<c:if test="${!empty thread.stickyUntil}">
<!-- Thread Sticky Date Information -->
<tr class="title caps">
 <td colspan="3" class="mid">This Discussion Thread is Stuck until <fmt:date fmt="d" date="${thread.stickyUntil}" /></td>
</tr>
</c:if>
<%@ include file="/jsp/cooler/threadImg.jspf" %>
<%@ include file="/jsp/cooler/threadPoll.jspf" %>

<!-- Thread Posts -->
<c:set var="postIdx" value="${0}" scope="request" />
<c:set var="contentWarn" value="${false}" scope="request" />
<c:forEach var="msg" items="${thread.posts}">
<!-- Response 0x<fmt:hex value="${msg.ID}" /> -->
<c:set var="pilot" value="${pilots[msg.authorID]}" scope="request" />
<c:set var="isPilot" value="${fn:contains(pilot.roles, 'Pilot')}" scope="request" />
<c:set var="pilotLoc" value="${userData[msg.authorID]}" scope="request" />
<c:set var="postIdx" value="${postIdx + 1}" scope="request" />
<c:set var="canEdit" value="${access.canEdit && (postIdx == postCount)}" scope="request" />
<c:set var="contentWarn" value="${contentWarn || msg.contentWarning}" scope="request" />
<tr>
 <td rowspan="2" class="postInfo small">
<c:if test="${isPilot}">
 <el:profile location="${pilotLoc}">${pilot.name}</el:profile><br />
<c:if test="${!empty pilot.pilotCode}"><span class="sec bld">${pilot.pilotCode}</span><br /></c:if>
 <span class="caps bld">${pilot.rank}</span>, ${pilot.equipmentType}<br />
<c:if test="${!empty pilot.certifications}"><span class="ter bld">
<fmt:list value="${pilot.certifications}" delim=", " /></span><br /></c:if>
<el:showaddr user="${pilot}"><el:email user="${pilot}" className="small caps" label="E-MAIL" /><br /></el:showaddr>
<br />
Joined on <fmt:date d="MMMM dd yyyy" fmt="d" date="${pilot.createdOn}" /><br />
<c:choose>
<c:when test="${pilot.legs >= 1500}">
<font color="#AF2020"><b>${ccLevels['CC1500']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 1000}">
<font color="#AF2020"><b>${ccLevels['CC1000']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 800}">
<font color="#6060BF"><b>${ccLevels['CC800']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 700}">
<font color="#6060BF"><b>${ccLevels['CC700']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 600}">
<font color="#6060BF"><b>${ccLevels['CC600']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 500}">
<font color="#8080AF"><b>${ccLevels['CC500']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 400}">
<font color="#408090"><b>${ccLevels['CC400']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 300}">
<font color="#308060"><b>${ccLevels['CC300']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 200}">
<font color="#805020"><b>${ccLevels['CC200']}</b></font><br />
</c:when>
<c:when test="${pilot.legs >= 100}">
<font color="#0000A1"><b>${ccLevels['CC100']}</b></font><br />
</c:when>
</c:choose>
<c:if test="${!empty pilot.motto}">
<span class="small"><i>"${pilot.motto}"</i></span><br />
</c:if>
<br />
<c:if test="${pilot.legs > 0}">
<b><fmt:int fmt="#,##0" value="${pilot.legs}" /></b> legs, <b><fmt:dec fmt="#,##0.0" value="${pilot.hours}" /></b> hours total<br />
</c:if>
<c:if test="${pilot.onlineLegs > 0}">
<span class="sec"><b><fmt:int fmt="#,##0" value="${pilot.onlineLegs}" /></b> legs, <b>
<fmt:dec fmt="#,##0.0" value="${pilot.onlineHours}" /></b> hours online</span><br />
</c:if>
<content:filter roles="Moderator">
<fmt:int fmt="#,##0" value="${postStats[pilot.ID]}" /> total posts<br />
</content:filter>
<content:activeUser user="${msg.authorID}">
<span class="ter small bld">CURRENTLY LOGGED IN</span><br />
</content:activeUser>
</c:if>
<c:if test="${!isPilot}">
<span class="pri bld">${pilot.name}</span><br />
<span class="caps">APPLICANT</span><br />
</c:if>
<el:showaddr user="${pilot}">
<c:if test="${!empty pilot.IMHandle['AOL']}">
<a href="aim:goim?screenname=${pilot.IMHandle['AOL']}"><img border="0" src="http://big.oscar.aol.com/${pilot.IMHandle['AOL']}?on_url=http://${serverName}/${imgPath}/im/aimonline.png&off_url=http://${serverName}/${imgPath}/im/aimoffline.png" alt="AIM Status" /></a>
</c:if>
<c:if test="${!empty pilot.IMHandle['MSN']}">
<a href="msnim:chat?contact=${pilot.IMHandle['MSN']}"><img border="0" src="http://blockchecker.msnfanatic.com/status/${pilot.IMHandle['MSN']}.gif" alt="My MSN status" /></a>
</c:if>
</el:showaddr></td>
 <td class="postDate" colspan="${((access.canDelete && (postCount > 1)) || canEdit) ? '1' : '2'}">Post created on <fmt:date date="${msg.createdOn}" d="MMMM dd yyyy" />
<content:filter roles="Moderator,HR">
 from ${msg.remoteAddr} (${msg.remoteHost}) <c:if test="${msg.contentWarning}"><span class="error bld">CONTENT 
WARNING</span></c:if>
</content:filter>
<c:choose>
<c:when test="${canEdit}">
 </td>
 <td class="postEdit"><el:cmd className="pri bld small" url="thread" linkID="0x${thread.ID}" op="edit">EDIT POST</el:cmd>
</c:when>
<c:when test="${access.canDelete && (postCount > 1)}">
 </td>
 <td class="postEdit"><el:cmd className="pri error small" url="postkill" linkID="0x${thread.ID}" op="${fn:hex(msg.ID)}">KILL POST</el:cmd>
</c:when>
</c:choose></td>
</tr>
<tr>
 <td class="postBody" colspan="2"><fmt:msg value="${msg.body}" filter="${!noFilter && msg.contentWarning}" />
<c:if test="${isPilot && (pilot.hasSignature || pilot.hasDefaultSignature)}">
<br />
<c:choose>
<c:when test="${pilot.hasSignature}">
<!-- Signature Image for ${pilot.name} -->
<br />
<img src="/sig/${pilotLoc.DB}/0x<fmt:hex value="${pilot.ID}" />.${pilot.signatureExtension}" alt="${pilot.name} (${pilot.pilotCode})" /><br />
</c:when>
<c:when test="${pilot.hasDefaultSignature}">
<!-- Default Signature Image -->
<el:table className="${pilotLoc.airlineCode}_defaultSig" pad="0"><tr>
 <td valign="bottom" class="sig" style="background-image: url(http://www.${pilotLoc.domain}/${imgPath}/sig/${fn:lower(pilot.equipmentType)}.png);">
 <div class="${pilotLoc.airlineCode}_defaultSigText"><h2>${pilot.name}</h2><span class="pri bld ${pilotLoc.airlineCode}_defaultSig caps">${pilot.rank}, ${pilot.equipmentType}</span></div>
 </td>
</tr></el:table>
</c:when> 
</c:choose>
</c:if>
 </td>
</tr>
</c:forEach>
<content:filter roles="HR,Moderator"><c:if test="${!empty thread.updates}">
<!-- Thread Status History -->
<tr class="title caps">
 <td colspan="3">THREAD STATUS HISTORY</td>
</tr>
<c:forEach var="update" items="${thread.updates}">
<c:set var="pilot" value="${pilots[update.authorID]}" scope="request" />
<tr>
 <td class="mid small"><fmt:date date="${update.date}" t="HH:mm" /></td>
 <td colspan="2">${update.message} by <span class="pri bld">${pilot.name}</span></td>
</tr>
</c:forEach>
</c:if></content:filter>
<c:if test="${access.canLock || access.canUnlock || access.canDelete || access.canUnstick}">
<!-- Moderator Tools -->
<tr class="title caps">
 <td colspan="3">MODERATOR TOOLS</td>
</tr>
<tr class="pri bld mid">
 <td colspan="3">
<c:if test="${access.canLock}">
 <el:cmdbutton ID="LockButton" label="LOCK" url="threadlock" linkID="0x${thread.ID}" op="lock" />
 <el:cmdbutton ID="HideButton" label="HIDE" url="threadlock" linkID="0x${thread.ID}" op="hide" />
</c:if>
<c:if test="${access.canUnlock}">
 <el:cmdbutton ID="UnlockButton" label="UNLOCK" url="threadunlock" linkID="0x${thread.ID}" op="unlock" />
 <el:cmdbutton ID="UnhideButton" label="UNHIDE" url="threadunlock" linkID="0x${thread.ID}" op="unhide" />
</c:if>
<c:if test="${imgAccess.canDelete}">
 <el:cmdbutton ID="ImgDeleteButton" label="DELETE IMAGE" url="imgdelete" linkID="0x${img.ID}" />
</c:if>
<content:filter roles="Moderator"><c:if test="${contentWarn || (thread.reportCount > 0)}">
 <el:cmdbutton ID="UnfilterButton" label="CLEAR WARNINGS" url="clearcontentwarn" linkID="0x${thread.ID}" />
</c:if></content:filter>
<c:if test="${access.canUnstick}">
 <el:cmdbutton ID="UnstickButton" label="UNSTICK" url="unstick" linkID="0x${thread.ID}" />
</c:if>
<c:if test="${access.canDelete}">
 <el:cmdbutton ID="DeleteButton" label="DELETE THREAD" url="threadkill" linkID="0x${thread.ID}" />
</c:if>
<content:filter roles="Moderator">
 MOVE TO <el:combo name="newChannel" idx="*" size="1" options="${channels}" firstEntry="-" value="${thread.channel}" />
 <el:cmdbutton ID="MoveButton" label="MOVE" url="threadmove" post="true" linkID="0x${thread.ID}" />
 STICK UNTIL <el:text name="stickyDate" idx="*" size="9" max="10" value="${stickyDate}" />
<c:if test="${user.dateFormat == 'MM/dd/yyyy'}">
 <el:button ID="CalendarButton" label="CALENDAR" className="BUTTON" onClick="void show_calendar('forms[0].stickyDate')" />
</c:if>
 <el:cmdbutton ID="StickButton" label="STICK" url="threadstick" post="true" linkID="0x${thread.ID}" />
</content:filter>
 </td>
</tr>
</c:if>
<content:filter roles="Pilot">
<!-- Message Thread Update notification -->
<tr class="title caps">
 <td colspan="3">UPDATE NOTIFICATIONS</td>
</tr>
<tr class="pri bld mid">
 <td colspan="3">You will <c:if test="${!doNotify}"><u><i>NOT</i></u> </c:if>receive an e-mail 
notification each time a reply is posted in this Thread.
<el:cmdbutton url="notifytoggle" linkID="0x${thread.ID}" label="${doNotify ? 'DISABLE' : 'ENABLE'} NOTIFICATIONS" /> 
<content:filter roles="Moderator">
<c:if test="${!empty notify.IDs}"><el:cmdbutton url="notifyclear" linkID="0x${thread.ID}" label="RESET NOTIFICATIONS" /></c:if>
</content:filter></td>
</tr>
</content:filter>
<c:if test="${access.canEditTitle}">
<!-- Update Thread Title -->
<tr class="title caps">
 <td colspan="3">UPDATE DISCUSSION THREAD TITLE</td>
</tr>
<tr class="pri bld mid">
 <td colspan="3">Update to <el:text name="newTitle" idx="*" size="64" max="96" value="${thread.subject}" />
 <el:cmdbutton url="threadsubjectedit" linkID="0x${thread.ID}" post="true" label="UPDATE" /></td>
</tr>
</c:if>

<c:if test="${access.canReply}">
<!-- Message Thread Response -->
<tr class="title caps">
 <td colspan="3">${doEdit ? 'EDIT MY POST' : 'NEW RESPONSE'}</td>
</tr>
<tr class="mid">
 <td colspan="3"><el:textbox name="msgText" width="90%" height="8">${lastPost.body}</el:textbox></td>
</tr>
</c:if>

<!-- Button Bar -->
<c:if test="${access.canReply || access.canReport}">
<tr class="buttons mid title">
 <td colspan="3"><c:if test="${access.canReply}"><el:button className="BUTTON" ID="SaveButton" label="SAVE RESPONSE" type="submit" />
&nbsp;<el:button ID="EmoticonButton" className="BUTTON" onClick="void openEmoticons()" label="EMOTICONS" /></c:if></td>
</tr>
</c:if>
</el:table>
<el:text name="doEdit" type="hidden" value="${doEdit}" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
</body>
</html>

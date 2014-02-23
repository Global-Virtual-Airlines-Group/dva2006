<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title>New <content:airline /> ${forumName} Disucssion Thread</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:json />
<content:js name="common" />
<content:js name="datePicker" />
<script type="text/javascript">
var imgData = {URLs: [], descs: [], maxSeq:0, seq:[]};
imgData.add = function(url, desc) { this.URLs.push(url); this.descs.push(desc); this.maxSeq++; this.seq.push(this.maxSeq); }
imgData.contains = function(url) { return (this.URLs.indexOf(url) > -1); }
imgData.getIndex = function(seq) { return this.seq.indexOf(seq); }
imgData.size = function() { return this.URLs.length; }
imgData.remove = function(idx) { this.URLs.splice(idx, 1); this.descs.splice(idx, 1); this.seq.splice(idx, 1); }

function validate(form)
{
if ((!form) || (!checkSubmit())) return false;
if (!validateText(form.subject, 8, 'Title of your Thread')) return false;
if (!validateText(form.msgText, 5, 'text of your Message')) return false;
if (!validateFile(form.img, 'gif,jpg,png', 'Attached Image')) return false;

setSubmit();
disableButton('EmoticonButton');
disableButton('LinkButton');
disableButton('SaveButton');
return true;
}

function openEmoticons()
{
var flags = 'height=280,width=250,menubar=no,toolbar=no,status=no,scrollbars=yes';
var w = window.open('emoticons.do', 'emoticonHelp', flags);
return true;
}

function enablePoll()
{
var f = document.forms[0];
if (!f.hasPoll) return false;
f.pollOptions.disabled = !f.hasPoll.checked;
return true;
}

function removeLink(seq)
{
var idx = imgData.getIndex(seq);
imgData.remove(idx);
document.forms[0].imgData.value = JSON.stringify(imgData);
var r = document.getElementById('linkImg' + seq);
r.parentNode.removeChild(r);
return true;
}

function submitImage()
{
var f = document.forms[0];
if (!validateText(f.imgURL, 12, 'URL of your Linked Image')) return false;
if (!validateText(f.imgDesc, 6, 'Description of your Linked Image')) return false;

// Check extensions
var imgURL = f.imgURL.value;
var allowedExts = ['gif', 'jpg', 'jpeg', 'png'];
var ext = imgURL.substring(imgURL.lastIndexOf('.') + 1).toLowerCase();

// Clear message
var msgSpan = document.getElementById('imgLinkMsg');
msgSpan.innerHTML = '';

// Check the image has been added already
if (imgData.contains(imgURL)) {
	alert('This image has already been linked.');
	return false;
} else if (!allowedExts.contains(ext)) {
	alert('This does not appear to be an image.');
	return false;
}

// Check the image itself
var img = new Image();
img.onload = function() {
	imgData.add(imgURL, f.imgDesc. value);
	var imgIdx = imgData.maxSeq;

	// Add the image
	var r = document.createElement('tr');
	r.setAttribute('id', 'linkImg' + imgIdx);
	var ld = document.createElement('td');
	ld.setAttribute('colspan', '2');
	ld.setAttribute('class', 'mid');
	var img = document.createElement('img');
	img.setAttribute('alt', f.imgDesc.value);
	img.setAttribute('title', f.imgDesc.value);
	img.setAttribute('src', imgURL);
	ld.appendChild(img);
	ld.appendChild(document.createElement('br'));
	ld.appendChild(document.createTextNode(f.imgDesc.value + ' - '));
	var rmvLink = document.createElement('a');
	rmvLink.setAttribute('class', 'small caps');
	rmvLink.setAttribute('onclick', 'javascript:void removeLink(' + imgIdx + ')');
	rmvLink.appendChild(document.createTextNode('Remove Linked Image'));
	ld.appendChild(rmvLink);
	r.appendChild(ld);

	// Add to the DOM
	var ref = document.getElementById('imgLink');
	ref.parentNode.insertBefore(r, ref);

	// Convert to JSON
	f.imgData.value = JSON.stringify(imgData);

	// Clear the fields
	f.imgURL.value = '';
	f.imgDesc.value = '';
	return true;	
}

img.onerror = function() { msgSpan.innerHTML = 'Canot load image!'; return false; }
img.src = imgURL;
return true;
}
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/cooler/header.jspf" %> 
<%@ include file="/jsp/cooler/sideMenu.jspf" %>
<content:sysdata var="maxX" name="cooler.img_max.x" />
<content:sysdata var="maxY" name="cooler.img_max.y" />
<content:sysdata var="maxSize" name="cooler.img_max.size" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="threadpost.do" method="post" allowUpload="true" validate="return validate(this)">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="left caps">New ${forumName} Discusion Thread</td>
</tr>
<tr>
 <td class="label">Thread Title</td>
 <td class="data"><el:text name="subject" className="pri bld" required="true" idx="*" size="64" max="80" value="${param.subject}" /></td>
</tr>
<tr>
 <td class="label">${forumName} Channel</td>
 <td class="data"><el:combo name="id" idx="*" size="1" options="${channels}" value="${empty param.id ? 'General Aviation Discussion' : param.id}" /></td>
</tr>
<content:filter roles="Moderator,Operations,PIREP,HR,Examination">
<tr>
 <td class="label">Sticky Until</td>
 <td class="data"><el:text name="stickyDate" idx="*" size="10" max="11" value="${param.stickyDate}" />
<c:if test="${user.dateFormat == 'MM/dd/yyyy'}">
 <el:button label="CALENDAR" onClick="void show_calendar('forms[0].stickyDate')" /></c:if></td>
</tr>
</content:filter>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="updateNotify" idx="*" label="Send e-mail when responses are posted" value="true" />&nbsp;
<el:button ID="EmoticonButton" onClick="void openEmoticons()" label="EMOTICONS" /></td>
</tr>
<tr id="imgUpload">
 <td class="label">Upload Image</td>
 <td class="data"><el:file name="img" className="small" idx="*" size="64" max="144" onChange="void toggleImgOptions(this)" />
<c:if test="${imgBadSize}"><div class="error bld">Your attached image was too large (<fmt:int value="${imgSize}" /> bytes).</div></c:if>
<c:if test="${imgBadDim}"><div class="error bld">Your attached image was too large (<fmt:int value="${imgX}" />
 by <fmt:int value="${imgY}" /> pixels).</div></c:if>
<c:if test="${imgInvalid}"><div class="error bld">Your attached image is in an unknown format.</div></c:if></td>
</tr>
<tr class="title caps">
 <td colspan="2">LINKED IMAGES</td>
</tr>
<tr id="imgLink">
 <td class="label">New Image URL</td>
 <td class="data"><el:text name="imgURL" className="small" idx="*" size="64" max="192" value="" />
&nbsp;<span id="imgLinkMsg" class="small error bld"></span></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td class="data"><el:text name="imgDesc" idx="*" size="64" max="192" value="" /> 
<el:button ID="LinkButton" label="LINK IMAGE" onClick="void submitImage()" /></td>
</tr>
<content:filter roles="PIREP,HR,Instructor,Operations,Moderator">
<!-- Pilot Poll -->
<tr class="title caps">
 <td colspan="2">PILOT POLL</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="hasPoll" idx="*" value="true" onChange="void enablePoll()" label="Enable Pilot Poll in this Discussion Thread" /></td>
</tr>
<tr>
 <td class="label top">Poll Options</td>
 <td class="data"><el:textbox name="pollOptions" idx="*" width="60" height="4" resize="true"></el:textbox></td>
</tr>
</content:filter>

<!-- Message Text -->
<tr class="title caps">
 <td colspan="2">NEW MESSAGE TEXT</td>
</tr>
<tr>
 <td class="mid" colspan="2"><el:textbox name="msgText" required="true" idx="*" width="90%" height="5" resize="true" spellcheck="true">${param.msgText}</el:textbox></td>
</tr>

<!-- Button Bar -->
<tr class="title mid">
 <td colspan="2">
<c:if test="${channelAccess.canPost}">
 <el:button ID="SaveButton" label="SAVE NEW THREAD" type="submit" />
</c:if>
 </td>
</tr>
</el:table>
<el:text name="imgData" type="hidden" value="" />
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

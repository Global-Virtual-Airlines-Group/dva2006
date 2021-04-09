<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<content:sysdata var="forumName" name="airline.forum" />
<html lang="en">
<head>
<title>New <content:airline />&nbsp;${forumName}&nbsp;Disucssion Thread</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:json />
<content:js name="common" />
<content:js name="datePicker" />
<content:captcha action="threadCreate" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.imgData = {URLs: [], descs: [], maxSeq:0, seq:[]};
golgotha.local.imgData.add = function(url, desc) { this.URLs.push(url); this.descs.push(desc); this.maxSeq++; this.seq.push(this.maxSeq); }
golgotha.local.imgData.contains = function(url) { return (this.URLs.indexOf(url) > -1); }
golgotha.local.imgData.getIndex = function(seq) { return this.seq.indexOf(seq); }
golgotha.local.imgData.size = function() { return this.URLs.length; }
golgotha.local.imgData.remove = function(idx) { this.URLs.splice(idx, 1); this.descs.splice(idx, 1); this.seq.splice(idx, 1); }

golgotha.local.validate = function(f) {
    if (!golgotha.form.check()) return false;
    golgotha.form.validate({f:f.subject, l:8, t:'Title of your Thread'});
    golgotha.form.validate({f:f.msgText, l:5, t:'Text of your Message'});
    golgotha.form.validate({f:f.img, ext:['gif','jpg','jpeg','png'], t:'Attached Image', empty:true});
    golgotha.form.submit(f);
    return true;
};

golgotha.local.openEmoticons = function() {
	return window.open('emoticons.do', 'emoticonHelp', 'height=280,width=250,menubar=no,toolbar=no,status=no,scrollbars=yes');
};

golgotha.local.enablePoll = function() {
	const f = document.forms[0];
	if (!f.hasPoll) return false;
	f.pollOptions.disabled = !f.hasPoll.checked;
	return true;
};

golgotha.local.removeLink = function(seq) {
    const idx = golgotha.local.imgData.getIndex(seq);
    golgotha.local.imgData.remove(idx);
    document.forms[0].imgData.value = JSON.stringify(golgotha.local.imgData);
    const r = document.getElementById('linkImg' + seq);
    r.parentNode.removeChild(r);
    return true;
};

golgotha.local.submitImage = function(f)
{
golgotha.form.validate({f:f.imgURL, l:12, t:'URL of your Linked Image'});
golgotha.form.validate({f:f.imgDesc, l:6, t:'Description of your Linked Image'});

// Check extensions
const imgURL = f.imgURL.value;
const allowedExts = ['gif', 'jpg', 'jpeg', 'png'];
const ext = imgURL.substring(imgURL.lastIndexOf('.') + 1).toLowerCase();

// Clear message
const msgSpan = document.getElementById('imgLinkMsg');
msgSpan.innerHTML = '';

// Check the image has been added already
if (golgotha.local.imgData.contains(imgURL))
	throw new golgotha.event.ValidationError('This image has already been linked.', f.imgURL);
if (!allowedExts.contains(ext))
	throw new golgotha.event.ValidationError('This does not appear to be an image.', f.imgURL);

// Check the image itself
var img = new Image();
img.onload = function() {
	golgotha.local.imgData.add(imgURL, f.imgDesc. value);
	const imgIdx = golgotha.local.imgData.maxSeq;

	// Add the image
	const r = document.createElement('tr');
	r.setAttribute('id', 'linkImg' + imgIdx);
	const ld = document.createElement('td');
	ld.setAttribute('colspan', '2');
	ld.setAttribute('class', 'mid');
	const img = document.createElement('img');
	img.setAttribute('alt', f.imgDesc.value);
	img.setAttribute('title', f.imgDesc.value);
	img.setAttribute('src', imgURL);
	ld.appendChild(img);
	ld.appendChild(document.createElement('br'));
	ld.appendChild(document.createTextNode(f.imgDesc.value + ' - '));
	const rmvLink = document.createElement('a');
	rmvLink.setAttribute('class', 'small caps');
	rmvLink.setAttribute('onclick', 'javascript:void golgotha.local.removeLink(' + imgIdx + ')');
	rmvLink.appendChild(document.createTextNode('Remove Linked Image'));
	ld.appendChild(rmvLink);
	r.appendChild(ld);

	// Add to the DOM
	const ref = document.getElementById('imgLink');
	ref.parentNode.insertBefore(r, ref);

	// Convert to JSON
	f.imgData.value = JSON.stringify(golgotha.local.imgData);

	// Clear the fields
	f.imgURL.value = '';
	f.imgDesc.value = '';
	return true;	
};

img.onerror = function() { msgSpan.innerHTML = 'Canot load image!'; return false; };
img.src = imgURL;
return true;
};
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
<el:form action="threadpost.do" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title">
 <td colspan="2" class="left caps">New ${forumName} Discussion Thread</td>
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
 <td class="data"><el:box name="updateNotify" idx="*" label="Send e-mail when responses are posted" value="true" />&nbsp;<el:button onClick="void golgotha.local.openEmoticons()" label="EMOTICONS" /></td>
</tr>
<tr id="imgUpload">
 <td class="label">Upload Image</td>
 <td class="data"><el:file name="img" className="small" idx="*" size="64" max="144" onChange="void golgotha.local.toggleImgOptions(this)" />
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
 <td class="data"><el:text name="imgDesc" idx="*" size="64" max="192" value="" />&nbsp;<el:button label="LINK IMAGE" onClick="void golgotha.form.wrap(golgotha.local.submitImage, document.forms[0])" /></td>
</tr>
<content:filter roles="PIREP,HR,Instructor,Operations,Moderator">
<!-- Pilot Poll -->
<tr class="title caps">
 <td colspan="2">PILOT POLL</td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="hasPoll" idx="*" value="true" onChange="void golgotha.local.enablePoll()" label="Enable Pilot Poll in this Discussion Thread" /></td>
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
 <td colspan="2">&nbsp;
<c:if test="${channelAccess.canPost}"><el:button label="SAVE NEW THREAD" type="submit" /></c:if>
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

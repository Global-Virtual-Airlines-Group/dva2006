<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_view.tld" prefix="view" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Video Library - ${(!empty video) ? video.name : 'New Video'}</title>
<content:css name="main" />
<content:css name="form" />
<content:js name="common" />
<content:js name="resumable" />
<content:js name="progress" />
<content:pics />
<content:favicon />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script>
golgotha.local.validate = function(f) {
    if (!golgotha.local.file || !golgotha.form.check()) return false;
    if (!golgotha.local.uploadComplete) {
    	f.id.value = golgotha.local.file.file.name;
    	golgotha.local.showProgress(true);
    	golgotha.local.pb.set(0.01);
    	window.setTimeout(golgotha.local.updateProgress, 50);
    	golgotha.util.display('selectFile', false);
    	golgotha.util.disable('SelectButton', true);
    	golgotha.local.r.upload();
    	return false;
    }
    
    golgotha.form.validate({f:f.title, l:10, t:'Video Title'});
    golgotha.form.validate({f:f.category, t:'Video Category'});
    golgotha.form.validate({f:f.desc, l:10, t:'Description'});
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
<content:sysdata var="cats" name="airline.video.categories" />
<content:enum var="securityOptions" className="org.deltava.beans.fleet.Security" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="tvideo.do" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
<c:choose>
<c:when test="${!empty video}">
 <td colspan="2"><content:airline /> VIDEO LIBRARY - ${video.name}</td>
</c:when>
<c:otherwise>
 <td colspan="2">NEW <content:airline /> VIDEO LIBRARY ENTRY</td>
</c:otherwise>
</c:choose>
</tr>
<tr id="selectFile">
 <td class="label top">Video File</td>
 <td class="data" style="height:64px;"><span id="dropTarget" class="ovalBorder pri ita">Drag a File here to Upload</span> <el:button ID="SelectButton" label="SELECT FILE" /></td>
</tr>
<tr>
 <td class="label">Video Title</td>
 <td class="data"><el:text name="title" className="pri bld" required="true" idx="*" size="48" max="80" value="${video.name}" /></td>
</tr>
<tr>
 <td class="label">Category</td>
 <td class="data"><el:combo name="category" idx="*" size="1" required="true" options="${cats}" value="${video.category}" firstEntry="[ CATEGORY ]" /></td>
</tr>
<tr>
 <td class="label top">Description</td>
 <td class="data"><el:textbox name="desc" idx="*" width="80%" height="3" required="true" resize="true">${video.description}</el:textbox></td>
</tr>
<c:if test="${!empty video}">
<tr>
 <td class="label">Video Information</td>
<c:if test="${video.size > 0}">
 <td class="data"><span class="pri bld">${video.type}</span>, <span class="sec bld"><fmt:int value="${video.size}" /> bytes</span></td>
</c:if>
<c:if test="${video.size == 0}">
 <td class="data warning bld caps">FILE NOT PRESENT ON FILESYSTEM</td>
</c:if>
</tr>
<tr>
 <td class="label">Statistics</td>
 <td class="data">Viewed <b><fmt:int value="${video.downloadCount}" /></b> times</td>
</tr>
</c:if>
<content:filter roles="HR,AcademyAdmin">
<tr>
 <td class="label top">Flight Academy Certifications</td>
 <td class="data"><el:check name="certNames" width="185" cols="3" className="small" newLine="true" checked="${video.certifications}" options="${certs}" /></td>
</tr>
</content:filter>
<tr>
 <td class="label">Document Security</td>
 <td class="data"><el:combo name="security" idx="*" size="1" required="true" value="${entry.security}" options="${securityOptions}" /></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="noNotify" idx="*" value="true" label="Don't send notification e-mail" /></td>
</tr>
<tr class="progress title caps" style="display:none;">
 <td colspan="2">UPLOAD PROGRESS</td>
</tr>
<tr class="progress" style="display:none;">
 <td colspan="2" class="mid"><span id="progressBar" class="ovalBorder" style="width:85%; height:32px;"></span></td>
</tr>
</el:table>

<!-- Button Bar -->
<el:table className="bar">
<tr>
 <td><c:if test="${access.canEdit || access.canCreate}"><el:button ID="SaveButton" type="submit" label="SAVE VIDEO" />
<c:if test="${!empty video}"> <el:cmdbutton ID="DeleteButton" url="tvdelete" linkID="${video.fileName}" label="DELETE VIDEO" /></c:if></c:if> </td>
</tr>
</el:table>
<el:text name="id" type="hidden" value="" />
</el:form>
<br />
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
<script async>
golgotha.util.disable('SaveButton', true);
golgotha.local.r = new Resumable({chunkSize:524288, withCredentials:true, chunkNumberParameterName:'c', chunkSizeParameterName:'cs', totalChunksParameterName:'cc', totalSizeParameterName:'ts', xhrTimeout:25000, fileType:['mp4', 'mkv', 'm4v']});
var dt = document.getElementById('dropTarget');
golgotha.local.r.assignDrop(dt);
golgotha.local.r.assignBrowse(document.getElementById('SelectButton'));
golgotha.local.r.on('fileAdded', function(f, ev) {
	golgotha.local.file = f;
    dt.innerHTML = f.file.name + ', ' + f.file.size + ' bytes';
    golgotha.local.r.opts.target = '/upload/video/' + f.file.name;
    golgotha.util.disable('SaveButton', false);
});

golgotha.local.pb = new ProgressBar.Line('#progressBar', {color:'#1a4876', text:{value:'', className:'pri', style:{color:'#ffff'}}, fill:'#1a4876'});
golgotha.local.showProgress = function(doShow) {
	var pr = golgotha.util.getElementsByClass('progress', 'tr');
	pr.forEach(function(r) { golgotha.util.display(r, doShow); });
};

golgotha.local.updateProgress = function() {
	var p = golgotha.local.r.progress();
    golgotha.local.pb.setText(Math.round(p * 100) + '% complete');
	golgotha.local.pb.animate(p, {duration: 50});
	if (p >= 1) {
		console.log('Upload Complete');
		golgotha.local.showProgress(false);
		golgotha.local.uploadComplete = true;
		document.forms[0].submit();
		return true;
	}
	
    window.setTimeout(golgotha.local.updateProgress, 65);
    return true;
};
</script>
</body>
</html>

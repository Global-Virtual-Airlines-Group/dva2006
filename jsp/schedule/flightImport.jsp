<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> Raw Schedule Import</title>
<content:css name="main" />
<content:css name="form" />
<content:pics />
<content:favicon />
<content:js name="common" />
<content:js name="resumable" />
<content:js name="progress" />
<script>
golgotha.local.validate = function(f) {
	if (!golgotha.form.check()) return false;
	golgotha.form.validate({f:f.schedType, t:'Schedule Type'});
	if (golgotha.local.isInnovata) {
		golgotha.local.uploadComplete = true;
		golgotha.form.submit(f);
		return true;
	}

	golgotha.form.validate({f:f.csvData, ext:['txt','pdf','gz'], t:'Flight Schedule data'});
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

	golgotha.form.submit(f);
	return true;
};
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/schedule/header.jspf" %> 
<%@ include file="/jsp/schedule/sideMenu.jspf" %>
<content:enum var="schedTypes" className="org.deltava.beans.schedule.ScheduleSource" exclude="MANUAL" />
<content:enum var="importPolicies" className="org.deltava.beans.schedule.ImportPolicy" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="schedimport.do" method="post" allowUpload="true" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>RAW SCHEDULE DATA UPLOAD</td>
</tr>
<tr id="selectFile">
 <td class="label top">Schedule Data</td>
 <td class="data" style="height:64px;"><span id="dropTarget" class="ovalBorder pri ita">Drag a File here to Upload</span> <el:button ID="SelectButton" label="SELECT FILE" /></td>
</tr>
<tr>
 <td class="label">Schedule Format</td>
 <td class="data"><el:combo name="schedType" idx="*" size="1" options="${schedTypes}" required="true" firstEntry="[ SCHEDULE TYPE ]" onChange="void golgotha.local.updateSource(this)" /></td>
</tr>
<tr>
 <td class="label">Duration Calculation</td>
 <td class="data"><el:combo name="importPolicy" idx="*" size="1" options="${importPolicies}" required="true" firstEntry="[ CALCULATION ]" /></td>
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
 <td><el:button ID="SaveButton" type="submit" label="UPLOAD RAW SCHEDULE DATA" /></td>
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
golgotha.local.r = new Resumable({chunkSize:524288, withCredentials:true, chunkNumberParameterName:'c', chunkSizeParameterName:'cs', totalChunksParameterName:'cc', totalSizeParameterName:'ts', xhrTimeout:25000, fileType:['pdf','txt','gz']});
const dt = document.getElementById('dropTarget');
golgotha.local.r.assignDrop(dt);
golgotha.local.r.assignBrowse(document.getElementById('SelectButton'));
golgotha.local.r.on('fileAdded', function(f, ev) {
    golgotha.local.file = f;
    dt.innerHTML = f.file.name + ', ' + f.file.size + ' bytes';
    golgotha.local.r.opts.target = '/upload/sched/' + f.file.name;
    golgotha.util.disable('SaveButton', false);
});

golgotha.local.pb = new ProgressBar.Line('#progressBar', {color:'#1a4876', text:{value:'', className:'pri', style:{color:'#ffff'}}, fill:'#1a4876'});
golgotha.local.showProgress = function(doShow) {
    const pr = golgotha.util.getElementsByClass('progress', 'tr');
    pr.forEach(function(r) { golgotha.util.display(r, doShow); });
};

golgotha.local.updateProgress = function() {
    const p = golgotha.local.r.progress();
    golgotha.local.pb.setText(Math.round(p * 100) + '% complete');
    golgotha.local.pb.animate(p, {duration:50});
    if (p >= 1) {
    	const f = document.forms[0];
        console.log('Upload Complete');
        golgotha.local.showProgress(false);
        golgotha.local.uploadComplete = true;
        golgotha.form.submit(f);
        f.submit();
        return true;
    }

    window.setTimeout(golgotha.local.updateProgress, 75);
    return true;
};

golgotha.local.updateSource = function(cb) {
	golgotha.local.isInnovata = (golgotha.form.getCombo(cb) == 'INNOVATA');
	golgotha.util.display(document.getElementById('selectFile'), !golgotha.local.isInnovata);
	const canEnable = golgotha.local.isInnovata || golgotha.local.file; 
	golgotha.util.disable(document.getElementById('SaveButton'), !canEnable);
	return true;
};
</script>
</body>
</html>

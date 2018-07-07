<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8"  session="false" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<html lang="en">
<head>
<title><content:airline /> ACARS Installer Upload</title>
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

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="acarsinstUpdate.do" op="save" method="post" validate="return golgotha.form.wrap(golgotha.local.validate, this)">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><span class="nophone"><content:airline />&nbsp;</span>ACARS INCREMENTAL INSTALLER UPDATE</td>
</tr>
<tr id="selectFile">
 <td class="label top">Installer</td>
 <td class="data" style="height:64px;"><span id="dropTarget" class="ovalBorder pri ita">Drag a File here to Upload</span> <el:button ID="SelectButton" label="SELECT FILE" /></td>
</tr>
<tr>
 <td class="label">Version Information</td>
 <td class="data">Version <el:text name="version" value="${latest.version}" size="1" max="2" /> Build <el:text className="bld" name="build" value="${latest.clientBuild}" size="2" max="3" />
<span id="beta" style="display:none;"> Beta <el:text name="beta" value="${latest.getBeta()}" size="1" max="2" /></span></td>
</tr>
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:box name="isBeta" value="true" label="This is a Beta build" checked="${latest.isBeta()}" onChange="void golgotha.local.setBeta(this)" /></td>
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
 <td><el:button ID="SaveButton" type="submit" label="UPDATE INCREMENTAL INSTALLER" /></td>
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
golgotha.util.disable('SaveButton', true); golgotha.util.display('beta', ${latest.isBeta()});
golgotha.local.r = new Resumable({chunkSize:524288, withCredentials:true, chunkNumberParameterName:'c', chunkSizeParameterName:'cs', totalChunksParameterName:'cc', totalSizeParameterName:'ts', xhrTimeout:25000, fileType:['exe']});
var dt = document.getElementById('dropTarget');
golgotha.local.r.assignDrop(dt);
golgotha.local.r.assignBrowse(document.getElementById('SelectButton'));
golgotha.local.r.on('fileAdded', function(f, ev) {
    golgotha.local.file = f;
    dt.innerHTML = f.file.name + ', ' + f.file.size + ' bytes';
    golgotha.local.r.opts.target = '/upload/acars/' + f.file.name;
    golgotha.util.disable('SaveButton', false);
});

golgotha.local.pb = new ProgressBar.Line('#progressBar', {color:'#1a4876', text:{value:'', className:'pri', style:{color:'#ffff'}}, fill:'#1a4876'});
golgotha.local.showProgress = function(doShow) {
    var pr = golgotha.util.getElementsByClass('progress', 'tr');
    pr.forEach(function(r) { golgotha.util.display(r, doShow); });
};

golgotha.local.setBeta = function(cb) { golgotha.util.display('beta', cb.checked); };
golgotha.local.updateProgress = function() {
    var p = golgotha.local.r.progress();
    golgotha.local.pb.setText(Math.round(p * 100) + '% complete');
    golgotha.local.pb.animate(p, {duration: 50});
    if (p >= 1) {
    	var f = document.forms[0];
        console.log('Upload Complete');
        golgotha.local.showProgress(false);
        golgotha.local.uploadComplete = true;
        golgotha.form.submit(f);
        f.submit();
        return true;
    }
    
    window.setTimeout(golgotha.local.updateProgress, 65);
    return true;
};
</script>
</body>
</html>

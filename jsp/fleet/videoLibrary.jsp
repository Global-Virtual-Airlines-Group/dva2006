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
<title><content:airline /> Video Library</title>
<content:css name="main" />
<content:css name="view" />
<content:pics />
<content:js name="common" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<script type="text/javascript">
golgotha.local.play = function(id, name) {
	var tbody = golgotha.util.getElementsByClass('', 'tbody', document.getElementById('videoList'))[0];
	golgotha.local.stop();

	var r = document.createElement('tr');
	var c = document.createElement('td'); c.setAttribute('colspan', '5');

	// Create the video element
	var v = document.createElement('video'); v.setAttribute('controls', 'true');
	var src = document.createElement('source');
	src.type = 'video/mp4; codecs="avc1.4D401E, mp4a.40.2"'; src.src='/video/' + name;
	v.appendChild(src); c.appendChild(v); r.appendChild(c);
	golgotha.local.video = {vid:v, row:r, ofs:id};
	tbody.insertBefore(r, document.getElementById('video-' + (id+1)));
	v.play();
	return true;
};

golgotha.local.stop = function() {
	var tbody = golgotha.util.getElementsByClass('', 'tbody', document.getElementById('videoList'))[0];
	if (golgotha.local.video != null) {
        golgotha.local.video.vid.pause();
        tbody.removeChild(golgotha.local.video.row);
        delete golgotha.local.video;
    }

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
<view:table cmd="tvlibrary" ID="videoList">
<!-- Table Header Bar -->
<tr class="title caps">
 <td style="width:25%">TITLE</td>
 <td style="width:7%">&nbsp;</td>
 <td class="nophone" style="width:10%">SIZE</td>
<c:choose>
<c:when test="${access.canCreateVideo}">
 <td class="nophone" style="width:10%"><el:cmdbutton url="tvideo" op="edit" label="NEW VIDEO" /></td>
</c:when>
<c:otherwise>
 <td class="nophone" style="width:10%">&nbsp;</td>
</c:otherwise>
</c:choose>
 <td class="nophone">VIDEO DESCRIPTION</td>
</tr>

<!-- Table Data Section -->
<c:set var="rowID" value="0" scope="page" />
<c:forEach var="video" items="${viewContext.results}">
<c:set var="hasFile" value="${video.file().exists()}" scope="page" />
<c:set var="rowID" value="${rowID + 1}" scope="page" />
<view:row entry="${video}" ID="video-${rowID}">
<c:if test="${access.canEditVideo}">
 <td class="pri bld"><el:cmd url="tvideo" linkID="${video.fileName}" op="edit">${video.name}</el:cmd></td>
</c:if>
<c:if test="${!access.canEditVideo}">
 <td class="pri bld"><el:link url="/video/${video.fileName}">${video.name}</el:link></td>
</c:if>
<c:if test="${hasFile}">
 <td><a href="javascript:void golgotha.local.play(${rowID}, '${video.fileName}')"><el:img src="library/play.png" caption="View Video" x="48" y="48" className="noborder" /></a></td>
</c:if>
<c:if test="${!hasFile}">
 <td><a href="javascript:void golgotha.local.stop()"><el:img src="library/error.png" caption="No Video" x="48" y="48" className="noborder" /></a></td>
</c:if>
 <td class="sec bld nophone"><fmt:int value="${video.size / 1024}" />K</td>
 <td class="small left nophone" colspan="2"><fmt:text value="${video.description}" /></td>
</view:row>
</c:forEach>

<!-- Scroll Bar row -->
<tr class="title">
 <td colspan="5">&nbsp;</td>
</tr>
</view:table>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

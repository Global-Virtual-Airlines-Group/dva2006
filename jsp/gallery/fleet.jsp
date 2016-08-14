<!DOCTYPE html>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/dva_content.tld" prefix="content" %>
<%@ taglib uri="/WEB-INF/dva_html.tld" prefix="el" %>
<%@ taglib uri="/WEB-INF/dva_format.tld" prefix="fmt" %>
<html lang="en">
<head>
<title><content:airline /> Fleet Gallery</title>
<content:expire expires="60" />
<content:css name="main" />
<content:css name="form" />
<content:pics />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<content:js name="common" />
<content:sysdata var="imgDB" name="airline.db" />
<script type="text/javascript">
golgotha.local.selectAircraft = function(combo)
{
if (!golgotha.form.comboSet(combo)) return false;

// Get the image object and its description object
var img = document.getElementById('FleetPic');
var desc = document.getElementById('FleetDesc');

// Load the picture in its place, save the description
img.src = '/gallery/${imgDB}/0x' + escape(golgotha.form.getCombo(combo)) + '.jpg';
desc.innerHTML = golgotha.local.dList[combo.selectedIndex - 1];
desc.focus();
return true;
};

golgotha.local.dList = <fmt:jsarray items="${fleetGalleryDesc}" />;
</script>
</head>
<content:copyright visible="false" />
<body>
<content:page>
<%@ include file="/jsp/main/header.jspf" %> 
<%@ include file="/jsp/main/sideMenu.jspf" %>
<content:sysdata var="imgPath" name="path.img" />

<!-- Main Body Frame -->
<content:region id="main">
<el:form action="fleet.do" method="get" validate="return false">
<el:table className="form">
<tr class="title caps">
 <td colspan="2"><content:airline /> FLEET GALLERY</td>
</tr>
<tr>
 <td class="label">Select Aircraft</td>
 <td><el:combo name="Aircraft" size="1" idx="1" options="${fleetGallery}" firstEntry="[ SELECT AIRCRAFT ]" onChange="void golgotha.local.selectAircraft(this)" /></td>
</tr>
<tr>
 <td class="label">Description</td>
 <td><span id="FleetDesc"></span></td>
</tr>
<tr valign="middle">
 <td colspan="2" class="mid"><img id="FleetPic" style="width:100%; max-width:600px; max-height:450px;" src="/${imgPath}/blank.png" alt="<content:airline /> Fleet Gallery" /></td>
</tr>
<tr class="title">
 <td colspan="2">&nbsp;</td>
</tr>
</el:table>
</el:form>
<content:copyright />
</content:region>
</content:page>
<content:googleAnalytics />
</body>
</html>

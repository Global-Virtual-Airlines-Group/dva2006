<%@ taglib uri="/WEB-INF/dva_jspfunc.tld" prefix="fn" %>
<!-- Soundex Potential Match list -->
<tr class="title caps">
 <td colspan="2"><fmt:int value="${fn:sizeof(soundexUsers)}" /> POTENTIAL MATCHING USERS</td>
</tr>

<c:forEach var="person" items="${soundexUsers}">
<c:set var="personLoc" value="${userData[person.ID]}" scope="request" />
<tr>
 <td class="label">&nbsp;</td>
 <td class="data"><el:profile location="${personLoc}">${person.name}</el:profile> 
<a href="mailto:${person.email}" class="small">${person.email}</a>, registered on <fmt:date date="${person.createdOn}" /></td>
</tr>
</c:forEach>

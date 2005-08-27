<content:pics />
<c:if test="${!empty statusUpdates}">
<content:filter roles="HR">
<c:set var="isHR" value="${true}" scope="request" />
</content:filter>

<!-- Pilot Status History -->
<tr class="title caps">
 <td colspan="${cspan + 1}">PROMOTION / STATUS HISTORY</td>
</tr>
<tr class="title mid caps">
 <td>DATE</td>
 <td colspan="2">DESCRIPTION</td>
 <td colspan="2">UPDATED BY</td>
 <td colspan="2">UPDATE TYPE</td>
</tr>

<!-- Pilot Status History Data -->
<c:forEach var="update" items="${statusUpdates}">
<c:if test="${(update.type != 0) || isHR}">
<tr class="mid">
 <td class="pri bld"><fmt:date fmt="d" date="${update.createdOn}" /></td>
 <td colspan="2">${update.description}</td>
 <td class="pri" colspan="2">${update.firstName} ${update.lastName}</td> 
 <td class="sec" colspan="2">${update.typeName}</td>
</tr>
</c:if>
</c:forEach>
</c:if>

<%@include file="/WEB-INF/jsp/include.jsp" %>

<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" type="text/css" href="style/main.css" />
    <title>Contents of <c:out value="${space.metadata.name}"/></title>
  </head>
  <body>
    <form action="spaces.htm" method="get">
      <input type="hidden" name="accountId" value="<c:out value="${space.accountId}"/>" />                  
      <input type='submit' value="<- Back to Spaces Listing"/>
    </form> 

    <c:if test="${not empty error}">
      <div id="error"><c:out value="${error}" /></div>
    </c:if>

    <div id="contents">
      <h2><c:out value="${space.metadata.name}"/></h2>
      <h4><c:out value="${space.metadata.count}"/> Items</h4>
      <table id="content_list">
        <c:forEach items="${space.contents}" var="content" varStatus="status">
          <tr>
            <td>
              <c:out value="${status.count}."/>
            </td>
            <td>
              <c:out value="${content}"/>
            </td>        
            <td>
              <form action="content.htm"  method="get" target="content_target">
                <input type="hidden" name="accountId" value="<c:out value="${space.accountId}"/>" />
                <input type="hidden" name="spaceId" value="<c:out value="${space.spaceId}"/>" />
                <input type="hidden" name="contentId" value="<c:out value="${content}"/>" />                       
                <input type='submit' value="View Properties"/>
              </form>
            </td>          
            <td>
              <form action="content/<c:out value="${space.accountId}/${space.spaceId}/${content}"/>">
                <input type='submit' value="Download Content"/>
              </form>
            </td>
            <td>
              <form action="removeContent.htm" method="post">
                <input type="hidden" name="action" value="delete" />
                <input type="hidden" name="accountId" value="<c:out value="${space.accountId}"/>" />
                <input type="hidden" name="spaceId" value="<c:out value="${space.spaceId}"/>" />
                <input type="hidden" name="contentId" value="<c:out value="${content}"/>" />                       
                <input type='submit' value="Delete"/>
              </form>          
            </td>          
          </tr>
        </c:forEach>
      </table>       
    
      <h4>Add Content</h4>
      <form action="addContent.htm" method="post" enctype="multipart/form-data">
        <input type="hidden" name="action" value="add" />
        <input type="hidden" name="accountId" value="<c:out value="${space.accountId}"/>" />
        <input type="hidden" name="spaceId" value="<c:out value="${space.spaceId}"/>" />
        <p>
          <label for="contentId">Content ID</label>
          <input type="text" id="contentId" name="contentId" />
        </p>
        <p>
          <label for="contentName">Content Name</label>
          <input type="text" id="contentName" name="contentName" />
        </p>
        <p>
          <label for="contentMimetype">Content MIME Type</label>
          <input type="text" id="contentMimetype" name="contentMimetype" />
        </p>
        <p>        
          <label for="file">File</label>
          <input type="file" id="file" name="file">
        </p>
        <p>                 
          <input type='submit' value="Add"/>
        </p>
      </form>        
    </div>

    <iframe id='content_target' name='content_target' src='#'></iframe>

  </body>
</html>
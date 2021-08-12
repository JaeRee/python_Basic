<!-- Quiz.jsp -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
	<title>Quiz</title>
</head>
<body>
<p><a href="home">home</a></p>
<h1>Quiz</h1>

<p> 파일 업로드</p>
<form action="QuizUploadFile" method="post" enctype="multipart/form-data">
	<input type="file" name="uploadFile1"/><br/>
	<input type="file" name="uploadFile2"/><br/>
	<input type="submit">
</form>

<c:forEach var="url" items="${urlLst }">
	<img src="${url }"><br/>
</c:forEach>
</body>
</html>


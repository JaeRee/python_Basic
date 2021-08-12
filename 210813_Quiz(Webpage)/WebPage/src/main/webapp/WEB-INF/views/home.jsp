<!--
1. aws에 그룹 추가(S3 권한)
2. 사용자 추가(프로그래밍 방식 액세스)
3. src->main->webapp->WEB-INF에 props 폴더 생성 후 aws.properties 파일 생성 및 계정 정보 입력
4. 계정정보를 얻기 위해 spring->appServelt에 있는 servlet-context.xml에 코드 입력
5. view page(jsp page) 설정
6. controller 설정

web server에 접근하게 되면 아래와 같은 주소로 접근하게 된다. 
localhost:8080/WebPage 

localhost는 내 컴퓨터에 설치되어 있는 서버에 접근한다는 의미로 127.0.0.1의 IP를 사용한다.
8080은 서버(내 컴퓨터)로 접근한 후 들어갈 문 정도로 생각하면 된다. tomcat 서버에 접근할 경우 사용한다.
WebPage는 tomcat서버에서 사용되는 여러 웹프로그램 중 하나로 우리가 만든 프로젝트가 된다.
프로젝트가 여러 개일 경우 구분하기 위한 이름이다.
-->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<!-- 주석은 ctrl + shift + / -->
<!-- 현재 페이지에서 한글이 깨지는 현상을 막기 위한 코드 -->
<%@ page contentType="text/html; charset=UTF-8" %>
<html>
<head>
	<title>Home</title>
</head>
<body>
<h1>
	<!-- Hello world! -->
	인증 테스트  
</h1>

<%-- <P>  The time on the server is ${serverTime}. </P> --%>
<!-- 서버에 접속하기 위해 a 태그를 사용 -->
<p>1. 인증키 얻어오기(<a href='authKey'>클릭</a>)</p>
<%-- 서버에서 값을 전달 받을 경우 ${전달받을 키 } 형식으로 사용하다. --%>
${authKey }

<p>2. Quiz(<a href="quiz">클릭</a>)</p>
<!-- controller에서 전달한 값 출력 -->
${quiz }

<p>3. S3 얻기(<a href="getS3">클릭</a>)</p>
${regionName }

<p>5. Quiz(<a href="quiz2">클릭</a>)</p>

<p>6. 이미지 분석(<a href="rekognition">클릭</a>)</p>

<p>5. Quiz2(<a href="quiz3">클릭</a>)</p>
</body>
</html>


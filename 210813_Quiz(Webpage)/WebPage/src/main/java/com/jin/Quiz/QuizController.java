package com.jin.Quiz;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

/*
 * 새롭게 패키지를 만들고 controller를 생성할 경우 클래스명 위에 @Controller를 추가 해야 한다.
 * 이 Controller를 동작시키기 위해 spring->appServlet->servlet-context.xml을 열고
 * context:component-scan을 추가해야 controller 및 service를 이용할 수 있다.
 */

@Controller
public class QuizController {
	private static final Logger logger = LoggerFactory.getLogger(QuizController.class);
	
//	@Service로 생성된 구현체(class, QuizServiceImpl)를 연결할 경우 Autowired를 사용함.
	@Autowired
	private IQuizService iQuizServ;

	private final String BUCKETNAME = "infiscap-bucket2";
	
	@RequestMapping(value = "/QuizUploadFile")
	public String QuizUploadFile(Model model, HttpServletRequest request) {
//		logger.warn("QuizUploadFile");
		List<URL> urlLst = new ArrayList<URL>();
		
//		1. 파일을 서버에 전달
//		serviceImpl에서 파일 정보를 얻는다.
		List<MultipartFile> multiFileLst = iQuizServ.UploadFile(request);
//		logger.warn(multiFileLst.size()+"");
//		얻은 정보를 S3에 저장한다.
		for(MultipartFile multiFile:multiFileLst) {
//			logger.warn(multiFile.getOriginalFilename());

//			2. 전달 받은 파일을 S3에 저장
			String fileName = iQuizServ.UploadS3(BUCKETNAME , multiFile);
			
//			3. S3에 저장된 파일의 권한 변경
			iQuizServ.setGrant(BUCKETNAME, fileName);
			
//			4. 저장된 파일의 URL 얻기
			URL imgUrl = iQuizServ.getImgUrl(BUCKETNAME, fileName);
//			logger.warn(imgUrl.toString());
			urlLst.add(imgUrl);
		}
		model.addAttribute("urlLst", urlLst);
		return "Quiz";
	}
}











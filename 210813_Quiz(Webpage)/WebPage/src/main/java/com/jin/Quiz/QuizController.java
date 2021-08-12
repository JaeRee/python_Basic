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
 * ���Ӱ� ��Ű���� ����� controller�� ������ ��� Ŭ������ ���� @Controller�� �߰� �ؾ� �Ѵ�.
 * �� Controller�� ���۽�Ű�� ���� spring->appServlet->servlet-context.xml�� ����
 * context:component-scan�� �߰��ؾ� controller �� service�� �̿��� �� �ִ�.
 */

@Controller
public class QuizController {
	private static final Logger logger = LoggerFactory.getLogger(QuizController.class);
	
//	@Service�� ������ ����ü(class, QuizServiceImpl)�� ������ ��� Autowired�� �����.
	@Autowired
	private IQuizService iQuizServ;

	private final String BUCKETNAME = "infiscap-bucket2";
	
	@RequestMapping(value = "/QuizUploadFile")
	public String QuizUploadFile(Model model, HttpServletRequest request) {
//		logger.warn("QuizUploadFile");
		List<URL> urlLst = new ArrayList<URL>();
		
//		1. ������ ������ ����
//		serviceImpl���� ���� ������ ��´�.
		List<MultipartFile> multiFileLst = iQuizServ.UploadFile(request);
//		logger.warn(multiFileLst.size()+"");
//		���� ������ S3�� �����Ѵ�.
		for(MultipartFile multiFile:multiFileLst) {
//			logger.warn(multiFile.getOriginalFilename());

//			2. ���� ���� ������ S3�� ����
			String fileName = iQuizServ.UploadS3(BUCKETNAME , multiFile);
			
//			3. S3�� ����� ������ ���� ����
			iQuizServ.setGrant(BUCKETNAME, fileName);
			
//			4. ����� ������ URL ���
			URL imgUrl = iQuizServ.getImgUrl(BUCKETNAME, fileName);
//			logger.warn(imgUrl.toString());
			urlLst.add(imgUrl);
		}
		model.addAttribute("urlLst", urlLst);
		return "Quiz";
	}
}











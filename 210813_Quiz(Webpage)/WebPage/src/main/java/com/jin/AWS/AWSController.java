package com.jin.AWS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests for the application home page.
 */
/*
1. 패키지 만들기 - com.jin.AWS
2. IAWSService 만들기
3. AWSServiceImpl
  - S3 정보를 얻기 위한 함수 생성
4. HomeController를 복사하여 com.jin.AWS 패키지에 붙여넣기
5. HomeController를 AWSController로 변경 및 내부 코드 수정
6. src->main->webapp->WEB-INF->spring->appServlet에 있는 servlet-context.xml 수정
7. home.jsp 수정
 */
@Controller
public class AWSController {
	private static final Logger logger = LoggerFactory.getLogger(AWSController.class);
	
	@Autowired
	private IAWSService iAWSServ;
	
	@RequestMapping(value = "/getS3")
	public String getS3(Model model) {
		String regionName = iAWSServ.getRegionName();
		model.addAttribute("regionName", regionName);
		return "home";
	}
}




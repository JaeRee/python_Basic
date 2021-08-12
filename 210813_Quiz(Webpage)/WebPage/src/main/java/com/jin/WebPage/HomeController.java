package com.jin.WebPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/*
	 * controller에서 Service를 호출할 경우 interface를 적용한다.
	 * private은 고정적으로 사용한다.
	 * ITestService는 Interface의 자료형을 작성한 것이다.
	 * iTestServ는 변수명이다.
	 * @Autowired 역시 고정으로 사용한다.
	 */
	@Autowired
	private ITestService iTestServ;
	
	/* 
	 * @는 annotation이라고 하여 내부적으로 처리되는 메소드(함수)이다.
	 * aws.properties에 있는 정보를 @Value를 통해 얻어온다.
	 * @Value 내부에 있는 이름과  aws.properties의 이름은 같아야 한다.
	 * 이렇게 얻어온 정보를 아래에 있는 private String accessKey;에 저장한다.
	 */
	@Value("${aws.accessKey}")private String accesskey;
	@Value("${aws.secretKey}")private String secretkey;
	
	/**
	 * Simply selects the home view to render by returning its name.
	 * 
	 * localhost:8080/WebPage이 홈이다.
	 * 위의 주소로 서버에 접속을 시도하면 WebPage이후의 /가 있는것으로 인식되어
	 * localhost:8080/WebPage/로 접근하게 되며 
	 * value="/"에서 이를 받아 내부 내용을 처리하게 된다.
	 * 즉, 처음 홈페이지(첫 페이지)로 접근하게 되면 아래의 메소드(함수)로 이동하게 된다. 
	 */
	@RequestMapping(value = {"/", "/home"})
	public String home() {
		logger.info("홈페이지 접속");
		/* 메소드 진행 이후 아래의 이름과 똑같은 jsp 페이지를 호출하게 됨 */
		return "home";
	}
//	@RequestMapping(value = "home")
//	public String home2() {
//		logger.info("홈페이지 접속");
//		/* 메소드 진행 이후 아래의 이름과 똑같은 jsp 페이지를 호출하게 됨 */
//		return "home";
//	}
	/* 
	 * jsp 페이지에 있는 '1. 인증키 얻어오기(클릭)'을 누르면 
	 * localhost:8080/WebPage/authKey로 접근하게 된다.
	 * 기본 홈인 localhost:8080/WebPage을 제외하면 /authKey로 접근하게 되는 것이다.
	 * controller에는 /authKey를 받는 @RequestMapping이 있어야 하며 대소문자를 구분한다.
	 * value의 값과 메소드(함수) 이름은 같게 만드는 것이 편하다.(중복 방지)
	 */
	@RequestMapping(value = "/authKey")
//	Model은 Interface로(몰라도 됨) 값을 전달할 때 사용하며 model이라는 변수로 설정
	public String authKey(Model model) {
		logger.info(accesskey + " : "+secretkey);
		iTestServ.TestMethod();
//		값을 jsp로 전달할 경우 model.addAttribute를 이용하여 
//		첫 인자는 전달할 key이고 두 번째 값은 value이다.
		model.addAttribute("authKey", accesskey + " : "+secretkey);
		return "home";
	}
	@Autowired
	private IService iServ;
	
	@RequestMapping(value = "/quiz")
	public String quiz(Model model) {
		iServ.HelloWorld();
//		ReturnMethod에서 반환한 값을 ret에 저장
		String ret = iServ.ReturnMethod();
//		저장된 값을 quiz라는 key로 home.jsp에 전달
		model.addAttribute("quiz", ret);
		return "home";
	}
	
	@RequestMapping(value = "/quiz2")
	public String quiz2(Model model) {
		return "Quiz";
	}
	@RequestMapping(value = "/rekognition")
	public String rekognition(Model model) {
		return "rekognition";
	}
	@RequestMapping(value = "/quiz3")
	public String quiz3(Model model) {
		return "Quiz2";
	}
}




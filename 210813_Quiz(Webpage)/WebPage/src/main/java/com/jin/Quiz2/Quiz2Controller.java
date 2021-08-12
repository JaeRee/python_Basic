package com.jin.Quiz2;

import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.amazonaws.services.rekognition.model.FaceDetail;
import com.jin.Rekognition.IRekoService;

@Controller
public class Quiz2Controller {
	private static final Logger logger = LoggerFactory.getLogger(Quiz2Controller.class);
	
	@Autowired
	private IQuiz2Service iQuizServ;
	
	@RequestMapping(value = "/Quiz2UploadFile")
	public String QuizUploadFile(Model model, HttpServletRequest request) {
		logger.warn("Quiz2UploadFile");
		model.addAttribute("urlLst", iQuizServ.Upload(request));
		return "Quiz2";
	}	
	@RequestMapping(value = "/getRandomImg")
	public String getRandomImg(Model model) {
		String imgName = iQuizServ.getRandomImg();
		URL imgUrl = iQuizServ.getImgUrl(imgName);
		
		model.addAttribute("imgName", imgName);
		model.addAttribute("imgUrl", imgUrl);
		return "Quiz2";
	}
	
	@RequestMapping(value = "QuizAnalyzeFace")
	public String QuizAnalyzeFace(
			Model model,
			@RequestParam("imgName") String fileName
			) {
		List<FaceDetail> faceDetails = iQuizServ.AnalyzeFace(fileName);
		
//		logger.warn(faceDetails.get(0).toString());
		String msg = iQuizServ.MusicRecommend(faceDetails.get(0));
		model.addAttribute("msg", msg);
		return "Quiz2";
	}
	@RequestMapping(value = "Quiz3")
	public String Quiz3(
			Model model,
			@RequestParam("bucketName") String bucketName,
			@RequestParam("fileName") String fileName
			) {		
//		logger.warn(bucketName + " : " + fileName);
		model.addAttribute("recommand_msg", iQuizServ.Quiz3(bucketName, fileName));
		return "Quiz2";
	}
}











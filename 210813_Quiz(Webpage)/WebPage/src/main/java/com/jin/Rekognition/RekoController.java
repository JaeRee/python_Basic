package com.jin.Rekognition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RekoController {
	private static final Logger logger = LoggerFactory.getLogger(RekoController.class);
	@Autowired
	private IRekoService iRekoServ;
	@RequestMapping(value = "AnalyzeFace")
	public String AnalyzeFace(
			Model model,
			@RequestParam("imgName") String fileName
			) {
//		logger.warn(fileName);
		model.addAttribute("faceDetails", iRekoServ.AnalyzeFace(fileName));
		return "rekognition";
	}
	@RequestMapping(value = "BucketFileList")
	public String BucketFileList(Model model) {
		model.addAttribute("fileLst", iRekoServ.BucketFileList());
		return "rekognition";
	}
	
	@RequestMapping(value = "AnalyzeImage")
	public String AnalyzeImage(
			Model model,
			@RequestParam("bucketName") String bucketName,
			@RequestParam("fileName") String fileName
			) {
//		logger.warn(bucketName + fileName);
		model.addAttribute("imgLabels", iRekoServ.AnalyzeImage(bucketName, fileName));
		return "rekognition";
	}
}

package com.jin.Rekognition;

import java.util.List;

import com.amazonaws.services.rekognition.model.FaceDetail;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public interface IRekoService {
	public List<FaceDetail> AnalyzeFace(String fileName);
	public List<S3ObjectSummary> BucketFileList();
	public List<Label> AnalyzeImage(String bucketName, String fileName);
}

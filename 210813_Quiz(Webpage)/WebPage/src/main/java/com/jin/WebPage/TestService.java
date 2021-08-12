package com.jin.WebPage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
 * 클래스가 인터페이스를 상속받기 위해서는 implements를 이용한다.
 * 상속을 받게 되면 인터페이스에 정의되어 있는 메소드(함수)를 클래스에서 구현해야 한다.
 */
@Service
public class TestService implements ITestService{
	private static final Logger logger = LoggerFactory.getLogger(TestService.class);
	
	/* 
	 * 상속받아서 구현되었다는 의미로 @Override를 사용한다. 
	 */ 
	@Override
	public void TestMethod() {
		logger.info("TestMethod 실행");
	}
}

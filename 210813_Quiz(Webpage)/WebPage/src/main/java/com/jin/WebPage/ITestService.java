package com.jin.WebPage;

/*
 * spring에서 controller는 데이터의 분기를 맡게 된다.
 * 기능에 대한 구현은 Service에서 데이터 처리는 Model(controller의 model과 다름)에서 처리한다.
 * 기능 구현시 interface + class 형식으로 사용하다.
 * interface는 메소드의 원형만 작성하고 class는 interface를 상속 받아 내용을 구현한다.
 */
//interface는 메소드의 원형을 나열한다.
public interface ITestService {
//	메소드의 정의는 다음과 같은 패턴으로 작성한다.
//	public [반환자료형] 메소드명([파라미터])
//	void는 반환자료형이 없는 경우 사용한다.
	public void TestMethod();
}

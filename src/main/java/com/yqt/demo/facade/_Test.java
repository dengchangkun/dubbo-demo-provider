package com.yqt.demo.facade;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class _Test {

	public static void main(String[] args) throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("META-INF/spring/applicationContext-soa-provider.xml");
		DemoFacade facade = applicationContext.getBean(DemoFacade.class);
		System.out.println(facade.hello("张三"));
		applicationContext.start();
		System.in.read();
		com.alibaba.dubbo.container.Main.main(args);
	}
}

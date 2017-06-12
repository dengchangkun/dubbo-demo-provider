package com.yqt.demo.facade.impl;

import org.springframework.stereotype.Component;

import com.yqt.demo.facade.DemoFacade;

@Component
public class DemoFacadeImpl implements DemoFacade {

	@Override
	public String hello(String name) {
			return "hello,"+name;
	}
}

package com.luxoft.sample;

import org.springframework.stereotype.Service;

@Service
public class SampleServiceBean {

	public String getStringValue() {
		return "sample string value from service";
	}
	
}

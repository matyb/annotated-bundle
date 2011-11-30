package com.sandwich.annotatedbundle;

import java.util.Map;

public abstract class Entry implements java.util.Map.Entry<String, Map<String, String>>{

	private Map<String, String> value;
	private final String key;
	
	public Entry(String key){
		this.key = key;
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public Map<String, String> getValue() {
		if(value == null){
			value = readProperties();
		}
		return value;
	}
	
	abstract Map<String, String> readProperties();

	@Override
	public Map<String, String> setValue(Map<String, String> value) {
		Map<String, String> temp = this.value;
		this.value = value;
		return temp;
	}
	
}

package com.example;

import java.util.ArrayList;
import java.util.List;

public class ClassWithValues {
	
	
	private List<String> values = new ArrayList<>();
	
	public void add(String value) {
		this.values.add(value);
	}
	
	public List<String> getValues() {
		return this.values;
	}
}

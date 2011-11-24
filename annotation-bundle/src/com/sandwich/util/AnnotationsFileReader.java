package com.sandwich.util;

import java.util.Map;
import java.util.ResourceBundle;

public class AnnotationsFileReader extends FileReader {

	/**
	 * end of a annotations file name
	 */
	private static final String ANNOTATIONS_FILE_SUFFIX = ".annotations";
	/**
	 * start of a bounded key in annotation
	 */
	private static final String BOUND_KEY_START = ANNOTATION_LINE_START+" @";
	
	public AnnotationsFileReader(ResourceBundle bundle){
		super(bundle);
	}
	
	@Override
	protected boolean isNullFileAcceptable() {
		return true;
	}
	
	/**
	 * end of a annotations file name
	 */
	@Override
	public String getFileSuffix() {
		return ANNOTATIONS_FILE_SUFFIX;
	}
	
	@Override
	void captureProperties(Map<String, Map<String, String>> propertyAttributes, String currentLine, String previousLine, String key) {
		int indexOfFirstDelimiter = currentLine.indexOf(ANNOTATION_VALUE_DELIMITER);
		if(currentLine.startsWith(BOUND_KEY_START) && indexOfFirstDelimiter > BOUND_KEY_START.length()){
			String boundedKey =  currentLine.substring(BOUND_KEY_START.length(), indexOfFirstDelimiter);
			if(boundedKey.equals(key)){
				String line = ANNOTATION_LINE_START + currentLine.substring(indexOfFirstDelimiter + 1);
				propertyAttributes.put(boundedKey, parseAttributesFromLine(line));
			}
		}
	}
}

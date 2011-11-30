package com.sandwich.annotatedbundle;

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
		isNullFileAcceptable = true;
	}
	
	/**
	 * end of a annotations file name
	 */
	@Override
	public String getFileSuffix() {
		return ANNOTATIONS_FILE_SUFFIX;
	}
	
	@Override
	Map.Entry<String, Map<String, String>> captureProperties(String currentLine, String previousLine) {
		int indexOfFirstDelimiter = currentLine == null ? -1 : currentLine.indexOf(ANNOTATION_VALUE_DELIMITER);
		if(indexOfFirstDelimiter > BOUND_KEY_START.length() && currentLine.startsWith(BOUND_KEY_START)){
			final String boundedKey =  currentLine.substring(BOUND_KEY_START.length(), indexOfFirstDelimiter);
			final String line = currentLine.substring(indexOfFirstDelimiter + 1);
			return new Entry(boundedKey) {
				@Override Map<String, String> readProperties() {
					return parseAttributesFromLine(line);
				}
			};
		}
		return null;
	}
}
